package com.github.tvinke.algorilla.rules

/**
 * Holds the Big-O complexity estimate for a finding: what the code currently does
 * and what it could be after applying the suggested fix.
 */
public data class ComplexityEstimate(
    val current: String,
    val suggested: String,
)

/**
 * Central registry of complexity formulas used across all rules.
 * Each factory method encodes a specific algorithmic anti-pattern.
 *
 * This is the single source of truth for Big-O claims — keeping them in one place
 * makes it easy to audit, validate, and extend. Custom rules can use the same
 * factory methods or construct [ComplexityEstimate] directly.
 */
@Suppress("TooManyFunctions")
public object ComplexityModel {
    // ── Loop amplifiers ──────────────────────────────────────

    /** Linear scan inside a loop: O(outer × inner) → pre-build a Set/Map. */
    public fun loopTimesLookup(
        outerVar: String,
        innerVar: String,
    ): ComplexityEstimate = ComplexityEstimate("O($outerVar \u00d7 $innerVar)", "O($outerVar + $innerVar)")

    /** Expensive operation repeated every iteration: O(loop × cost) → hoist or batch. */
    public fun loopTimesCost(
        loopVar: String,
        costLabel: String,
    ): ComplexityEstimate = ComplexityEstimate("O($loopVar \u00d7 $costLabel)", "O($loopVar)")

    /** N+1 query pattern: one query per iteration → batch into a single query. */
    public fun nPlusOne(loopVar: String): ComplexityEstimate = ComplexityEstimate("O($loopVar \u00d7 IO)", "O(1 \u00d7 IO + $loopVar)")

    /** Blocking join per iteration: sequential waits → parallel dispatch. */
    public fun loopTimesWait(loopVar: String): ComplexityEstimate = ComplexityEstimate("O($loopVar \u00d7 wait)", "O(max-wait)")

    /** Collection copy inside loop: addAll/concat per iteration → accumulate then copy. */
    public fun loopTimesCopy(loopVar: String): ComplexityEstimate = ComplexityEstimate("O($loopVar \u00d7 copy)", "O($loopVar + copy)")

    // ── Sort abuse ───────────────────────────────────────────

    /** Sort the entire collection just to get first/last → linear scan instead. */
    public fun sortForAccess(): ComplexityEstimate = ComplexityEstimate("O(n log n)", "O(n)")

    /** Filter after sort: sorting elements that will be discarded → filter first. */
    public fun filterAfterSort(): ComplexityEstimate = ComplexityEstimate("O(n log n + k)", "O(k log k + n)")

    /** Linear lookup inside sort comparator: comparator runs O(n) per comparison. */
    public fun sortTimesLookup(): ComplexityEstimate = ComplexityEstimate("O(n\u00b2 log n)", "O(n log n)")

    /** Expensive parse/creation inside sort comparator. */
    public fun sortTimesParse(): ComplexityEstimate = ComplexityEstimate("O(n log n \u00d7 parse)", "O(n log n)")

    // ── Redundancy ───────────────────────────────────────────

    /** Same expensive call invoked multiple times → cache the result. */
    public fun redundantCalls(count: Int): ComplexityEstimate = ComplexityEstimate("O(${count}x)", "O(1x)")

    /** Same getter called multiple times with same argument → cache the result. */
    public fun uncachedGetter(count: Int): ComplexityEstimate = ComplexityEstimate("${count}x lookup", "1x lookup")

    /** Multiple linear scans on the same collection → combine into single pass. */
    public fun repeatedScans(count: Int): ComplexityEstimate = ComplexityEstimate("O(n \u00d7 $count)", "O(n)")

    // ── Query patterns ───────────────────────────────────────

    /** Full table/collection scan followed by in-memory filter → query with predicate. */
    public fun fullScanForLookup(): ComplexityEstimate = ComplexityEstimate("O(n)", "O(1)")

    // ── Construction cost ────────────────────────────────────

    /** Heavyweight object created per invocation → hoist to field/singleton. */
    public fun heavyweightPerInvocation(): ComplexityEstimate = ComplexityEstimate("O(n \u00d7 init)", "O(1)")

    /** Cascading getter chain: each hop may be O(n). */
    public fun chainedGetters(depth: Int): ComplexityEstimate = ComplexityEstimate("O(n^$depth)", "O(n)")

    // ── Evidence helpers ─────────────────────────────────────

    /** Loop evidence: shows the size of the iterated collection. */
    public fun loopEvidence(varName: String): String = "O($varName)"

    /** Bottleneck marker for the expensive inner operation. */
    public fun bottleneck(label: String): String = "$label \u2190 bottleneck"

    /** Bottleneck with Big-O wrapper. */
    public fun bottleneckO(varName: String): String = "O($varName) \u2190 bottleneck"

    /** Sort evidence: O(n log n) with optional bottleneck marker. */
    public fun sortEvidence(isBottleneck: Boolean = false): String = if (isBottleneck) "O(n log n) \u2190 bottleneck" else "O(n log n)"
}
