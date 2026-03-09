package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.Reporter
import com.github.tvinke.algorilla.model.Severity
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

    private fun buildReport(result: AnalysisResult): JsonReport {
        val findings =
            result.findings.map { finding ->
                JsonFinding(
                    ruleId = finding.ruleId,
                    ruleName = finding.ruleName,
                    category = finding.category?.name,
                    severity = finding.severity.name,
                    location = finding.location.toJsonLocation(),
                    message = finding.message,
                    suggestion = finding.suggestion,
                    currentComplexity = finding.currentComplexity,
                    suggestedComplexity = finding.suggestedComplexity,
                    evidence = finding.evidence.map { it.toJsonEvidence() },
                )
            }
        return JsonReport(
            summary = buildSummary(result),
            findings = findings,
        )
    }

    private fun buildSummary(result: AnalysisResult): JsonSummary {
        val findings = result.findings
        return JsonSummary(
            totalFindings = findings.size,
            errors = findings.count { it.severity == Severity.ERROR },
            warnings = findings.count { it.severity == Severity.WARNING },
            infos = findings.count { it.severity == Severity.INFO },
            filesAnalyzed = result.filesAnalyzed,
            elapsedMs = result.elapsedMs,
        )
    }

    private companion object {
        val jsonFormat = Json { prettyPrint = true }
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

@Serializable
internal data class JsonReport(
    val summary: JsonSummary,
    val findings: List<JsonFinding>,
)

@Serializable
internal data class JsonSummary(
    val totalFindings: Int,
    val errors: Int,
    val warnings: Int,
    val infos: Int,
    val filesAnalyzed: Int,
    val elapsedMs: Long,
)

@Serializable
internal data class JsonFinding(
    val ruleId: String,
    val ruleName: String,
    val category: String? = null,
    val severity: String,
    val location: JsonLocation,
    val message: String,
    val suggestion: String,
    val currentComplexity: String? = null,
    val suggestedComplexity: String? = null,
    val evidence: List<JsonEvidence> = emptyList(),
)

@Serializable
internal data class JsonLocation(
    val file: String,
    val line: Int,
    val column: Int,
)

@Serializable
internal data class JsonEvidence(
    val location: JsonLocation,
    val label: String,
    @SerialName("executionContext")
    val executionContext: String,
    val depth: Int = 0,
    val complexity: String? = null,
)
