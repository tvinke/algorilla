package com.github.tvinke.algorilla.model

/**
 * A type inferred for a local variable, parameter, or field during analysis.
 * Carries provenance so consumers can decide how much to trust the inference.
 */
public data class InferredType(
    /** Simple type name, e.g. "HashMap", "Set", "List". */
    val simpleName: String,
    /** How the type was determined. Higher-trust sources are more reliable for suppression. */
    val source: TypeSource,
)

/**
 * How a type was inferred, ordered from highest to lowest trust.
 */
public enum class TypeSource {
    /** Explicit type annotation in source code (parameter type, variable declaration). */
    DECLARED,

    /** Inferred from `new Type()` / constructor call. */
    CONSTRUCTOR,

    /** Inferred from a known factory method (e.g. `Set.of()`, `setOf()`). YAML-driven. */
    FACTORY,

    /** Inferred from a stream terminal operation (e.g. `.collect(toSet())`). */
    CHAIN_END,

    /** Inferred from method name suffix (e.g. `getCharacterSet()` → "Set"). Low trust. */
    NAME_HEURISTIC,
}
