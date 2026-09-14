package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.util.containsAnyAtWordBoundary

/**
 * Classifies a method call as pure, side-effectful, or unknown based on naming conventions.
 * All heuristics are loaded from the YAML semantics registry — no hardcoded lists.
 */
public enum class Purity { PURE, SIDE_EFFECT, UNKNOWN }

public object MethodPurity {
    private val registry: LanguageSemanticsRegistry by lazy {
        LanguageSemanticsRegistry.DEFAULT
    }

    /**
     * Classifies a method name by its likely purity.
     * Side-effect prefixes are checked first so that e.g. "setFormat" is SIDE_EFFECT.
     */
    public fun classify(
        methodName: String,
        language: Language = Language.JAVA,
    ): Purity {
        val lower = methodName.lowercase()
        val sideEffectPrefixes = registry.sideEffectPrefixes(language)
        val purePrefixes = registry.purePrefixes(language)
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
        language: Language = Language.JAVA,
    ): Purity {
        if (qualifiedTarget != null) {
            // Original case for the boundary check - lowercasing first would destroy the
            // camelCase signal, and "log" (a real side-effect-targets entry) is short enough
            // that "catalog"/"dialog"/"analog" all satisfy a bare contains with no boundary
            // check at all, unrelated words that merely happen to end in the same letters.
            if (containsAnyAtWordBoundary(qualifiedTarget, registry.sideEffectTargets(language))) return Purity.SIDE_EFFECT
        }
        return classify(methodName, language)
    }

    /**
     * Returns true if the method is classified as side-effectful (by name or target).
     */
    public fun isSideEffect(
        methodName: String,
        qualifiedTarget: String? = null,
        language: Language = Language.JAVA,
    ): Boolean = classify(methodName, qualifiedTarget, language) == Purity.SIDE_EFFECT
}
