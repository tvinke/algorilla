package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * A concrete code fix suggestion attached to a [Finding].
 * For now, [description] is always set; [oldText] and [newText] are optional
 * and reserved for rules where the transformation is mechanical enough to express as a diff.
 */
public data class SuggestedFix(
    val description: String,
    val oldText: String? = null,
    val newText: String? = null,
)

/**
 * A single finding produced by a [Rule], representing a detected complexity anti-pattern.
 */
public data class Finding(
    val ruleId: String,
    val ruleName: String,
    val severity: Severity,
    val location: SourceLocation,
    val message: String,
    val suggestion: String,
    val currentComplexity: String? = null,
    val suggestedComplexity: String? = null,
    val evidence: List<Evidence> = emptyList(),
    val category: RuleCategory? = null,
    val suggestedFix: SuggestedFix? = null,
)
