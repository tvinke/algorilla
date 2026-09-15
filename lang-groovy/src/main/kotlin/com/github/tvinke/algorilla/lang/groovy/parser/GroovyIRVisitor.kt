package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.lang.java.parser.JavaParser
import com.github.tvinke.algorilla.lang.java.parser.JavaParserBaseVisitor
import com.github.tvinke.algorilla.lang.java.parser.buildConstructorDecl
import com.github.tvinke.algorilla.lang.java.parser.classifyChainedCall
import com.github.tvinke.algorilla.lang.java.parser.classifyStandaloneCall
import com.github.tvinke.algorilla.lang.java.parser.dedupedChainedLookupOrNull
import com.github.tvinke.algorilla.lang.java.parser.extractLambdaParamNames
import com.github.tvinke.algorilla.lang.java.parser.extractParameters
import com.github.tvinke.algorilla.lang.java.parser.extractSupertypes
import com.github.tvinke.algorilla.lang.java.parser.extractVariableName
import com.github.tvinke.algorilla.lang.java.parser.handleForStatement
import com.github.tvinke.algorilla.lang.java.parser.handleIfStatement
import com.github.tvinke.algorilla.lang.java.parser.handleObjectCreation
import com.github.tvinke.algorilla.lang.java.parser.handleTryCatchStatement
import com.github.tvinke.algorilla.lang.java.parser.handleWhileStatement
import com.github.tvinke.algorilla.lang.java.parser.lambdaParamCallOrNull
import com.github.tvinke.algorilla.lang.java.parser.processBlockStatements
import com.github.tvinke.algorilla.lang.java.parser.simplifyGenericType
import com.github.tvinke.algorilla.lang.java.parser.visitArgNodes
import com.github.tvinke.algorilla.lang.java.parser.withLambdaParams
import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.TypeCheck
import com.github.tvinke.algorilla.model.VariableDecl
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Visits the ANTLR parse tree (parsed with Java grammar) and produces IR nodes
 * with Groovy-specific method classification (e.g. .each{}, .collect{}, .findAll{}).
 */
