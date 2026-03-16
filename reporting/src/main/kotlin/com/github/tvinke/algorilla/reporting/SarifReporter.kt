package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.Reporter
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import io.github.detekt.sarif4k.ArtifactLocation
import io.github.detekt.sarif4k.Level
import io.github.detekt.sarif4k.Location
import io.github.detekt.sarif4k.Message
import io.github.detekt.sarif4k.MultiformatMessageString
import io.github.detekt.sarif4k.PhysicalLocation
import io.github.detekt.sarif4k.Region
import io.github.detekt.sarif4k.ReportingDescriptor
import io.github.detekt.sarif4k.Result
import io.github.detekt.sarif4k.Run
import io.github.detekt.sarif4k.SarifSchema210
import io.github.detekt.sarif4k.SarifSerializer
import io.github.detekt.sarif4k.Tool
import io.github.detekt.sarif4k.ToolComponent
import io.github.detekt.sarif4k.Version

/**
 * Formats analysis results as a SARIF 2.1.0 JSON document using the sarif4k library.
 */
public class SarifReporter : Reporter {
    override fun report(
        result: AnalysisResult,
        output: Appendable,
    ) {
        val sarif = buildSarif(result)
        output.append(SarifSerializer.toJson(sarif))
    }

    private fun buildSarif(result: AnalysisResult): SarifSchema210 {
        val rules = buildRuleDescriptors(result)
        val ruleIndex = rules.mapIndexed { index, rule -> rule.id to index }.toMap()
        return SarifSchema210(
            schema = SARIF_SCHEMA_URI,
            version = Version.The210,
            runs =
                listOf(
                    Run(
                        tool = buildTool(rules),
                        results = result.findings.map { toSarifResult(it, ruleIndex) },
                    ),
                ),
        )
    }

    private fun buildTool(rules: List<ReportingDescriptor>): Tool =
        Tool(
            driver =
                ToolComponent(
                    name = TOOL_NAME,
                    version = TOOL_VERSION,
                    rules = rules,
                ),
        )

    private fun buildRuleDescriptors(result: AnalysisResult): List<ReportingDescriptor> =
        result.findings
            .map { it.ruleId }
            .distinct()
            .map { ruleId ->
                val sample = result.findings.first { it.ruleId == ruleId }
                ReportingDescriptor(
                    id = ruleId,
                    name = sample.ruleName,
                    shortDescription = MultiformatMessageString(text = sample.ruleName),
                    helpURI = "$DOCS_BASE_URL$ruleId",
                )
            }

    private fun toSarifResult(
        finding: Finding,
        ruleIndex: Map<String, Int>,
    ): Result =
        Result(
            ruleID = finding.ruleId,
            ruleIndex = ruleIndex[finding.ruleId]?.toLong(),
            level = finding.severity.toSarifLevel(),
            rank = finding.confidence.toSarifRank(),
            message = buildMessage(finding),
            locations = listOf(finding.location.toSarifLocation()),
        )

    private fun buildMessage(finding: Finding): Message {
        val text =
            buildString {
                append(finding.message)
                append(" Suggestion: ")
                append(finding.suggestion)
                if (finding.currentComplexity != null && finding.suggestedComplexity != null) {
                    append(" (${finding.currentComplexity} -> ${finding.suggestedComplexity})")
                }
            }
        return Message(text = text)
    }

    private companion object {
        const val SARIF_SCHEMA_URI =
            "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/main/sarif-2.1/schema/sarif-schema-2.1.0.json"
        const val TOOL_NAME = "algorilla"
        const val DOCS_BASE_URL = ReporterConstants.DOCS_BASE_URL

        val TOOL_VERSION: String by lazy {
            val props = java.util.Properties()
            SarifReporter::class.java.classLoader
                .getResourceAsStream("algorilla-version.properties")
                ?.use { props.load(it) }
            props.getProperty("version", "unknown")
        }
    }
}

private fun Severity.toSarifLevel(): Level =
    when (this) {
        Severity.ERROR -> Level.Error
        Severity.WARNING -> Level.Warning
        Severity.INFO -> Level.Note
    }

private const val SARIF_RANK_HIGH = 90.0
private const val SARIF_RANK_MEDIUM = 50.0
private const val SARIF_RANK_LOW = 10.0

private fun Confidence.toSarifRank(): Double =
    when (this) {
        Confidence.HIGH -> SARIF_RANK_HIGH
        Confidence.MEDIUM -> SARIF_RANK_MEDIUM
        Confidence.LOW -> SARIF_RANK_LOW
    }

private fun com.github.tvinke.algorilla.model.SourceLocation.toSarifLocation(): Location =
    Location(
        physicalLocation =
            PhysicalLocation(
                artifactLocation = ArtifactLocation(uri = file),
                region =
                    Region(
                        startLine = line.toLong(),
                        startColumn = column.toLong(),
                    ),
            ),
    )
