package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.Reporter
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import java.util.Locale

/**
 * Formats analysis results for console display with evidence chains and a summary line.
 */
public class ConsoleReporter : Reporter {
    override fun report(
        result: AnalysisResult,
        output: Appendable,
    ) {
        for (finding in result.findings) {
            formatFinding(finding, output)
            output.appendLine()
        }
        formatSummary(result, output)
    }

    private fun formatFinding(
        finding: Finding,
        output: Appendable,
    ) {
        output.appendLine(finding.location.format())
        val complexity = formatComplexity(finding)
        output.appendLine("  ${finding.ruleId} ${finding.ruleName}$complexity")
        output.appendLine("  ${finding.message}")
        output.appendLine("  → ${finding.suggestion}")
        output.appendLine("  Details: $DOCS_BASE_URL${finding.ruleId}")
        formatEvidence(finding, output)
    }

    private fun formatComplexity(finding: Finding): String =
        if (finding.currentComplexity != null && finding.suggestedComplexity != null) {
            " (${finding.currentComplexity} → ${finding.suggestedComplexity})"
        } else {
            ""
        }

    private fun formatEvidence(
        finding: Finding,
        output: Appendable,
    ) {
        if (finding.evidence.isEmpty()) return
        output.appendLine("  Evidence:")
        finding.evidence.forEachIndexed { index, evidence ->
            val file = evidence.location.file.substringAfterLast('/')
            val loc = "$file:${evidence.location.line}"
            output.appendLine(
                "    ${index + 1}. ${loc.padEnd(LOC_PAD)} " +
                    "${evidence.label.padEnd(LABEL_PAD)} " +
                    "[${evidence.executionContext}]",
            )
        }
    }

    private fun formatSummary(
        result: AnalysisResult,
        output: Appendable,
    ) {
        val errors = result.findings.count { it.severity == Severity.ERROR }
        val warnings = result.findings.count { it.severity == Severity.WARNING }
        val elapsedStr = String.format(Locale.US, "%.1f", result.elapsedMs / MS_PER_SECOND)
        output.appendLine(
            "Found ${result.findings.size} issues " +
                "($errors errors, $warnings warnings) " +
                "in ${result.filesAnalyzed} files analyzed (${elapsedStr}s)",
        )
    }

    private companion object {
        const val LOC_PAD = 30
        const val LABEL_PAD = 35
        const val MS_PER_SECOND = 1000.0
        const val DOCS_BASE_URL = "https://tvinke.github.io/algorilla/rules/"
    }
}
