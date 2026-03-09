package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.Reporter
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import java.util.Locale

/**
 * Formats analysis results for console display, grouped by file with evidence chains
 * and a summary line showing file/language breakdown.
 */
public class ConsoleReporter : Reporter {
    override fun report(
        result: AnalysisResult,
        output: Appendable,
    ) {
        if (result.findings.isEmpty()) {
            formatSummary(result, output)
            return
        }

        val grouped = result.findings.groupBy { it.location.file }
        for ((file, findings) in grouped) {
            val shortFile = file.substringAfterLast('/')
            output.appendLine("$shortFile (${findings.size} ${pluralize("finding", findings.size)})")
            for (finding in findings) {
                formatFinding(finding, output)
            }
            output.appendLine()
        }

        formatSummary(result, output)
    }

    private fun formatFinding(
        finding: Finding,
        output: Appendable,
    ) {
        val complexity = formatComplexity(finding)
        val categoryTag = finding.category?.let { "[${it.displayName}] " } ?: ""
        output.appendLine("  :${finding.location.line} $categoryTag${finding.ruleId}$complexity")
        output.appendLine("    ${finding.message}")
        output.appendLine("    → ${finding.suggestion}")
        val fix = finding.suggestedFix
        if (fix != null) {
            output.appendLine("    → Suggested fix: ${fix.description}")
        }
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
        finding.evidence.forEachIndexed { index, evidence ->
            val file = evidence.location.file.substringAfterLast('/')
            val loc = "$file:${evidence.location.line}"
            output.appendLine("    ${index + 1}. ${loc.padEnd(LOC_PAD)} ${evidence.label}")
        }
    }

    private fun formatSummary(
        result: AnalysisResult,
        output: Appendable,
    ) {
        val fileCount =
            result.findings
                .map { it.location.file }
                .toSet()
                .size
        val elapsedStr = String.format(Locale.US, "%.1f", result.elapsedMs / MS_PER_SECOND)
        val errors = result.findings.count { it.severity == Severity.ERROR }
        val warnings = result.findings.count { it.severity == Severity.WARNING }
        val infos = result.findings.count { it.severity == Severity.INFO }

        val parts = mutableListOf<String>()
        if (errors > 0) parts.add("$errors ${pluralize("error", errors)}")
        if (warnings > 0) parts.add("$warnings ${pluralize("warning", warnings)}")
        if (infos > 0) parts.add("$infos info")
        val breakdown = if (parts.isNotEmpty()) " (${parts.joinToString(", ")})" else ""

        output.appendLine(
            "Scanned ${result.filesAnalyzed} files in ${elapsedStr}s. " +
                "Found ${result.findings.size} ${pluralize("issue", result.findings.size)}$breakdown" +
                if (fileCount > 0) " across $fileCount ${pluralize("file", fileCount)}." else ".",
        )
    }

    private fun pluralize(
        word: String,
        count: Int,
    ): String = if (count == 1) word else "${word}s"

    private companion object {
        const val LOC_PAD = 30
        const val MS_PER_SECOND = 1000.0
    }
}
