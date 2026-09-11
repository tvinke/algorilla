package com.github.tvinke.algorilla.model

import com.github.tvinke.algorilla.rules.Finding

/**
 * A group of related findings that share a common anchor method or class.
 * Used for presenting findings in a structured, less repetitive way.
 *
 * Each finding belongs to exactly one group. Groups do not merge transitively —
 * two anchor methods with shared callees remain separate groups.
 */
public data class IssueGroup(
    /** Stable identifier derived from anchor and group type. */
    val id: String,
    /** The anchor method or class that defines this group. */
    val anchor: String,
    /** How the findings in this group are related. */
    val groupType: GroupType,
    /** How reliably the grouping relationship was resolved. */
    val visibility: GroupVisibility,
    /** Architectural path context inherited from findings, if consistent. */
    val pathContext: PathContext?,
    /** Worst-case cardinality across contributing findings. */
    val maxCardinality: CardinalityBucket?,
    /** The most representative finding in this group (highest severity, then confidence). */
    val representativeFinding: Finding,
    /** All findings in this group, including the representative. */
    val contributingFindings: List<Finding>,
)

/** How findings within an [IssueGroup] are related. */
public enum class GroupType {
    /** All findings share the same enclosing method. */
    SAME_METHOD,

    /** Findings are in methods connected by a resolved caller→callee edge. */
    SAFE_CALL_EDGE,

    /** Findings share evidence-chain locations (supporting signal, not standalone merge). */
    SHARED_EVIDENCE_CHAIN,
}

/** How reliably the grouping relationship was established. */
public enum class GroupVisibility {
    /** All relationships in the group are from exact-name or single-impl resolution. */
    RESOLVED_SAFE,

    /** Some relationships rely on heuristic resolution (e.g. simple-name fallback). */
    AMBIGUOUS,

    /** Grouping is based solely on co-location, no call resolution involved. */
    UNKNOWN,
}
