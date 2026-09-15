package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.engine.LanguageParser
import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.engine.parseFileOrEmpty
import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.ControlFlowExit
import com.github.tvinke.algorilla.model.ExitKind
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.TypeCheck
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.SmallFactoryCollectionSemantics
import io.github.oshai.kotlinlogging.KotlinLogging
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext

private val logger = KotlinLogging.logger {}

/**
 * Parses Java source files into the unified IR representation using ANTLR.
 */
public class JavaLanguageParser : LanguageParser {
    override val language: Language = Language.JAVA

    override fun canParse(filePath: String): Boolean = filePath.endsWith(".java")

    override fun parse(filePath: String): FileRoot =
        parseFileOrEmpty(filePath, language, logger) { file ->
            val input = CharStreams.fromPath(file.toPath())
            val lexer = JavaLexer(input)
            val tokens = CommonTokenStream(lexer)
            val antlrParser = JavaParser(tokens)
            antlrParser.removeErrorListeners()

            val compilationUnit = antlrParser.compilationUnit()
            JavaIRVisitor(filePath).visit(compilationUnit)
        }

    public companion object {
        init {
            ParserRegistry.register(JavaLanguageParser())
        }
    }
}

/**
 * Visits the ANTLR parse tree and produces IR nodes.
 */
