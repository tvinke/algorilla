package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.model.CardinalityBucket
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.GroupType
import com.github.tvinke.algorilla.model.GroupVisibility
import com.github.tvinke.algorilla.model.IssueGroup
import com.github.tvinke.algorilla.model.PathContext
import com.github.tvinke.algorilla.rules.Finding

/**
 * Groups related findings into [IssueGroup]s using anchor-based grouping.
 *
 * Each method with ≥2 findings becomes an anchor. Findings in methods directly connected
 * (via call edges) are assigned to the nearest anchor. Anchors never merge transitively.
 */
internal fun groupFindings(
    findings: List<Finding>,
    irTrees: Map<String, FileRoot>,
    callGraph: CallGraph,
    cachedMethodHints: Map<String, String?> = emptyMap(),
): List<IssueGroup> {
    if (findings.isEmpty()) return emptyList()

    val methodRangeIndex = buildMethodRangeIndex(irTrees)
    val findingMethods =
        findings.map { finding ->
            val rangesInFile = methodRangeIndex[finding.location.file] ?: emptyList()
            findEnclosingMethod(rangesInFile, finding.location.line)
                ?: cachedMethodHints["${finding.location.file}:${finding.location.line}"]
        }
    val methodAdj = buildMethodAdjacency(callGraph)
    val methodToIndices = buildMethodIndex(findingMethods)
    val anchors = determineAnchors(methodToIndices, methodAdj)
    val methodToAnchor = assignMethodsToAnchors(methodToIndices, anchors, methodAdj)
    val anchorGroups = buildAnchorGroups(findingMethods, methodToAnchor, findings)

    return anchorGroups
        .map { (anchor, indices) ->
            buildIssueGroup(anchor, indices, findings, findingMethods, callGraph)
        }.sortedWith(GROUP_ORDER)
}

private fun buildMethodIndex(findingMethods: List<String?>): Map<String, List<Int>> {
    val index = mutableMapOf<String, MutableList<Int>>()
    for ((idx, method) in findingMethods.withIndex()) {
        if (method != null) index.getOrPut(method) { mutableListOf() }.add(idx)
    }
    return index
}

private fun determineAnchors(
    methodToIndices: Map<String, List<Int>>,
    methodAdj: Map<String, Set<String>>,
): MutableSet<String> {
    val anchors = methodToIndices.keys.filter { (methodToIndices[it]?.size ?: 0) >= 2 }.toMutableSet()
    for (method in methodToIndices.keys) {
        if (method !in anchors && (methodAdj[method] ?: emptySet()).none { it in anchors }) {
            anchors.add(method)
        }
    }
    return anchors
}

private fun assignMethodsToAnchors(
    methodToIndices: Map<String, List<Int>>,
    anchors: MutableSet<String>,
    methodAdj: Map<String, Set<String>>,
): Map<String, String> {
    val result = mutableMapOf<String, String>()
    for (method in methodToIndices.keys) {
        if (method in anchors) {
            result[method] = method
        } else {
            val connected = (methodAdj[method] ?: emptySet()).filter { it in anchors }
            if (connected.isNotEmpty()) {
                result[method] = connected.maxBy { methodToIndices[it]?.size ?: 0 }
            } else {
                result[method] = method
                anchors.add(method)
            }
        }
    }
    return result
}

private fun buildAnchorGroups(
    findingMethods: List<String?>,
    methodToAnchor: Map<String, String>,
    findings: List<Finding>,
): Map<String, List<Int>> {
    val groups = mutableMapOf<String, MutableList<Int>>()
    for ((idx, method) in findingMethods.withIndex()) {
        // methodToAnchor is total over every method key assignMethodsToAnchors saw - which is
        // every non-null entry in findingMethods, by construction - so a missing key here means
        // that invariant broke, not a legitimate "no anchor yet" case worth falling back on.
        val anchor =
            when {
                method != null -> methodToAnchor.getValue(method)
                else -> classAnchorFromPath(findings[idx].location.file)
            }
        groups.getOrPut(anchor) { mutableListOf() }.add(idx)
    }
    return groups
}

/** Extracts a class-level anchor from a file path, e.g. "/src/.../OrderService.java" → "OrderService". */
private fun classAnchorFromPath(filePath: String): String {
    val fileName = filePath.substringAfterLast('/')
    return fileName.substringBeforeLast('.').ifEmpty { fileName }
}

