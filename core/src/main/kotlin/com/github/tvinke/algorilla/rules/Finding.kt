package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation

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
)
