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

@Suppress("CyclomaticComplexMethod")
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

    lookupKindFor(methodName)?.let { kind ->
        return LookupCall(kind = kind, targetVariable = targetVar, isO1 = isO1Type(targetText), location = loc, children = argNodes)
    }

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
    val cleaned =
        expr
            .replace(".stream()", "")
            .replace(".parallelStream()", "")
    val dotIndex = cleaned.lastIndexOf('.')
    return if (dotIndex >= 0) cleaned.substring(0, dotIndex) else cleaned
}

private val O1_INDICATORS =
    setOf(
        "Set",
        "Map",
        "HashMap",
        "HashSet",
        "TreeMap",
        "TreeSet",
        "LinkedHashMap",
        "LinkedHashSet",
    )

public fun isO1Type(targetText: String): Boolean = O1_INDICATORS.any { targetText.contains(it) }
