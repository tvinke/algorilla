package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.FunctionCall

/**
 * Shared bounded small-factory semantics used by both parser-time direct receiver handling
 * and rule-time variable initializer propagation.
 */
public object SmallFactoryCollectionSemantics {
    private val CONSTANT_SIZE_FACTORY_METHODS =
        setOf(
            "asList",
            "of",
            "singletonList",
            "singletonMap",
            "singleton",
            "emptyList",
            "emptySet",
            "emptyMap",
            "nCopies",
        )

    private const val SMALL_COLLECTION_THRESHOLD = 8

    public fun isConstantSizeFactory(call: FunctionCall): Boolean =
        call.name in CONSTANT_SIZE_FACTORY_METHODS && call.arguments.size <= SMALL_COLLECTION_THRESHOLD
}
