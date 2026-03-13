package com.github.tvinke.algorilla.model

/**
 * How confident Algorilla is that a finding is a true positive.
 *
 * Orthogonal to [Severity]: severity measures "how bad is this IF real",
 * confidence measures "how sure are we this IS real."
 *
 * - [HIGH] — Structurally unambiguous: the pattern is always an anti-pattern regardless of types
 *   or runtime context (e.g. string concatenation in a loop, sorting to get min/max).
 * - [MEDIUM] — Likely correct based on available evidence, but not provable from static analysis
 *   alone (e.g. cross-method inferences, name-based heuristics with some type support).
 * - [LOW] — Plausible but uncertain: detection relies on naming conventions or missing type info
 *   (e.g. a `.contains()` call where the receiver type is unknown — could be a HashSet).
 */
public enum class Confidence {
    LOW,
    MEDIUM,
    HIGH,
}