@Suppress("TooManyFunctions", "LargeClass") // Visitor pattern: one visit method per AST node type
internal class GroovyIRVisitor(
    private val filePath: String,
) : JavaParserBaseVisitor<List<IRNode>>() {
    private val lambdaParams = mutableSetOf<String>()
    private var enclosingClass: String? = null

    override fun defaultResult(): List<IRNode> = emptyList()

    override fun aggregateResult(
        aggregate: List<IRNode>,
        nextResult: List<IRNode>,
    ): List<IRNode> = aggregate + nextResult

    override fun visitClassDeclaration(ctx: JavaParser.ClassDeclarationContext): List<IRNode> {
        val prev = enclosingClass
        val className = ctx.identifier()?.text
        enclosingClass = className
        val result = visitChildren(ctx)
        enclosingClass = prev
        if (className == null) return result
        val supertypes = extractSupertypes(ctx)
        return listOf(
            ClassNode(
                name = className,
                supertypes = supertypes,
                location = locationOf(ctx),
                children = result,
            ),
        )
    }

    override fun visitInstanceOfOperatorExpression(ctx: JavaParser.InstanceOfOperatorExpressionContext): List<IRNode> {
        val expr = ctx.expression() ?: return visitChildren(ctx)
        val varName = extractVariableName(expr.text, Language.GROOVY) ?: return visitChildren(ctx)
        val typeCtx = ctx.typeType() ?: return visitChildren(ctx)
        val checkedType = typeCtx.text.simplifyGenericType()
        return visit(expr) + listOf(TypeCheck(varName, checkedType, locationOf(ctx)))
    }

    override fun visitBlock(ctx: JavaParser.BlockContext): List<IRNode> =
        processBlockStatements(ctx.blockStatement(), 0, this, ::locationOf)

    override fun visitLambdaExpression(ctx: JavaParser.LambdaExpressionContext): List<IRNode> {
        val params = extractLambdaParamNames(ctx.lambdaParameters())
        return withLambdaParams(lambdaParams, params) {
            ctx.lambdaBody()?.let { visitChildren(it) } ?: emptyList()
        }
    }

    override fun visitMethodDeclaration(ctx: JavaParser.MethodDeclarationContext): List<IRNode> {
        val name = ctx.identifier().text
        val params = extractParameters(ctx.formalParameters())
        val body = ctx.methodBody()?.let { visitChildren(it) } ?: emptyList()
        // Groovy constructors parsed as methods: name starts with uppercase, no explicit return type
        val isCtor = name.first().isUpperCase() && ctx.typeTypeOrVoid()?.text?.let { it == "void" || it == name } != true
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name
        val returnType =
            ctx
                .typeTypeOrVoid()
                ?.typeType()
                ?.text
                ?.simplifyGenericType()

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                isConstructor = isCtor,
                declaringClass = enclosingClass,
                returnType = returnType,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    override fun visitConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext): List<IRNode> =
        buildConstructorDecl(ctx, enclosingClass, this, ::locationOf)

    override fun visitStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        if (ctx.FOR() != null) return handleForStatement(ctx, Language.GROOVY, this, ::locationOf)
        if (ctx.WHILE() != null || ctx.DO() != null) return handleWhileStatement(ctx, this, ::locationOf)
        if (ctx.IF() != null) return handleIfStatement(ctx, this, ::locationOf)
        if (ctx.TRY() != null) return handleTryStatement(ctx)
        return visitChildren(ctx)
    }

    private fun handleTryStatement(ctx: JavaParser.StatementContext): List<IRNode> = handleTryCatchStatement(ctx, this, ::locationOf)

    override fun visitMethodCallExpression(ctx: JavaParser.MethodCallExpressionContext): List<IRNode> {
        val methodCall = ctx.methodCall() ?: return defaultResult()
        val name =
            methodCall.identifier()?.text
                ?: methodCall.THIS()?.text
                ?: methodCall.SUPER()?.text
                ?: return defaultResult()
        val loc = locationOf(ctx)
        val argNodes = visitArgNodes(methodCall, this, ::locationOf)

        return classifyGroovyCall(name, argNodes, loc)
    }

    override fun visitMemberReferenceExpression(ctx: JavaParser.MemberReferenceExpressionContext): List<IRNode> {
        val methodCall = ctx.methodCall()
        if (methodCall != null) {
            val methodName = methodCall.identifier()?.text ?: return visitChildren(ctx)
            val targetExpr = ctx.expression()
            val targetText = targetExpr?.text ?: return visitChildren(ctx)
            val loc = locationOf(ctx)
            return handleChainedCall(methodName, targetText, methodCall, targetExpr, loc)
        }
        return visitChildren(ctx)
    }

    override fun visitObjectCreationExpression(ctx: JavaParser.ObjectCreationExpressionContext): List<IRNode> =
        handleObjectCreation(ctx, this, ::locationOf)

    override fun visitLocalVariableDeclaration(ctx: JavaParser.LocalVariableDeclarationContext): List<IRNode> {
        val typeName = ctx.typeType()?.text
        val results = mutableListOf<IRNode>()
        for (declarator in ctx.variableDeclarators()?.variableDeclarator() ?: emptyList()) {
            val varName = declarator.variableDeclaratorId()?.text ?: continue
            val initChildren = declarator.variableInitializer()?.let { visitChildren(it) } ?: emptyList()
            results.add(VariableDecl(name = varName, typeName = typeName, location = locationOf(ctx), children = initChildren))
        }
        return results
    }

    private fun handleChainedCall(
        methodName: String,
        targetText: String,
        methodCall: JavaParser.MethodCallContext,
        targetExpr: JavaParser.ExpressionContext,
        loc: SourceLocation,
    ): List<IRNode> {
        val groovyLoop = groovyLoopKindFor(methodName)
        val targetVar = extractVariableName(targetText, Language.GROOVY)
        val argNodes = visitArgNodes(methodCall, this, ::locationOf)
        val targetChildren = visit(targetExpr)
        if (groovyLoop != null) {
            return targetChildren + listOf(LoopNode(groovyLoop, targetVar, loc, argNodes))
        }
        lambdaParamCallOrNull(methodName, targetVar, argNodes, lambdaParams, loc)?.let { return targetChildren + listOf(it) }
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc, Language.GROOVY)
        dedupedChainedLookupOrNull(node, targetChildren, targetVar, argNodes)?.let { return it }
        return targetChildren + listOf(node)
    }

    private fun classifyGroovyCall(
        name: String,
        argNodes: List<IRNode>,
        loc: SourceLocation,
    ): List<IRNode> {
        val groovyLoop = groovyLoopKindFor(name)
        if (groovyLoop != null) {
            return listOf(LoopNode(groovyLoop, null, loc, argNodes))
        }
        return classifyStandaloneCall(name, loc, argNodes, Language.GROOVY)
    }

    private fun locationOf(ctx: ParserRuleContext): SourceLocation =
        SourceLocation(filePath, ctx.start.line, ctx.start.charPositionInLine + 1)
}

private fun groovyLoopKindFor(methodName: String): LoopKind? =
    when (methodName) {
        "each", "eachWithIndex" -> LoopKind.HIGHER_ORDER
        "collect", "collectEntries" -> LoopKind.HIGHER_ORDER
        else -> null
    }
