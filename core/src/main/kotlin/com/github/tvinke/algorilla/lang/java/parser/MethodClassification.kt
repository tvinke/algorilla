package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry

@Suppress("CyclomaticComplexMethod", "ReturnCount")
public fun classifyChainedCall(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
    language: Language = Language.JAVA,
): IRNode {
    classifyAsIteration(methodName, targetText, targetVar, argNodes, loc)?.let { return it }

    classifyAsLookup(methodName, targetText, targetVar, argNodes, loc, language)?.let { return it }

    sortKindFor(methodName, language)?.let { kind ->
        return SortCall(
            kind = kind,
            hasComparator = argNodes.isNotEmpty(),
            comparatorBody = argNodes.ifEmpty { null },
            location = loc,
            children = argNodes,
        )
    }

    accessKindFor(methodName, language)?.let { kind ->
        // first/last/firstOrNull/lastOrNull with a predicate lambda is a linear scan (lookup),
        // not a positional access
        if (argNodes.isNotEmpty() && kind in setOf(AccessKind.FIRST, AccessKind.LAST)) {
            return LookupCall(kind = LookupKind.FIND, targetVariable = targetVar, isO1 = false, location = loc, children = argNodes)
        }
        return CollectionAccess(kind = kind, location = loc, children = argNodes)
    }

    // .get(0) on a collection → CollectionAccess(INDEX_ZERO), used by sort-for-last detection
    if (methodName == "get" && isLiteralZeroArg(argNodes)) {
        return CollectionAccess(kind = AccessKind.INDEX_ZERO, location = loc, children = argNodes)
    }

    return FunctionCall(
        name = methodName,
        qualifiedTarget = targetVar,
        arguments = argNodes,
        location = loc,
        children = argNodes,
    )
}

private fun classifyAsIteration(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
): IRNode? =
    when (methodName) {
        "forEach" -> {
            val kind =
                when {
                    targetText.contains(".parallelStream()") -> LoopKind.PARALLEL_STREAM_FOR_EACH
                    targetText.contains(".stream()") -> LoopKind.STREAM_FOR_EACH
                    else -> LoopKind.HIGHER_ORDER
                }
            LoopNode(kind = kind, iteratedVariable = targetVar, location = loc, children = argNodes)
        }
        "removeIf" -> LoopNode(kind = LoopKind.HIGHER_ORDER, iteratedVariable = targetVar, location = loc, children = argNodes)
        else -> null
    }

private fun classifyAsLookup(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
    language: Language = Language.JAVA,
): IRNode? {
    val kind = lookupKindFor(methodName, language) ?: return null
    if (kind.isStringApplicable() && isStringTarget(targetText)) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    // find() with 0 args is Matcher.find(), not a collection find; with 2+ args is Map.find()
    if (kind == LookupKind.FIND && argNodes.size != 1) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    val o1 = isO1Type(targetText) || isImplicitlyO1(methodName)
    return LookupCall(kind = kind, targetVariable = targetVar, isO1 = o1, location = loc, children = argNodes)
}

public fun classifyStandaloneCall(
    name: String,
    loc: SourceLocation,
    argNodes: List<IRNode>,
    language: Language = Language.JAVA,
): List<IRNode> {
    val lookupKind = lookupKindFor(name, language)
    if (lookupKind != null) {
        return listOf(
            LookupCall(kind = lookupKind, targetVariable = null, isO1 = false, location = loc, children = argNodes),
        )
    }
    return listOf(
        FunctionCall(name = name, qualifiedTarget = null, arguments = argNodes, location = loc, children = argNodes),
    )
}

public fun lookupKindFor(
    methodName: String,
    language: Language = Language.JAVA,
): LookupKind? =
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
        else -> registryInstance.lookupKindFor(methodName, language)
    }

public fun sortKindFor(
    methodName: String,
    language: Language = Language.JAVA,
): SortKind? =
    when (methodName) {
        "sort" -> SortKind.SORT
        "sorted" -> SortKind.SORTED
        "sortBy", "sortedBy" -> SortKind.SORT_BY
        "orderBy" -> SortKind.ORDER_BY
        else -> registryInstance.sortKindFor(methodName, language)
    }

public fun accessKindFor(
    methodName: String,
    language: Language = Language.JAVA,
): AccessKind? =
    when (methodName) {
        "first" -> AccessKind.FIRST
        "last" -> AccessKind.LAST
        "findFirst" -> AccessKind.FIND_FIRST
        "findAny" -> AccessKind.FIND_ANY
        else -> registryInstance.accessKindFor(methodName, language)
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
    LanguageSemanticsRegistry.loadDefaults().allStreamOps()
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
    setOf(
        "toString()",
        "substring(",
        "toLowerCase()",
        "toUpperCase()",
        "trim()",
        "replace(",
        "replaceAll(",
        "strip(",
        "split(",
        "concat(",
        "charAt(",
        "startsWith(",
        "endsWith(",
        ".append(",
    )

private val STRING_NAME_SUFFIXES =
    setOf(
        "Name",
        "name",
        "Text",
        "text",
        "Str",
        "String",
        "string",
        "Line",
        "line",
        "Path",
        "path",
        "Url",
        "url",
        "Key",
        "key",
        "Value",
        "value",
        "Id",
        "id",
        "Message",
        "message",
        "Description",
        "description",
        "Label",
        "label",
        "Title",
        "title",
        "Field",
        "field",
        "Signature",
        "signature",
        "Property",
        "property",
        "Qualifier",
        "qualifier",
        "Separator",
        "separator",
        "Delimiter",
        "delimiter",
        "Prefix",
        "prefix",
        "Suffix",
        "suffix",
        "Pattern",
        "pattern",
        "Expression",
        "expression",
        "Descriptor",
        "descriptor",
        "Content",
        "content",
        "Location",
        "location",
        "Source",
        "source",
        "Token",
        "token",
        "Spec",
        "spec",
        "Tag",
        "tag",
    )

/** Common short variable names that are almost always strings or StringBuilders. */
private val STRING_EXACT_NAMES =
    setOf("s", "sb", "str", "desc", "buf", "buffer", "input", "output", "line", "word", "query", "sql")

/**
 * Heuristic: returns true when the target expression is likely a String rather than a List.
 * This avoids treating `someString.indexOf(...)` as a collection lookup.
 */
public fun isStringTarget(targetText: String): Boolean =
    STRING_METHOD_INDICATORS.any { targetText.contains(it) } ||
        STRING_NAME_SUFFIXES.any { targetText.endsWith(it) } ||
        extractVariableName(targetText) in STRING_EXACT_NAMES

/** Returns true when the argument list is a single literal `0` (for `.get(0)` detection). */
private fun isLiteralZeroArg(argNodes: List<IRNode>): Boolean {
    if (argNodes.size != 1) return false
    val arg = argNodes[0]
    return arg is GenericNode && arg.nodeType.trim() == "0"
}

/** Lazily loaded registry instance for parser-time queries. */
private val registryInstance: LanguageSemanticsRegistry by lazy {
    LanguageSemanticsRegistry.loadDefaults()
}
