package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity

/**
 * A static analysis rule that evaluates an [AnalysisContext] and produces [Finding] instances.
 * Each rule targets a specific algorithmic complexity anti-pattern.
 */
public interface Rule {
    /** Unique rule identifier in kebab-case, e.g. "nested-lookup", "sort-for-last". */
    public val id: String

    /** Human-readable rule name, e.g. "Nested Lookup". */
    public val name: String

    /** Default severity for findings produced by this rule. */
    public val severity: Severity

    /** Set of languages this rule applies to. */
    public val languages: Set<Language>

    /** Category that this rule belongs to. */
    public val category: RuleCategory

    /**
     * Previous rule IDs that this rule was known by. Used to keep suppress comments
     * and config overrides working after a rule is renamed or merged.
     */
    public val aliases: List<String>
        get() = emptyList()

    /**
     * Rule IDs that this rule subsumes (i.e. is a more specific version of).
     * When this rule and a subsumed rule both fire at the same source location,
     * the engine keeps this rule's finding and drops the subsumed one.
     *
     * Example: `nested-lookup` subsumes `expensive-callback` because a linear
     * lookup inside a loop is exactly the pattern both rules detect — `nested-lookup`
     * just gives a more precise diagnosis.
     */
    public val subsumes: Set<String>
        get() = emptySet()

    /**
     * Evaluates this rule against the given analysis context and returns any findings.
     */
    public fun evaluate(context: AnalysisContext): List<Finding>
}
