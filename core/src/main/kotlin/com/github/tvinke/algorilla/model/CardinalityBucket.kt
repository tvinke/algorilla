package com.github.tvinke.algorilla.model

/**
 * Coarse classification of the iteration space of a loop.
 * Used for prioritisation: a loop over a DB query result is more impactful
 * than a loop over three enum values.
 */
public enum class CardinalityBucket {
    /** Compile-time bounded: enum, literal list, single-iteration, numeric < threshold. */
    CONSTANT_SMALL,

    /** Cannot determine — default bucket. */
    UNKNOWN,

    /** Source suggests unbounded/large: DB query result, repository findAll, bulk load. */
    LIKELY_LARGE,
}
