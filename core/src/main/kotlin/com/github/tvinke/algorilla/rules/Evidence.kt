package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * A single step in the evidence chain explaining why a finding was reported.
 * Together, evidence entries form a traceable path from the outer loop to the problematic operation.
 */
public data class Evidence(
    val location: SourceLocation,
    val label: String,
    val executionContext: ExecutionContext,
    val depth: Int = 0,
    /** Optional per-step complexity hint, e.g. "O(n)" or "O(1)". */
    val complexity: String? = null,
)
