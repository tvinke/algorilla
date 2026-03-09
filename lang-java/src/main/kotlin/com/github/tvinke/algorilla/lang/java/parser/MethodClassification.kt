package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.semantics.CollectionSemanticsRegistry

@Suppress("CyclomaticComplexMethod", "ReturnCount")
public fun classifyChainedCall(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
): IRNode {
    if (methodName == "forEach") {
        val kind = if (targetText.contains(".stream()")) LoopKind.STREAM_FOR_EACH else LoopKind.HIGHER_ORDER
        return LoopNode(kind = kind, iteratedVariable = targetVar, location = loc, children = argNodes)
    }

    classifyAsLookup(methodName, targetText, targetVar, argNodes, loc)?.let { return it }

    sortKindFor(methodName)?.let { kind ->
        return SortCall(
            kind = kind,
            hasComparator = argNodes.isNotEmpty(),
            comparatorBody = argNodes.ifEmpty { null },
            location = loc,
            children = argNodes,
        )
    }

    accessKindFor(methodName)?.let { kind ->
        return CollectionAccess(kind = kind, location = loc, children = argNodes)
    }

    return FunctionCall(
        name = methodName,
        qualifiedTarget = targetVar,
        arguments = argNodes,
        location = loc,
        children = argNodes,
    )
}

private fun classifyAsLookup(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
): IRNode? {
    val kind = lookupKindFor(methodName) ?: return null
    if (kind == LookupKind.INDEX_OF && isStringTarget(targetText)) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    if (kind == LookupKind.FIND && argNodes.size >= 2) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    val o1 = isO1Type(targetText) || isImplicitlyO1(methodName)
    return LookupCall(kind = kind, targetVariable = targetVar, isO1 = o1, location = loc, children = argNodes)
}

public fun classifyStandaloneCall(
    name: String,
    loc: SourceLocation,
    argNodes: List<IRNode>,
): List<IRNode> {
    val lookupKind = lookupKindFor(name)
    if (lookupKind != null) {
        return listOf(
            LookupCall(kind = lookupKind, targetVariable = null, isO1 = false, location = loc, children = argNodes),
        )
    }
    return listOf(
        FunctionCall(name = name, qualifiedTarget = null, arguments = argNodes, location = loc, children = argNodes),
    )
}

public fun lookupKindFor(methodName: String): LookupKind? =
    when (methodName) {
        "contains" -> LookupKind.CONTAINS
        "containsKey", "containsValue" -> LookupKind.CONTAINS
        "indexOf", "lastIndexOf" -> LookupKind.INDEX_OF
        "find" -> LookupKind.FIND
        "filter" -> LookupKind.FILTER
        "anyMatch" -> LookupKind.ANY_MATCH
        "allMatch" -> LookupKind.ALL_MATCH
        "noneMatch" -> LookupKind.NONE_MATCH
        "some" -> LookupKind.SOME
        "includes" -> LookupKind.INCLUDES
        else -> null
    }

public fun sortKindFor(methodName: String): SortKind? =
    when (methodName) {
        "sort" -> SortKind.SORT
        "sorted" -> SortKind.SORTED
        "sortBy", "sortedBy" -> SortKind.SORT_BY
        "orderBy" -> SortKind.ORDER_BY
        else -> null
    }

public fun accessKindFor(methodName: String): AccessKind? =
    when (methodName) {
        "first" -> AccessKind.FIRST
        "last" -> AccessKind.LAST
        "findFirst" -> AccessKind.FIND_FIRST
        "findAny" -> AccessKind.FIND_ANY
        else -> null
    }

public fun extractVariableName(expr: String?): String? {
    if (expr == null) return null
    var cleaned = expr
    // Strip static utility wrappers like Arrays.stream(...), Collections.unmodifiableList(...)
    cleaned = unwrapStaticCall(cleaned) ?: cleaned
    // Strip stream entry points
    cleaned = cleaned.replace(".stream()", "").replace(".parallelStream()", "")
    // Strip chained stream intermediate operations (e.g. .map(...), .flatMap(...), .filter(...))
    cleaned = stripStreamChainOps(cleaned)
    // Return the full remaining expression — don't strip the last dotted segment,
    // as it may be a meaningful field access (e.g. "resource.currentLocationIds")
    return cleaned.ifBlank { null }
}

