package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.FileRoot
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
        if (!file.exists()) {
            logger.warn { "File not found: $filePath" }
            return emptyFileRoot(filePath)
        }
        return try {
            val input = CharStreams.fromPath(file.toPath())
            val lexer = JavaLexer(input)
            val tokens = CommonTokenStream(lexer)
            val antlrParser = JavaParser(tokens)
            antlrParser.removeErrorListeners()

            val compilationUnit = antlrParser.compilationUnit()
            val children = JavaIRVisitor(filePath).visit(compilationUnit)

            FileRoot(
                filePath = filePath,
                language = Language.JAVA,
                location = SourceLocation(filePath, 1, 1),
                children = children,
            )
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn { "Failed to parse $filePath: ${e.message}" }
            emptyFileRoot(filePath)
        }
    }

    private fun emptyFileRoot(filePath: String) =
        FileRoot(
            filePath = filePath,
            language = language,
            location = SourceLocation(filePath, 1, 1),
            children = emptyList(),
        )

    public companion object {
        init {
            ParserRegistry.register(JavaLanguageParser())
        }
    }
}

/**
 * Visits the ANTLR parse tree and produces IR nodes.
 */
@Suppress("TooManyFunctions", "LargeClass")
internal class JavaIRVisitor(
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
        val loc = locationOf(ctx)
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                declaringClass = enclosingClass,
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
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                isConstructor = true,
                declaringClass = enclosingClass,
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
        if (ctx.IF() != null) {
            return handleIfStatement(ctx)
        }
        if (ctx.TRY() != null) {
            return handleTryStatement(ctx)
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

    override fun visitFieldDeclaration(ctx: JavaParser.FieldDeclarationContext): List<IRNode> {
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

    @Suppress("LongMethod")
    override fun visitLocalVariableDeclaration(ctx: JavaParser.LocalVariableDeclarationContext): List<IRNode> {
        val typeName = ctx.typeType()?.text
        val results = mutableListOf<IRNode>()

        // Java 10+ var: grammar uses `VAR identifier ASSIGN expression` (no variableDeclarators)
        if (ctx.VAR() != null && ctx.identifier() != null && ctx.expression() != null) {
            val varName = ctx.identifier().text
            val initChildren = visit(ctx.expression())
            results.add(
                VariableDecl(
                    name = varName,
                    typeName = null,
                    location = locationOf(ctx),
                    children = initChildren,
                ),
            )
            return results
        }

        // Standard path: `typeType variableDeclarators`
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

    private fun handleIfStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        val conditionNodes = ctx.expression(0)?.let { visit(it) } ?: emptyList()
        val thenBranch = ctx.statement(0)?.let { visitChildren(it) } ?: emptyList()
        val elseBranch = ctx.statement(1)?.let { visitChildren(it) }
        if (elseBranch != null) {
            return conditionNodes + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(ctx)))
        }
        return conditionNodes + thenBranch
    }

    private fun handleTryStatement(ctx: JavaParser.StatementContext): List<IRNode> = handleTryCatchStatement(ctx, this, ::locationOf)

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
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc)

        // Chained stream ops (e.g. .filter(a).filter(b)) compose into a single pass.
        // If the target already produced a LookupCall for the same variable, skip the duplicate.
        if (node is LookupCall && targetChildren.any { it is LookupCall && (it as LookupCall).targetVariable == targetVar }) {
            return targetChildren + argNodes
        }

        // Constant-size factory: Arrays.asList(a,b).contains(x) or List.of(a,b).stream().forEach(...)
        if (isConstantSizeFactory(targetChildren)) {
            if (node is LookupCall && !node.isO1) {
                return targetChildren + listOf(node.copy(isO1 = true))
            }
            if (node is LoopNode) {
                return targetChildren + argNodes
            }
        }
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

    private fun visitArgNodes(methodCall: JavaParser.MethodCallContext): List<IRNode> {
        val expressions = methodCall.arguments()?.expressionList()?.expression() ?: return emptyList()
        return expressions
            .map { expr ->
                val visited = visit(expr)
                visited.ifEmpty { listOf(GenericNode(expr.text, locationOf(expr), emptyList())) }
            }.flatten()
    }

    private fun locationOf(ctx: ParserRuleContext): SourceLocation =
        SourceLocation(
            file = filePath,
            line = ctx.start.line,
            column = ctx.start.charPositionInLine + 1,
        )
}

private val JAVA_CONSTANT_SIZE_FACTORIES =
    setOf(
        "asList",
        "of",
        "singletonList",
        "singletonMap",
        "singleton",
        "emptyList",
        "emptySet",
        "emptyMap",
        "nCopies",
    )

private const val SMALL_COLLECTION_THRESHOLD = 8

/**
 * Returns true when the target of a chained call is a known constant-size factory
 * like `Arrays.asList(a, b)`, `List.of(a, b)`, or `Collections.emptyList()`.
 */
private fun isConstantSizeFactory(targetChildren: List<IRNode>): Boolean {
    val call = targetChildren.filterIsInstance<FunctionCall>().firstOrNull() ?: return false
    return call.name in JAVA_CONSTANT_SIZE_FACTORIES && call.arguments.size <= SMALL_COLLECTION_THRESHOLD
}
