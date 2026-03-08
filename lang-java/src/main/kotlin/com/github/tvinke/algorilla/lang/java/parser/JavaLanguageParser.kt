package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParseException
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import io.github.oshai.kotlinlogging.KotlinLogging
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Parses Java source files into the unified IR representation using ANTLR.
 */
public class JavaLanguageParser : LanguageParser {
    override val language: Language = Language.JAVA

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".java")

    override fun parse(filePath: String): FileRoot {
        val file = File(filePath)
        if (!file.exists()) throw ParseException("File not found: $filePath")

        val input = CharStreams.fromPath(file.toPath())
        val lexer = JavaLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = JavaParser(tokens)
        parser.removeErrorListeners()

        val compilationUnit = parser.compilationUnit()
        val children = JavaIRVisitor(filePath).visit(compilationUnit)

        return FileRoot(
            filePath = filePath,
            language = Language.JAVA,
            location = SourceLocation(filePath, 1, 1),
            children = children,
        )
    }
}

/**
 * Visits the ANTLR parse tree and produces IR nodes.
 */
@Suppress("TooManyFunctions")
internal class JavaIRVisitor(
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
        val loc = locationOf(ctx)

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = name,
                parameters = params,
                location = loc,
                children = body,
            ),
        )
    }

    override fun visitConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext): List<IRNode> {
        val name = ctx.identifier()?.text ?: "<init>"
        val params = extractParameters(ctx.formalParameters())
        val body = ctx.block()?.let { visitChildren(it) } ?: emptyList()
        val loc = locationOf(ctx)

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = name,
                parameters = params,
                isConstructor = true,
                location = loc,
                children = body,
            ),
        )
    }

    override fun visitStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        if (ctx.FOR() != null) {
            return handleForStatement(ctx)
        }
        if (ctx.WHILE() != null || ctx.DO() != null) {
            return handleWhileOrDoStatement(ctx)
        }
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

        return classifyStandaloneCall(name, loc, argNodes)
    }

    override fun visitMemberReferenceExpression(ctx: JavaParser.MemberReferenceExpressionContext): List<IRNode> {
        val methodCall = ctx.methodCall()
        if (methodCall != null) {
            val methodName = methodCall.identifier()?.text ?: return visitChildren(ctx)
            val targetExpr = ctx.expression()
            val targetText = targetExpr?.text ?: return visitChildren(ctx)
            val loc = locationOf(methodCall)

            return handleChainedMethodCall(methodName, targetText, methodCall, targetExpr, loc)
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
            results.add(
                VariableDecl(
                    name = varName,
                    typeName = typeName,
                    location = locationOf(ctx),
                    children = initChildren,
                ),
            )
        }
        return results
    }

    private fun handleForStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val forControl = ctx.forControl()
        val enhancedFor = forControl?.enhancedForControl()

        if (enhancedFor != null) {
            val iterVar = enhancedFor.expression()?.text
            val body = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
            return listOf(
                LoopNode(
                    kind = LoopKind.FOR_EACH,
                    iteratedVariable = extractVariableName(iterVar),
                    location = locationOf(ctx),
                    children = body,
                ),
            )
        }

        val body = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        return listOf(
            LoopNode(
                kind = LoopKind.FOR,
                iteratedVariable = null,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    private fun handleWhileOrDoStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val body = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        return listOf(
            LoopNode(
                kind = LoopKind.WHILE,
                iteratedVariable = null,
                location = locationOf(ctx),
                children = body,
            ),
        )
    }

    private fun handleChainedMethodCall(
        methodName: String,
        targetText: String,
        methodCall: JavaParser.MethodCallContext,
        targetExpr: JavaParser.ExpressionContext,
        loc: SourceLocation,
    ): List<IRNode> {
        val targetVar = extractVariableName(targetText)
        val argNodes = visitArgNodes(methodCall)
        val targetChildren = visit(targetExpr)
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc)

        return targetChildren + listOf(node)
    }

    private fun extractParameters(ctx: JavaParser.FormalParametersContext?): List<Parameter> {
        if (ctx == null) return emptyList()
        val params = mutableListOf<Parameter>()

        ctx.formalParameter()?.let { param ->
            params.add(
                Parameter(
                    name = param.variableDeclaratorId().text,
                    typeName = param.typeType().text,
                ),
            )
        }

        for (paramList in ctx.formalParameterList()) {
            for (param in paramList.formalParameter()) {
                params.add(
                    Parameter(
                        name = param.variableDeclaratorId().text,
                        typeName = param.typeType().text,
                    ),
                )
            }
        }

        return params
    }

    private fun visitArgNodes(methodCall: JavaParser.MethodCallContext): List<IRNode> =
        methodCall.arguments()?.expressionList()?.let { visitChildren(it) } ?: emptyList()

    private fun locationOf(ctx: ParserRuleContext): SourceLocation =
        SourceLocation(
            file = filePath,
            line = ctx.start.line,
            column = ctx.start.charPositionInLine + 1,
        )
}
