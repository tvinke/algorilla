package com.github.tvinke.algorilla.lang.java.parser

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

// Shared traversal scaffolding for the ANTLR-Java-grammar-based visitors (Java, Groovy).
// Kotlin goes through tree-sitter instead, so it never hangs off a JavaParser context and
// none of this applies to it. Constructor-vs-method detection, the var/initializer handling
// in local variable declarations, and control-flow-exit tracking stayed put in their own
// visitors on purpose: they looked similar in the CPD report but aren't actually the same
// logic.

/** Strips generic type parameters: "List<Order>" -> "List", "Map<String,Integer>" -> "Map". */
public fun String.simplifyGenericType(): String = substringBefore('<').substringAfterLast('.')

/** Extracts supertype names from extends/implements clauses, stripping generics. */
public fun extractSupertypes(ctx: JavaParser.ClassDeclarationContext): List<String> {
    val supertypes = mutableListOf<String>()
    ctx
        .typeType()
        ?.text
        ?.simplifyGenericType()
        ?.let { supertypes.add(it) }
    for (typeList in ctx.typeList()) {
        for (typeType in typeList.typeType()) {
            supertypes.add(typeType.text.simplifyGenericType())
        }
    }
    return supertypes
}

/** Extracts [Parameter]s from a method/constructor's formal parameter list. */
public fun extractParameters(ctx: JavaParser.FormalParametersContext?): List<Parameter> {
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

/** Visits a method call's argument expressions, falling back to a [GenericNode] for leaves that produce no IR. */
public inline fun visitArgNodes(
    methodCall: JavaParser.MethodCallContext,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val expressions = methodCall.arguments()?.expressionList()?.expression() ?: return emptyList()
    return expressions
        .map { expr ->
            val visited = visitor.visit(expr)
            visited.ifEmpty { listOf(GenericNode(expr.text, locationOf(expr), emptyList())) }
        }.flatten()
}

/**
 * Runs [body] with [params] added to [lambdaParams] for its duration, so nested visits can
 * tell a lambda parameter apart from an outer-scope variable of the same name.
 */
public inline fun <T> withLambdaParams(
    lambdaParams: MutableSet<String>,
    params: List<String>,
    body: () -> T,
): T {
    lambdaParams.addAll(params)
    val result = body()
    lambdaParams.removeAll(params.toSet())
    return result
}

/** Shared `if`/`else` handling: an else-less `if` just prepends its then-branch. */
public inline fun handleIfStatement(
    ctx: JavaParser.StatementContext,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val conditionNodes = ctx.expression(0)?.let { visitor.visit(it) } ?: emptyList()
    val thenBranch = ctx.statement(0)?.let { visitor.visitChildren(it) } ?: emptyList()
    val elseBranch = ctx.statement(1)?.let { visitor.visitChildren(it) }
    if (elseBranch != null) {
        return conditionNodes + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(ctx)))
    }
    return conditionNodes + thenBranch
}

/** Shared `while`/`do-while` handling: both just wrap the body in a [LoopKind.WHILE] node. */
public inline fun handleWhileStatement(
    ctx: JavaParser.StatementContext,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val body = ctx.statement(0)?.let { visitor.visitChildren(it) } ?: emptyList()
    return listOf(LoopNode(kind = LoopKind.WHILE, iteratedVariable = null, location = locationOf(ctx), children = body))
}

/**
 * Shared `for`/enhanced-`for` handling. [language] only affects how the iterated variable's
 * name is recovered from its source text (`extractVariableName` is language-aware for that).
 */
public inline fun handleForStatement(
    ctx: JavaParser.StatementContext,
    language: Language,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val enhancedFor = ctx.forControl()?.enhancedForControl()
    val body = ctx.statement(0)?.let { visitor.visitChildren(it) } ?: emptyList()
    if (enhancedFor == null) {
        return listOf(LoopNode(kind = LoopKind.FOR, iteratedVariable = null, location = locationOf(ctx), children = body))
    }
    val loopVarType = enhancedFor.typeType()?.text
    val loopVarName = enhancedFor.variableDeclaratorId()?.text
    val loopVarDecl =
        if (loopVarType != null && loopVarName != null) {
            listOf(VariableDecl(loopVarName, loopVarType, null, locationOf(ctx), emptyList()))
        } else {
            emptyList()
        }
    val iterVar = extractVariableName(enhancedFor.expression()?.text, language)
    return listOf(
        LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = iterVar, location = locationOf(ctx), children = loopVarDecl + body),
    )
}

/**
 * Shared constructor handling. Unlike [extractParameters] and friends, this is the whole
 * `visitConstructorDeclaration` body - Groovy and Java build the exact same [FunctionDecl]
 * shape here (no annotations, no initializer). Compare with `visitMethodDeclaration`, which
 * stayed separate per-language: Groovy has to guess constructor-vs-method by name casing
 * because its grammar doesn't distinguish them, and Java attaches annotations Groovy doesn't.
 */
public inline fun buildConstructorDecl(
    ctx: JavaParser.ConstructorDeclarationContext,
    enclosingClass: String?,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val name = ctx.identifier()?.text ?: "<init>"
    val params = extractParameters(ctx.formalParameters())
    val body = ctx.block()?.let { visitor.visitChildren(it) } ?: emptyList()
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

/** Shared `new Foo(...)` handling. */
public inline fun handleObjectCreation(
    ctx: JavaParser.ObjectCreationExpressionContext,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val creator = ctx.creator() ?: return visitor.visitChildren(ctx)
    val typeName = creator.createdName()?.text ?: return visitor.visitChildren(ctx)
    val loc = locationOf(ctx)
    val argNodes =
        creator
            .classCreatorRest()
            ?.arguments()
            ?.expressionList()
            ?.let { visitor.visitChildren(it) } ?: emptyList()
    return listOf(ObjectCreation(typeName = typeName, location = loc, children = argNodes))
}

/**
 * When a chained call's target is a lambda parameter (e.g. `x -> x.getName()` inside a
 * `.map(...)`), it's a plain method call on that parameter - not a collection operation on
 * whatever the lambda is closed over. Returns null when [targetVar] isn't a lambda param.
 */
public fun lambdaParamCallOrNull(
    methodName: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    lambdaParams: Set<String>,
    loc: SourceLocation,
): FunctionCall? {
    if (targetVar == null || targetVar !in lambdaParams) return null
    return FunctionCall(
        name = methodName,
        qualifiedTarget = targetVar,
        arguments = argNodes,
        location = loc,
        children = argNodes,
    )
}

/**
 * Chained stream ops (e.g. `.filter(a).filter(b)`) compose into a single pass. If the target
 * already produced a [LookupCall] for the same variable, [node] would double-count it - this
 * returns the de-duplicated result, or null when there's nothing to merge.
 */
public fun dedupedChainedLookupOrNull(
    node: IRNode,
    targetChildren: List<IRNode>,
    targetVar: String?,
    argNodes: List<IRNode>,
): List<IRNode>? {
    if (node !is LookupCall) return null
    val alreadyLookedUp = targetChildren.any { it is LookupCall && it.targetVariable == targetVar }
    if (!alreadyLookedUp) return null
    return targetChildren + argNodes
}
