package com.github.tvinke.algorilla.model

/**
 * A type inferred for a local variable, parameter, or field during analysis.
 * Carries provenance so consumers can decide how much to trust the inference.
 */
public data class InferredType(
    /** Simple type name, e.g. "HashMap", "Set", "List". Generics stripped. */
    val simpleName: String,
    /** How the type was determined. Higher-trust sources are more reliable for suppression. */
    val source: TypeSource,
    /** Full type text including generics, e.g. "List<Order>", "Map<String, Order>". Null when unknown. */
    val fullTypeName: String? = null,
) {
    /**
     * Parses top-level type arguments from [fullTypeName].
     * `"Map<String, List<Order>>"` → `["String", "List<Order>"]`.
     * Returns empty list when no generics or [fullTypeName] is null.
     */
    val typeArguments: List<String>
        get() {
            val full = fullTypeName ?: return emptyList()
            val start = full.indexOf('<')
            if (start < 0) return emptyList()
            val inner = full.substring(start + 1, full.length - 1)
            return splitTopLevelTypeArgs(inner)
        }
}

private fun splitTopLevelTypeArgs(inner: String): List<String> {
    val args = mutableListOf<String>()
    var depth = 0
    var start = 0
    for (i in inner.indices) {
        when (inner[i]) {
            '<' -> depth++
            '>' -> depth--
            ',' ->
                if (depth == 0) {
                    args.add(inner.substring(start, i).trim())
                    start = i + 1
                }
        }
    }
    args.add(inner.substring(start).trim())
    return args.filter { it.isNotEmpty() }
}

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

    /** Inferred from a method return type in another file (L1b). */
    CROSS_FILE_RETURN,

    /** Inferred from method name suffix (e.g. `getCharacterSet()` → "Set"). Low trust. */
    NAME_HEURISTIC,
}
