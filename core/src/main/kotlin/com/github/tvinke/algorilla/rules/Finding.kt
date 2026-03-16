package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * A single finding produced by a [Rule], representing a detected complexity anti-pattern.
 *
 * @property ruleId Unique rule identifier, e.g. `"nested-lookup"`, `"sort-for-last"`.
 * @property ruleName Human-readable rule name, e.g. `"Nested Lookup"`, `"Sort For Last"`.
 * @property severity How actionable this finding is — [Severity.ERROR] for clear performance bugs,
 *   [Severity.WARNING] for likely issues, [Severity.INFO] for informational hints.
 * @property confidence How sure Algorilla is that this finding is a true positive. Orthogonal to
 *   severity: a finding can be high-severity but low-confidence (e.g. a likely O(n²) pattern where
 *   the receiver type is unknown). Defaults to [Confidence.MEDIUM].
 * @property location Source file, line, and column where the anti-pattern was detected.
 * @property message Explanation of what was detected, e.g.
 *   `"Linear lookup list.contains() inside for-loop over items"`.
 * @property suggestions Typed suggestion objects describing recommended fixes.
 * @property currentComplexity Big-O or multiplier string for current code, e.g.
 *   `"O(n × m)"`, `"O(n log n)"`, `"2x lookup"`, `"O(n × init)"`. Null when not applicable.
 * @property suggestedComplexity Big-O or multiplier string after applying the suggestion, e.g.
 *   `"O(n + m)"`, `"O(n)"`, `"1x lookup"`, `"O(1)"`. Null when not applicable.
 * @property evidence Ordered chain of source locations tracing why this finding was reported —
 *   from the outer loop down to the expensive inner operation.
 * @property category Broad rule family, set by the engine after evaluation. Null for uncategorized rules.
 */
public data class Finding(
    val ruleId: String,
    val ruleName: String,
    val severity: Severity,
    val location: SourceLocation,
    val message: String,
    val suggestions: List<Suggestion>,
    val confidence: Confidence = Confidence.MEDIUM,
    val currentComplexity: String? = null,
    val suggestedComplexity: String? = null,
    val evidence: List<Evidence> = emptyList(),
    val category: RuleCategory? = null,
) {
    /** Primary suggestion text for reporters that render a single string. */
    val suggestion: String get() = suggestions.firstOrNull()?.render() ?: ""
}