@Suppress("TooManyFunctions", "LargeClass") // Visitor pattern: one visit method per AST node type
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
        val className = ctx.identifier()?.text
        enclosingClass = className
        val result = visitChildren(ctx)
        enclosingClass = prev
        if (className == null) return result
        val supertypes = extractSupertypes(ctx)
        val annotations = extractClassAnnotations(ctx)
        return listOf(
            ClassNode(
                name = className,
                supertypes = supertypes,
                annotations = annotations,
                location = locationOf(ctx),
                children = result,
            ),
        )
    }

    override fun visitInstanceOfOperatorExpression(ctx: JavaParser.InstanceOfOperatorExpressionContext): List<IRNode> {
        val expr = ctx.expression() ?: return visitChildren(ctx)
        val varName = extractVariableName(expr.text, Language.JAVA) ?: return visitChildren(ctx)
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
        val loc = locationOf(ctx)
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name
        val returnType =
            ctx
                .typeTypeOrVoid()
                ?.typeType()
                ?.text
                ?.simplifyGenericType()
        val annotations = extractMethodAnnotations(ctx)

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                declaringClass = enclosingClass,
                returnType = returnType,
                annotations = annotations,
                location = loc,
                children = body,
            ),
        )
    }

    override fun visitConstructorDeclaration(ctx: JavaParser.ConstructorDeclarationContext): List<IRNode> =
        buildConstructorDecl(ctx, enclosingClass, this, ::locationOf)

    @Suppress("ReturnCount") // One guard clause per statement type — clearer than a when/map
    override fun visitStatement(ctx: JavaParser.StatementContext): List<IRNode> {
        if (ctx.FOR() != null) {
            return handleForStatement(ctx, Language.JAVA, this, ::locationOf)
        }
        if (ctx.WHILE() != null || ctx.DO() != null) {
            return handleWhileStatement(ctx, this, ::locationOf)
        }
        if (ctx.IF() != null) {
            return handleIfStatement(ctx, this, ::locationOf)
        }
        if (ctx.TRY() != null) {
            return handleTryStatement(ctx)
        }
        if (ctx.THROW() != null) {
            return visitChildren(ctx) + listOf(ControlFlowExit(ExitKind.THROW, locationOf(ctx)))
        }
        if (ctx.BREAK() != null) {
            return listOf(ControlFlowExit(ExitKind.BREAK, locationOf(ctx)))
        }
        if (ctx.RETURN() != null) {
            return visitChildren(ctx) + listOf(ControlFlowExit(ExitKind.RETURN, locationOf(ctx)))
        }
        if (ctx.CONTINUE() != null) {
            return listOf(ControlFlowExit(ExitKind.CONTINUE, locationOf(ctx)))
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
        val argNodes = visitArgNodes(methodCall, this, ::locationOf)

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

    override fun visitTernaryExpression(ctx: JavaParser.TernaryExpressionContext): List<IRNode> {
        val expressions = ctx.expression()
        if (expressions.size == TERNARY_CHILD_COUNT) {
            val conditionNodes = visit(expressions[0])
            val thenBranch = visit(expressions[1])
            val elseBranch = visit(expressions[2])
            return conditionNodes + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(ctx)))
        }
        return visitChildren(ctx)
    }

    override fun visitObjectCreationExpression(ctx: JavaParser.ObjectCreationExpressionContext): List<IRNode> =
        handleObjectCreation(ctx, this, ::locationOf)

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
            val initExpr = initChildren.firstOrNull { it is FunctionCall } as? FunctionCall
            results.add(
                VariableDecl(
                    name = varName,
                    typeName = null,
                    initializer = initExpr,
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
            val initExpr = initChildren.firstOrNull { it is FunctionCall } as? FunctionCall
            results.add(
                VariableDecl(
                    name = varName,
                    typeName = typeName,
                    initializer = initExpr,
                    location = locationOf(ctx),
                    children = initChildren,
                ),
            )
        }
        return results
    }

    private fun handleTryStatement(ctx: JavaParser.StatementContext): List<IRNode> = handleTryCatchStatement(ctx, this, ::locationOf)

    private fun handleChainedMethodCall(
        methodName: String,
        targetText: String,
        methodCall: JavaParser.MethodCallContext,
        targetExpr: JavaParser.ExpressionContext,
        loc: SourceLocation,
    ): List<IRNode> {
        val targetVar = extractVariableName(targetText, Language.JAVA)
        val argNodes = visitArgNodes(methodCall, this, ::locationOf)
        val targetChildren = visit(targetExpr)
        lambdaParamCallOrNull(methodName, targetVar, argNodes, lambdaParams, loc)?.let { return targetChildren + listOf(it) }
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc)

        dedupedChainedLookupOrNull(node, targetChildren, targetVar, argNodes)?.let { return it }

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

    /** Extracts simple annotation names from classBodyDeclaration modifiers (method-level). */
    private fun extractMethodAnnotations(ctx: JavaParser.MethodDeclarationContext): List<String> {
        val classBodyDecl = ctx.parent?.parent as? JavaParser.ClassBodyDeclarationContext ?: return emptyList()
        return classBodyDecl.modifier().mapNotNull { mod ->
            mod
                .classOrInterfaceModifier()
                ?.annotation()
                ?.qualifiedName()
                ?.text
                ?.substringAfterLast('.')
        }
    }

    /** Extracts simple annotation names from typeDeclaration modifiers (class-level). */
    private fun extractClassAnnotations(ctx: JavaParser.ClassDeclarationContext): List<String> {
        val typeDecl = ctx.parent as? JavaParser.TypeDeclarationContext ?: return emptyList()
        return typeDecl.classOrInterfaceModifier().mapNotNull { mod ->
            mod
                .annotation()
                ?.qualifiedName()
                ?.text
                ?.substringAfterLast('.')
        }
    }

    private fun locationOf(ctx: ParserRuleContext): SourceLocation =
        SourceLocation(
            file = filePath,
            line = ctx.start.line,
            column = ctx.start.charPositionInLine + 1,
        )
}

/** Ternary expressions have exactly 3 sub-expressions: condition, then, else. */
private const val TERNARY_CHILD_COUNT = 3

/**
 * Returns true when the target of a chained call is a known constant-size factory
 * like `Arrays.asList(a, b)`, `List.of(a, b)`, or `Collections.emptyList()`.
 */
private fun isConstantSizeFactory(targetChildren: List<IRNode>): Boolean {
    val call = targetChildren.filterIsInstance<FunctionCall>().firstOrNull() ?: return false
    return SmallFactoryCollectionSemantics.isConstantSizeFactory(call)
}