private fun buildIssueGroup(
    anchor: String,
    indices: List<Int>,
    findings: List<Finding>,
    findingMethods: List<String?>,
    callGraph: CallGraph,
): IssueGroup {
    val groupFindings = indices.map { findings[it] }
    val methods = indices.mapNotNull { findingMethods[it] }.toSet()
    val groupType = classifyGroupType(methods, callGraph)
    val visibility = visibilityFor(groupType)
    val representative = selectRepresentative(groupFindings)
    val pathContexts = groupFindings.mapNotNull { it.pathContext }.toSet()
    val maxCardinality = groupFindings.mapNotNull { it.cardinalityBucket }.maxByOrNull { cardinalityRank(it) }

    return IssueGroup(
        id = "$anchor:${groupType.name.lowercase()}",
        anchor = anchor,
        groupType = groupType,
        visibility = visibility,
        pathContext = if (pathContexts.size == 1) pathContexts.first() else null,
        maxCardinality = maxCardinality,
        representativeFinding = representative,
        contributingFindings = groupFindings,
    )
}

private fun hasCallEdgeBetween(
    methods: Set<String>,
    callGraph: CallGraph,
): Boolean = methods.any { m -> callGraph.callees(m).any { it in methods } || callGraph.callers(m).any { it in methods } }

private fun classifyGroupType(
    methods: Set<String>,
    callGraph: CallGraph,
): GroupType =
    when {
        methods.size <= 1 -> GroupType.SAME_METHOD
        hasCallEdgeBetween(methods, callGraph) -> GroupType.SAFE_CALL_EDGE
        else -> GroupType.SHARED_EVIDENCE_CHAIN
    }

/**
 * Derived from [GroupType] rather than re-running [hasCallEdgeBetween] - the two questions
 * ("how are these findings related" and "how reliably was that established") reduce to the
 * same call-edge check, one-to-one with the group type that already answered it.
 */
private fun visibilityFor(groupType: GroupType): GroupVisibility =
    when (groupType) {
        GroupType.SAME_METHOD -> GroupVisibility.UNKNOWN
        GroupType.SAFE_CALL_EDGE -> GroupVisibility.RESOLVED_SAFE
        GroupType.SHARED_EVIDENCE_CHAIN -> GroupVisibility.AMBIGUOUS
    }

/**
 * Selects the most representative finding: highest severity, then confidence,
 * then worst cardinality (LIKELY_LARGE first), then most specific message.
 */
internal fun selectRepresentative(findings: List<Finding>): Finding =
    findings
        .sortedWith(
            compareByDescending<Finding> { it.severity }
                .thenByDescending { it.confidence }
                .thenByDescending { cardinalityRank(it.cardinalityBucket) }
                .thenBy { it.message.length }
                .thenBy { it.location.file }
                .thenBy { it.location.line },
        ).first()

/** Sort order for groups: severity → confidence → pathContext → cardinality → size → visibility → file → line. */
private val GROUP_ORDER: Comparator<IssueGroup> =
    compareByDescending<IssueGroup> { it.representativeFinding.severity }
        .thenByDescending { it.representativeFinding.confidence }
        .thenBy { pathContextRank(it.pathContext) }
        .thenByDescending { cardinalityRank(it.maxCardinality) }
        .thenByDescending { it.contributingFindings.size }
        .thenBy { visibilityRank(it.visibility) }
        .thenBy { it.representativeFinding.location.file }
        .thenBy { it.representativeFinding.location.line }

private fun pathContextRank(ctx: PathContext?): Int =
    when (ctx) {
        PathContext.REQUEST -> PATH_RANK_REQUEST
        PathContext.BATCH -> PATH_RANK_BATCH
        PathContext.MIXED -> PATH_RANK_MIXED
        null -> PATH_RANK_UNKNOWN
        PathContext.LIFECYCLE -> PATH_RANK_LIFECYCLE
    }

// CardinalityBucket and GroupVisibility are both declared in worst-to-best/best-to-worst
// ranking order already, so the rank is just the enum's ordinal - no separate mapping to
// keep in sync with the enum by hand.
private fun cardinalityRank(c: CardinalityBucket?): Int = (c ?: CardinalityBucket.UNKNOWN).ordinal

private const val PATH_RANK_REQUEST = 0
private const val PATH_RANK_BATCH = 1
private const val PATH_RANK_MIXED = 2
private const val PATH_RANK_UNKNOWN = 3
private const val PATH_RANK_LIFECYCLE = 4

private fun visibilityRank(v: GroupVisibility): Int = v.ordinal

// ── Method resolution helpers ──
// buildMethodRangeIndex/findEnclosingMethod/maxLineOf live in MethodRangeIndex.kt, shared with
// AnalysisEngine's cache persistence - see that file's kdoc for why.

private fun buildMethodAdjacency(callGraph: CallGraph): Map<String, Set<String>> {
    val adj = mutableMapOf<String, MutableSet<String>>()
    for ((caller, callees) in callGraph.allEdges()) {
        for (callee in callees) {
            adj.getOrPut(caller) { mutableSetOf() }.add(callee)
            adj.getOrPut(callee) { mutableSetOf() }.add(caller)
        }
    }
    return adj
}
