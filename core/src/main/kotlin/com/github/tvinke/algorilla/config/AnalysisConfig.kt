package com.github.tvinke.algorilla.config

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity

/**
 * Configuration for an analysis run. Parsed from `.algorilla.yml` by the CLI module
 * and passed to the engine as an immutable data structure.
 */
public data class AnalysisConfig(
    val languages: Set<Language> = Language.entries.toSet(),
    val excludePatterns: List<String> = emptyList(),
    val ruleOverrides: Map<String, RuleOverride> = emptyMap(),
    val typeHints: Map<String, String> = emptyMap(),
    val maxCallDepth: Int = DEFAULT_MAX_CALL_DEPTH,
    val minSeverity: Severity = Severity.WARNING,
    val minConfidence: Confidence = Confidence.MEDIUM,
    val heavyweightTypes: Set<String> = DEFAULT_HEAVYWEIGHT_TYPES,
    val includeTests: Boolean = false,
) {
    public companion object {
        public const val DEFAULT_MAX_CALL_DEPTH: Int = 5

        public val DEFAULT_HEAVYWEIGHT_TYPES: Set<String> =
            setOf(
                "ObjectMapper",
                "Gson",
                "XmlMapper",
                "DocumentBuilderFactory",
                "TransformerFactory",
            )
    }
}

/**
 * Per-rule configuration override.
 */
public data class RuleOverride(
    val enabled: Boolean = true,
    val severity: Severity? = null,
)