/**
 * Set of method names that should be stripped from variable name expressions.
 * Derived from the semantics registry (YAML) — all classified methods and stream pipeline ops.
 * This is the single source of truth; to add a new method, update the YAML files.
 */
private val STREAM_CHAIN_OPS: Set<String> by lazy {
    CollectionSemanticsRegistry.loadDefaults().allStreamOps()
}

/**
 * Strips chained stream operations with balanced parentheses from the expression.
 * Handles arbitrary nesting like `.map(x -> foo(bar(x)))`.
 */
private fun stripStreamChainOps(input: String): String {
    var result = input
    var changed = true
    while (changed) {
        changed = false
        for (op in STREAM_CHAIN_OPS) {
            val stripped = stripSingleOp(result, op)
            if (stripped != null) {
                result = stripped
                changed = true
            }
        }
    }
    return result
}

private fun stripSingleOp(
    text: String,
    op: String,
): String? {
    val prefix = ".$op("
    val idx = text.indexOf(prefix)
    if (idx < 0) return null
    val end = findBalancedClose(text, idx + prefix.length - 1)
    if (end < 0) return null
    return text.substring(0, idx) + text.substring(end + 1)
}

/** Finds the index of the closing paren that balances the opening paren at [openIndex]. */
private fun findBalancedClose(
    text: String,
    openIndex: Int,
): Int {
    var depth = 0
    for (i in openIndex until text.length) {
        when (text[i]) {
            '(' -> depth++
            ')' -> {
                depth--
                if (depth == 0) return i
            }
        }
    }
    return -1
}

private val STATIC_WRAPPERS = setOf("Arrays", "Collections", "Stream", "Optional")

/**
 * Unwraps static utility calls like `Arrays.stream(x)` → `x`.
 * Returns null if the expression doesn't match.
 */
private fun unwrapStaticCall(expr: String): String? {
    val dotIdx = expr.indexOf('.')
    if (dotIdx < 0) return null
    val className = expr.substring(0, dotIdx)
    if (className !in STATIC_WRAPPERS) return null
    val parenStart = expr.indexOf('(', dotIdx)
    if (parenStart < 0) return null
    val parenEnd = findBalancedClose(expr, parenStart)
    if (parenEnd < 0) return null
    return expr.substring(parenStart + 1, parenEnd)
}

/**
 * O(1) type detection — delegates to the semantics registry.
 */
public fun isO1Type(targetText: String): Boolean {
    val registry = registryInstance
    return registry.isO1Type(targetText)
}

/**
 * Methods that are only defined on O(1) types (e.g. containsKey/containsValue are Map-only).
 * Delegates to the semantics registry.
 */
public fun isImplicitlyO1(methodName: String): Boolean {
    val registry = registryInstance
    return methodName in registry.allImplicitlyO1Methods()
}

private val STRING_METHOD_INDICATORS =
    setOf("toString()", "substring(", "toLowerCase()", "toUpperCase()", "trim()", "replace(", "replaceAll(", "strip()")

/**
 * Heuristic: returns true when the target expression is likely a String rather than a List.
 * This avoids treating `someString.indexOf(...)` as a collection lookup.
 */
public fun isStringTarget(targetText: String): Boolean =
    STRING_METHOD_INDICATORS.any { targetText.contains(it) } ||
        targetText.endsWith("Name") ||
        targetText.endsWith("name") ||
        targetText.endsWith("Text") ||
        targetText.endsWith("text") ||
        targetText.endsWith("Str") ||
        targetText.endsWith("String") ||
        targetText.endsWith("string") ||
        targetText.endsWith("Line") ||
        targetText.endsWith("line") ||
        targetText.endsWith("Path") ||
        targetText.endsWith("path") ||
        targetText.endsWith("Url") ||
        targetText.endsWith("url")

/** Lazily loaded registry instance for parser-time queries. */
private val registryInstance: CollectionSemanticsRegistry by lazy {
    CollectionSemanticsRegistry.loadDefaults()
}
