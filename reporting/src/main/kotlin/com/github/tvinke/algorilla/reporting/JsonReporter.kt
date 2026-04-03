package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.Reporter
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Formats analysis results as a structured JSON document with summary and findings.
 */
public class JsonReporter : Reporter {
    override fun report(
        result: AnalysisResult,
        output: Appendable,
    ) {
        val report = buildReport(result)
        output.append(jsonFormat.encodeToString(report))
        output.appendLine()
    }

    private fun buildReport(result: AnalysisResult): JsonReport =
        JsonReport(
            summary = buildSummary(result),
            findings = result.findings.map { toJsonFinding(it, result.projectRoot) },
        )

    private fun toJsonFinding(
        finding: Finding,
        projectRoot: java.io.File?,
    ): JsonFinding =
        JsonFinding(
            ruleId = finding.ruleId,
            ruleName = finding.ruleName,
            category = finding.category?.name,
            severity = finding.severity.name,
            confidence = finding.confidence.name,
            location = finding.location.toJsonLocation(),
            message = finding.message,
            suggestion = finding.suggestion,
            suggestions =
                finding.suggestions.map { s ->
                    JsonSuggestion(type = s::class.simpleName ?: "Freeform", text = s.render())
                },
            currentComplexity = finding.currentComplexity,
            suggestedComplexity = finding.suggestedComplexity,
            ruleUrl = ReporterConstants.ruleUrl(finding.ruleId),
            evidence = finding.evidence.map { it.toJsonEvidence() },
            fingerprint = Baseline.fingerprintOf(finding, projectRoot).contentHash,
            suggestedCode = finding.suggestedCode?.code,
            suggestedCodeLanguage = finding.suggestedCode?.language,
            suggestedCodeFramework = finding.suggestedCode?.framework,
            pathContext = finding.pathContext?.name,
        )

    private fun buildSummary(result: AnalysisResult): JsonSummary {
        val findings = result.findings
        return JsonSummary(
            totalFindings = findings.size,
            errors = findings.count { it.severity == Severity.ERROR },
            warnings = findings.count { it.severity == Severity.WARNING },
            infos = findings.count { it.severity == Severity.INFO },
            filesAnalyzed = result.filesAnalyzed,
            elapsedMs = result.elapsedMs,
            baselinedCount = result.baselinedCount,
        )
    }

    private companion object {
        val jsonFormat =
            Json {
                prettyPrint = true
                encodeDefaults = true
            }
    }
}

private fun com.github.tvinke.algorilla.model.SourceLocation.toJsonLocation(): JsonLocation =
    JsonLocation(file = file, line = line, column = column)

private fun com.github.tvinke.algorilla.rules.Evidence.toJsonEvidence(): JsonEvidence =
    JsonEvidence(
        location = location.toJsonLocation(),
        label = label,
        executionContext = executionContext.name,
        depth = depth,
        complexity = complexity,
    )

/**
 * Top-level JSON output structure containing a summary and all individual findings.
 */
@Serializable
internal data class JsonReport(
    val schemaVersion: Int = SCHEMA_VERSION,
    val algorillaVersion: String = toolVersion(),
    val summary: JsonSummary,
    val findings: List<JsonFinding>,
)

private const val SCHEMA_VERSION = 2

private fun toolVersion(): String {
    val props = java.util.Properties()
    JsonReporter::class.java.classLoader
        .getResourceAsStream("algorilla-version.properties")
        ?.use { props.load(it) }
    return props.getProperty("version", "unknown")
}

/**
 * Aggregate counts for a scan run.
 */
@Serializable
internal data class JsonSummary(
    val totalFindings: Int,
    val errors: Int,
    val warnings: Int,
    val infos: Int,
    val filesAnalyzed: Int,
    val elapsedMs: Long,
    val baselinedCount: Int = 0,
)

/**
 * JSON representation of a single finding.
 *
 * @property ruleId Rule identifier, e.g. `"nested-lookup"`, `"sort-for-last"`.
 * @property ruleName Human-readable name, e.g. `"Nested Lookup"`.
 * @property category Rule family name, e.g. `"Loop amplifiers"`, `"Sort abuse"`. Null if uncategorized.
 * @property severity `"ERROR"`, `"WARNING"`, or `"INFO"`.
 * @property currentComplexity Big-O or multiplier notation for the current code, e.g.
 *   `"O(n × m)"`, `"O(n log n)"`, `"2x lookup"`. Null when not applicable.
 * @property suggestedComplexity Expected complexity after applying the fix, e.g.
 *   `"O(n + m)"`, `"O(n)"`, `"1x lookup"`. Null when not applicable.
 */
@Serializable
internal data class JsonFinding(
    val ruleId: String,
    val ruleName: String,
    val category: String? = null,
    val severity: String,
    val confidence: String = "MEDIUM",
    val location: JsonLocation,
    val message: String,
    val suggestion: String,
    val suggestions: List<JsonSuggestion> = emptyList(),
    val ruleUrl: String,
    val currentComplexity: String? = null,
    val suggestedComplexity: String? = null,
    val evidence: List<JsonEvidence> = emptyList(),
    val fingerprint: String? = null,
    val suggestedCode: String? = null,
    val suggestedCodeLanguage: String? = null,
    val suggestedCodeFramework: String? = null,
    val pathContext: String? = null,
)

@Serializable
internal data class JsonLocation(
    val file: String,
    val line: Int,
    val column: Int,
)

/**
 * A step in the evidence chain showing how the anti-pattern was traced.
 *
 * @property label Description of this step, e.g. `"for-loop over items"`, `"contains() call"`.
 * @property executionContext `"LOOP"`, `"CALLBACK"`, `"SORT_COMPARATOR"`, or `"TOP_LEVEL"`.
 * @property depth Nesting depth in the evidence chain (0 = outermost).
 * @property complexity Per-step complexity hint, e.g. `"O(n)"`, `"O(n log n) ← bottleneck"`. Null if not set.
 */
@Serializable
internal data class JsonEvidence(
    val location: JsonLocation,
    val label: String,
    @SerialName("executionContext")
    val executionContext: String,
    val depth: Int = 0,
    val complexity: String? = null,
)

/**
 * JSON representation of a typed suggestion.
 *
 * @property type The suggestion variant name, e.g. `"Freeform"`, `"PreBuildStructure"`, `"CacheResult"`.
 * @property text The rendered human-readable suggestion text.
 */
@Serializable
internal data class JsonSuggestion(
    val type: String,
    val text: String,
)
