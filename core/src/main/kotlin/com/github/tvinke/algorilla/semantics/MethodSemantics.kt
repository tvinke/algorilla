package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind

/**
 * The semantic category of a collection method.
 */
public enum class SemanticCategory {
    ITERATION,
    LOOKUP,
    SORT,
    ACCESS,
    QUADRATIC,
    BLOCKING,
    SERIALIZATION,
    REPOSITORY_CALL,
    COPY_ON_MODIFY,
}

/**
 * Describes the semantics of a single method name within a language registry.
 */
public data class MethodSemantics(
    val category: SemanticCategory,
    val lookupKind: LookupKind? = null,
    val sortKind: SortKind? = null,
    val accessKind: AccessKind? = null,
    val complexity: String? = null,
    val note: String? = null,
)
