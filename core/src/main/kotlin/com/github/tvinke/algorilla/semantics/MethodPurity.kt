package com.github.tvinke.algorilla.semantics

/**
 * Classifies a method call as pure, side-effectful, or unknown based on naming conventions.
 * All heuristics are loaded from the YAML semantics registry — no hardcoded lists.
 */
public enum class Purity { PURE, SIDE_EFFECT, UNKNOWN }

public object MethodPurity {
    private val registry: LanguageSemanticsRegistry by lazy {
        LanguageSemanticsRegistry.DEFAULT
    }

    private val purePrefixes: List<String> by lazy { registry.allPurePrefixes() }
    private val sideEffectPrefixes: List<String> by lazy { registry.allSideEffectPrefixes() }
    private val sideEffectTargets: List<String> by lazy { registry.allSideEffectTargets() }

    /**
     * Classifies a method name by its likely purity.
     * Side-effect prefixes are checked first so that e.g. "setFormat" is SIDE_EFFECT.
     */
    public fun classify(methodName: String): Purity {
        val lower = methodName.lowercase()
        if (sideEffectPrefixes.any { lower.startsWith(it) }) return Purity.SIDE_EFFECT
        if (purePrefixes.any { lower.startsWith(it) }) return Purity.PURE
        return Purity.UNKNOWN
    }

    /**
     * Classifies a method call including its target context.
     * A call on a known side-effect target (logger, builder, etc.) is always SIDE_EFFECT.
     */
    public fun classify(
        methodName: String,
        qualifiedTarget: String?,
    ): Purity {
        if (qualifiedTarget != null) {
            val lowerTarget = qualifiedTarget.lowercase()
            if (sideEffectTargets.any { lowerTarget.contains(it) }) return Purity.SIDE_EFFECT
        }
        return classify(methodName)
    }

    /**
     * Returns true if the method is classified as side-effectful (by name or target).
     */
    public fun isSideEffect(
        methodName: String,
        qualifiedTarget: String? = null,
    ): Boolean = classify(methodName, qualifiedTarget) == Purity.SIDE_EFFECT
}
