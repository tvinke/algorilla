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

// Exhaustive method-name dispatch — each branch is a distinct IR classification
@Suppress("CyclomaticComplexMethod", "ReturnCount", "LongMethod")
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
            qualifiedTarget = targetVar,
        )
    }

    accessKindFor(methodName, language)?.let { kind ->
        // first/last/firstOrNull/lastOrNull with a predicate lambda is a linear scan (lookup),
        // not a positional access
        if (argNodes.isNotEmpty() && kind in setOf(AccessKind.FIRST, AccessKind.LAST)) {
            return LookupCall(kind = LookupKind.FIND, targetVariable = targetVar, isO1 = false, location = loc, children = argNodes)
        }
        return CollectionAccess(kind = kind, location = loc, children = argNodes, qualifiedTarget = targetVar)
    }

    // .get(0) on a collection → CollectionAccess(INDEX_ZERO), used by sort-for-last detection
    if (methodName == "get" && isLiteralZeroArg(argNodes)) {
        return CollectionAccess(kind = AccessKind.INDEX_ZERO, location = loc, children = argNodes, qualifiedTarget = targetVar)
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
): IRNode? {
    if (registryInstance.isMonadicTarget(targetText)) return null
    return when (methodName) {
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
    if (kind.isStringApplicable() && isStringTarget(targetText, language)) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    // find() with 0 args is Matcher.find(), not a collection find; with 2+ args is Map.find()
    if (kind == LookupKind.FIND && argNodes.size != 1) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    val o1 = isO1Type(targetText) || isImplicitlyO1(methodName, language)
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

// Intentional §2 exception: these universal mappings (contains→CONTAINS etc.) are identical
// across all languages and won't change. Keeping them as a when gives compiler-optimized
// dispatch on the hot parse path. Language-specific methods fall through to the registry.
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

public fun extractVariableName(
    expr: String?,
    language: Language = Language.JAVA,
): String? {
    if (expr == null) return null
    var cleaned = expr
    // Strip static utility wrappers like Arrays.stream(...), Collections.unmodifiableList(...)
    cleaned = unwrapStaticCall(cleaned) ?: cleaned
    // Strip stream entry points
    cleaned = cleaned.replace(".stream()", "").replace(".parallelStream()", "")
    // Strip collection-view accessors (values/keySet/entrySet) — these don't carry
    // meaningful variable identity (map.values() → map). But preserve Enum.values()
    // for the LoopBoundAnnotator to detect constant-bound enum iteration.
    cleaned = stripCollectionViewAccessors(cleaned, language)
    // Strip chained stream intermediate operations (e.g. .map(...), .flatMap(...), .filter(...))
    cleaned = stripStreamChainOps(cleaned, language)
    // Return the full remaining expression — don't strip the last dotted segment,
    // as it may be a meaningful field access (e.g. "resource.currentLocationIds")
    return cleaned.ifBlank { null }
}

/**
 * Strips collection-view accessors (e.g. `.values()`, `.keySet()`, `.entrySet()`)
 * from the expression to recover the underlying variable name (`map.values()` → `map`).
 *
 * Accessor names are YAML-driven via the `collection-view-accessors` section.
 *
 * Preserves `Type.values()` where the prefix starts with an uppercase letter,
 * as this indicates enum iteration (e.g. `Status.values()` stays intact for
 * [LoopBoundAnnotator] to detect constant-bound loops).
 */
private fun stripCollectionViewAccessors(
    input: String,
    language: Language,
): String {
    val accessors = LanguageSemanticsRegistry.DEFAULT.extraSection(language, "collection-view-accessors")
    if (accessors.isEmpty()) return input
    var result = input
    for (accessor in accessors) {
        val suffix = ".$accessor()"
        if (result.endsWith(suffix)) {
            val prefix = result.substringBefore(suffix)
            // Preserve Enum.values() — uppercase prefix indicates enum type
            if (accessor == "values" && prefix.isNotEmpty() && prefix[0].isUpperCase()) continue
            result = prefix
        }
    }
    return result
}

/**
 * Strips chained stream operations with balanced parentheses from the expression.
 * Handles arbitrary nesting like `.map(x -> foo(bar(x)))`.
 *
 * Uses language-specific stream ops so that e.g. `.values()` is only stripped for
 * JavaScript (where it's a stream/iteration op) but preserved for Java/Kotlin/Groovy
 * (where it's enum iteration).
 */
private fun stripStreamChainOps(
    input: String,
    language: Language,
): String {
    val ops = LanguageSemanticsRegistry.DEFAULT.streamOps(language)
    var result = input
    var changed = true
    while (changed) {
        changed = false
        for (op in ops) {
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

private val STATIC_WRAPPERS = setOf("Arrays", "Collections", "Stream", "Optional", "List", "Set", "Map")

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
public fun isImplicitlyO1(
    methodName: String,
    language: Language = Language.JAVA,
): Boolean {
    val registry = registryInstance
    return methodName in registry.implicitlyO1Methods(language)
}

/**
 * Heuristic: returns true when the target expression is likely a String rather than a List.
 * This avoids treating `someString.indexOf(...)` as a collection lookup.
 * All name sets are YAML-driven — see string-indicators, string-name-suffixes,
 * and string-exact-names sections in the language YAML files.
 */
public fun isStringTarget(
    targetText: String,
    language: Language = Language.JAVA,
): Boolean {
    val registry = registryInstance
    return registry.stringIndicators(language).any { targetText.contains(it) } ||
        registry.stringNameSuffixes(language).any { suffix ->
            val getterSuffix = suffix.replaceFirstChar { it.uppercaseChar() }
            targetText.contains(".get$getterSuffix(") ||
                targetText.contains(".$suffix(") // record accessor: .version(, .name(
        } ||
        registry.stringNameSuffixes(language).any { targetText.endsWith(it) } ||
        extractVariableName(targetText, language) in registry.stringExactNames(language)
}

/** Returns true when the argument list is a single literal `0` (for `.get(0)` detection). */
private fun isLiteralZeroArg(argNodes: List<IRNode>): Boolean {
    if (argNodes.size != 1) return false
    val arg = argNodes[0]
    return arg is GenericNode && arg.nodeType.trim() == "0"
}

/** Lazily loaded registry instance for parser-time queries. */
private val registryInstance: LanguageSemanticsRegistry by lazy {
    LanguageSemanticsRegistry.DEFAULT
}
