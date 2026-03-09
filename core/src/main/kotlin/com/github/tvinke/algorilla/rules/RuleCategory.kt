package com.github.tvinke.algorilla.rules

/**
 * Broad category that groups related rules into families.
 */
public enum class RuleCategory(
    public val displayName: String,
) {
    LOOP_AMPLIFIER("Loop amplifiers"),
    SORT_ABUSE("Sort abuse"),
    REDUNDANCY("Redundancy"),
    CONSTRUCTION_COST("Construction cost"),
    QUERY_PATTERN("Query patterns"),
}
