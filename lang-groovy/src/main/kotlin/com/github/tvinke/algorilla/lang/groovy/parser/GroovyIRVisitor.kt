package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.lang.java.parser.JavaParser
import com.github.tvinke.algorilla.lang.java.parser.JavaParserBaseVisitor
import com.github.tvinke.algorilla.lang.java.parser.classifyChainedCall
import com.github.tvinke.algorilla.lang.java.parser.classifyStandaloneCall
import com.github.tvinke.algorilla.lang.java.parser.extractLambdaParamNames
import com.github.tvinke.algorilla.lang.java.parser.extractVariableName
import com.github.tvinke.algorilla.lang.java.parser.handleTryCatchStatement
import com.github.tvinke.algorilla.lang.java.parser.processBlockStatements
import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
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
        enclosingClass = ctx.identifier()?.text
        val result = visitChildren(ctx)
        enclosingClass = prev
        return result
    }

    override fun visitBlock(ctx: JavaParser.BlockContext): List<IRNode> =
        processBlockStatements(ctx.blockStatement(), 0, this, ::locationOf)

    override fun visitLambdaExpression(ctx: JavaParser.LambdaExpressionContext): List<IRNode> {
        val params = extractLambdaParamNames(ctx.lambdaParameters())
        lambdaParams.addAll(params)
        val result = ctx.lambdaBody()?.let { visitChildren(it) } ?: emptyList()
        lambdaParams.removeAll(params.toSet())
        return result
    }

    override fun visitMethodDeclaration(ctx: JavaParser.MethodDeclarationContext): List<IRNode> {
        val name = ctx.identifier().text
        val params = extractParameters(ctx.formalParameters())
        val body = ctx.methodBody()?.let { visitChildren(it) } ?: emptyList()
        // Groovy constructors parsed as methods: name starts with uppercase, no explicit return type
        val isCtor = name.first().isUpperCase() && ctx.typeTypeOrVoid()?.text?.let { it == "void" || it == name } != true
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                isConstructor = isCtor,
                declaringClass = enclosingClass,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    override fun visitConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext): List<IRNode> {
        val name = ctx.identifier()?.text ?: "<init>"
        val params = extractParameters(ctx.formalParameters())
        val body = ctx.block()?.let { visitChildren(it) } ?: emptyList()
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                isConstructor = true,
                declaringClass = enclosingClass,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    override fun visitStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        if (ctx.FOR() != null) return handleForStatement(ctx)
        if (ctx.WHILE() != null || ctx.DO() != null) return handleWhileStatement(ctx)
        if (ctx.IF() != null) return handleIfStatement(ctx)
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
        val argNodes = visitArgNodes(methodCall)

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

    override fun visitObjectCreationExpression(ctx: JavaParser.ObjectCreationExpressionContext): List<IRNode> {
        val creator = ctx.creator() ?: return visitChildren(ctx)
        val typeName = creator.createdName()?.text ?: return visitChildren(ctx)
        val loc = locationOf(ctx)
        val argNodes =
            creator
                .classCreatorRest()
                ?.arguments()
                ?.expressionList()
                ?.let { visitChildren(it) } ?: emptyList()

        return listOf(ObjectCreation(typeName = typeName, location = loc, children = argNodes))
    }

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

    private fun handleIfStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val conditionNodes = ctx.expression(0)?.let { visit(it) } ?: emptyList()
        val thenBranch = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        val elseBranch = ctx.statement(1)?.let { visitChildren(it) }
        if (elseBranch != null) {
            return conditionNodes + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(ctx)))
        }
        return conditionNodes + thenBranch
    }

    private fun handleForStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val enhancedFor = ctx.forControl()?.enhancedForControl()
        val body = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        return if (enhancedFor != null) {
            val loopVarType = enhancedFor.typeType()?.text
            val loopVarName = enhancedFor.variableDeclaratorId()?.text
            val loopVarDecl =
                if (loopVarType != null && loopVarName != null) {
                    listOf(VariableDecl(loopVarName, loopVarType, null, locationOf(ctx), emptyList()))
                } else {
                    emptyList()
                }
            listOf(LoopNode(LoopKind.FOR_EACH, extractVariableName(enhancedFor.expression()?.text), locationOf(ctx), loopVarDecl + body))
        } else {
            listOf(LoopNode(LoopKind.FOR, null, locationOf(ctx), body))
        }
    }

    private fun handleWhileStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val body = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        return listOf(LoopNode(LoopKind.WHILE, null, locationOf(ctx), body))
    }

    private fun handleChainedCall(
        methodName: String,
        targetText: String,
        methodCall: JavaParser.MethodCallContext,
        targetExpr: JavaParser.ExpressionContext,
        loc: SourceLocation,
    ): List<IRNode> {
        val groovyLoop = groovyLoopKindFor(methodName)
        if (groovyLoop != null) {
            val targetVar = extractVariableName(targetText)
            val argNodes = visitArgNodes(methodCall)
            val targetChildren = visit(targetExpr)
            return targetChildren + listOf(LoopNode(groovyLoop, targetVar, loc, argNodes))
        }
        val targetVar = extractVariableName(targetText)
        val argNodes = visitArgNodes(methodCall)
        val targetChildren = visit(targetExpr)
        if (targetVar != null && targetVar in lambdaParams) {
            val call =
                FunctionCall(
                    name = methodName,
                    qualifiedTarget = targetVar,
                    arguments = argNodes,
                    location = loc,
                    children = argNodes,
                )
            return targetChildren + listOf(call)
        }
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc, Language.GROOVY)
        if (node is LookupCall && targetChildren.any { it is LookupCall && (it as LookupCall).targetVariable == targetVar }) {
            return targetChildren + argNodes
        }
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

    private fun extractParameters(ctx: JavaParser.FormalParametersContext?): List<Parameter> {
        if (ctx == null) return emptyList()
        val params = mutableListOf<Parameter>()
        ctx.formalParameter()?.let { param ->
            val name = param.variableDeclaratorId()?.text ?: return@let
            params.add(Parameter(name, param.typeType()?.text))
        }
        for (paramList in ctx.formalParameterList()) {
            for (param in paramList.formalParameter()) {
                val name = param.variableDeclaratorId()?.text ?: continue
                params.add(Parameter(name, param.typeType()?.text))
            }
        }
        return params
    }

    private fun visitArgNodes(methodCall: JavaParser.MethodCallContext): List<IRNode> {
        val expressions = methodCall.arguments()?.expressionList()?.expression() ?: return emptyList()
        return expressions
            .map { expr ->
                val visited = visit(expr)
                visited.ifEmpty { listOf(GenericNode(expr.text, locationOf(expr), emptyList())) }
            }.flatten()
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
