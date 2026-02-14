package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity

/**
 * A static analysis rule that evaluates an [AnalysisContext] and produces [Finding] instances.
 * Each rule targets a specific algorithmic complexity anti-pattern.
 */
public interface Rule {
    /** Unique rule identifier, e.g. "CL001". */
    public val id: String

    /** Human-readable rule name, e.g. "nested-lookup". */
    public val name: String

    /** Default severity for findings produced by this rule. */
    public val severity: Severity

    /** Set of languages this rule applies to. */
    public val languages: Set<Language>

    /**
     * Evaluates this rule against the given analysis context and returns any findings.
     */
    public fun evaluate(context: AnalysisContext): List<Finding>
}
