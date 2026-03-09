package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.lang.java.parser.JavaParser
import com.github.tvinke.algorilla.lang.java.parser.JavaParserBaseVisitor
import com.github.tvinke.algorilla.lang.java.parser.classifyChainedCall
import com.github.tvinke.algorilla.lang.java.parser.classifyStandaloneCall
import com.github.tvinke.algorilla.lang.java.parser.extractVariableName
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
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
@Suppress("TooManyFunctions")
internal class GroovyIRVisitor(
    private val filePath: String,
) : JavaParserBaseVisitor<List<IRNode>>() {
    override fun defaultResult(): List<IRNode> = emptyList()

    override fun aggregateResult(
        aggregate: List<IRNode>,
        nextResult: List<IRNode>,
    ): List<IRNode> = aggregate + nextResult

    override fun visitMethodDeclaration(ctx: JavaParser.MethodDeclarationContext): List<IRNode> {
        val name = ctx.identifier().text
        val params = extractParameters(ctx.formalParameters())
        val body = ctx.methodBody()?.let { visitChildren(it) } ?: emptyList()
        // Groovy constructors parsed as methods: name starts with uppercase, no explicit return type
        val isCtor = name.first().isUpperCase() && ctx.typeTypeOrVoid()?.text?.let { it == "void" || it == name } != true

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = name,
                parameters = params,
                isConstructor = isCtor,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    override fun visitConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext): List<IRNode> {
        val name = ctx.identifier()?.text ?: "<init>"
        val params = extractParameters(ctx.formalParameters())
        val body = ctx.block()?.let { visitChildren(it) } ?: emptyList()

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = name,
                parameters = params,
                isConstructor = true,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    override fun visitStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        if (ctx.FOR() != null) return handleForStatement(ctx)
        if (ctx.WHILE() != null || ctx.DO() != null) return handleWhileStatement(ctx)
        return visitChildren(ctx)
    }

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

    private fun handleForStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val enhancedFor = ctx.forControl()?.enhancedForControl()
        val body = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        return if (enhancedFor != null) {
            listOf(LoopNode(LoopKind.FOR_EACH, extractVariableName(enhancedFor.expression()?.text), locationOf(ctx), body))
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
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc)
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
        return classifyStandaloneCall(name, loc, argNodes)
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
                visited.ifEmpty { listOf(GenericNode("arg", locationOf(expr), emptyList())) }
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
