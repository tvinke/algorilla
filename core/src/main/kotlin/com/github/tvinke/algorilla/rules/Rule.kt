package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Confidence
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
     * Baseline confidence the engine assigns to findings from this rule.
     *
     * - [Confidence.HIGH] — the pattern is structurally unambiguous (always an anti-pattern
     *   regardless of types). Example: string concatenation in a loop is always O(n²).
     * - [Confidence.MEDIUM] — detection is reliable but may depend on context or types.
     *   This is the default for most rules.
     * - [Confidence.LOW] — detection relies heavily on heuristics, with a known high
     *   false-positive rate. Findings are hidden at the default `--confidence medium`.
     *
     * The engine may further adjust confidence based on evidence (e.g. cross-method findings
     * are capped at MEDIUM, untyped languages may demote to LOW when [requiresTypeContext]).
     */
    public val defaultConfidence: Confidence
        get() = Confidence.MEDIUM

    /**
     * Whether this rule's pattern depends on type resolution to be accurate. When `true`
     * and the file's language lacks type declarations, the engine demotes findings to
     * [Confidence.LOW]. This prevents false positives from name-based detection in
     * dynamically-typed languages (e.g. JS `Array.concat()` ≠ Java `String.concat()`).
     */
    public val requiresTypeContext: Boolean
        get() = false

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
     */
    public val subsumes: Set<String>
        get() = emptySet()

    /**
     * Evaluates this rule against the given analysis context and returns any findings.
     */
    public fun evaluate(context: AnalysisContext): List<Finding>
}
