package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.engine.Reporter
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.Finding
import java.util.Locale

/**
 * Formats analysis results for console display, grouped by file with evidence chains,
 * code snippets, and a summary line showing file/language breakdown.
 */
public class ConsoleReporter(
    private val color: Boolean = false,
    private val baseDir: String? = null,
    private val sourceRoots: List<String> = emptyList(),
) : Reporter {
    override fun report(
        result: AnalysisResult,
        output: Appendable,
    ) {
        if (result.findings.isEmpty()) {
            formatSummary(result, output)
            return
        }

        val snippetRenderer = SnippetRenderer(color)
        val grouped =
            result.findings
                .groupBy { it.location.file }
                .entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, List<Finding>>> { entry ->
                        entry.value.maxOf { it.severity.ordinal }
                    }.thenBy { it.key },
                )
        for ((file, findings) in grouped) {
            val displayFile = relativize(file)
            val header =
                "\u23fa " + Ansi.underline(Ansi.boldWhite(displayFile, color), color) +
                    " " + Ansi.dim("(${findings.size} ${pluralize("finding", findings.size)})", color)
            output.appendLine(header)
            for (finding in findings) {
                formatFinding(finding, snippetRenderer, output)
            }
            output.appendLine()
        }

        formatSummary(result, output)
    }

    private fun formatFinding(
        finding: Finding,
        snippetRenderer: SnippetRenderer,
        output: Appendable,
    ) {
        val sep = Ansi.dim(" \u00b7 ", color)
        val severityTag = formatSeverityTag(finding.severity)
        val confidenceTag = formatConfidenceTag(finding.confidence)
        val ruleId = Ansi.boldWhite(finding.ruleId, color)
        val category = finding.category?.let { sep + Ansi.dim(it.displayName, color) } ?: ""
        val complexity = formatComplexity(finding)

        output.appendLine()
        output.appendLine("    $severityTag$confidenceTag$sep$ruleId$category$complexity")
        output.appendLine("    ${Ansi.cyan(formatLocation(finding.location.file, finding.location.line), color)}")
        output.appendLine()
        output.appendLine("      ${finding.message}")
        output.appendLine("      ${Ansi.green("\u2192 ${finding.suggestion}", color)}")
        snippetRenderer.render(finding.location.file, finding.location.line, finding.severity, output)
        formatEvidence(finding, snippetRenderer, output)
        val hash = Baseline.fingerprintOf(finding).contentHash
        output.appendLine("      ${Ansi.dim("# $hash", color)}")
    }

    private fun formatSeverityTag(severity: Severity): String =
        when (severity) {
            Severity.ERROR -> Ansi.bgRed(" error ", color)
            Severity.WARNING -> Ansi.bgYellow(" warning ", color)
            Severity.INFO -> Ansi.bgCyan(" info ", color)
        }

    private fun formatConfidenceTag(confidence: Confidence): String =
        when (confidence) {
            Confidence.HIGH -> " " + Ansi.green("\u2714 high confidence", color)
            Confidence.LOW -> " " + Ansi.dim("\u2022 low confidence", color)
            Confidence.MEDIUM -> ""
        }

    private fun formatComplexity(finding: Finding): String {
        if (finding.currentComplexity == null || finding.suggestedComplexity == null) return ""
        val sep = Ansi.dim(" \u00b7 ", color)
        return "$sep${Ansi.yellow("${finding.currentComplexity} \u2192 ${finding.suggestedComplexity}", color)}"
    }

    private fun formatEvidence(
        finding: Finding,
        snippetRenderer: SnippetRenderer,
        output: Appendable,
    ) {
        if (finding.evidence.isEmpty()) return
        for (evidence in finding.evidence) {
            val indent = "  ".repeat(evidence.depth)
            val connector = Ansi.dim("\u23bf  ", color)
            val loc = Ansi.dim(formatShortLocation(evidence.location.file, evidence.location.line), color)
            val cplx = evidence.complexity
            val complexityTag = if (cplx != null) " " + Ansi.yellow(cplx, color) else ""
            output.appendLine("      $indent$connector${evidence.label} $loc$complexityTag")
            if (evidence.location.file != finding.location.file) {
                snippetRenderer.render(evidence.location.file, evidence.location.line, finding.severity, output)
            }
        }
    }

    private fun relativize(absolutePath: String): String {
        if (baseDir == null) return absolutePath
        val prefix = if (baseDir.endsWith("/")) baseDir else "$baseDir/"
        return if (absolutePath.startsWith(prefix)) absolutePath.removePrefix(prefix) else absolutePath
    }

    private fun formatLocation(
        absolutePath: String,
        line: Int,
    ): String {
        val ext = absolutePath.substringAfterLast('.', "")
        if (ext in JVM_EXTENSIONS) {
            for (root in sourceRoots) {
                val rootPrefix = if (root.endsWith("/")) root else "$root/"
                if (absolutePath.startsWith(rootPrefix)) {
                    val relative = absolutePath.removePrefix(rootPrefix)
                    val withoutExt = relative.removeSuffix(".$ext")
                    val qualifiedName = withoutExt.replace('/', '.')
                    return "$qualifiedName:$line"
                }
            }
        }
        return "${relativize(absolutePath)}:$line"
    }

    /**
     * Shorter location for evidence lines: just filename + line.
     * The file header already provides full context.
     */
    private fun formatShortLocation(
        absolutePath: String,
        line: Int,
    ): String {
        val fileName = absolutePath.substringAfterLast('/')
        return "$fileName:$line"
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
        val cacheInfo = if (result.filesCached > 0) Ansi.dim(" (${result.filesCached} cached)", color) else ""
        val elapsedStr = String.format(Locale.US, "%.1f", result.elapsedMs / MS_PER_SECOND)
        val breakdown = formatSeverityBreakdown(result.findings)
        val hiddenSuffix = formatHiddenCounts(result)

        val scanned = "Scanned ${result.filesAnalyzed} files$cacheInfo in ${elapsedStr}s."
        val issueCount = result.findings.size
        val foundText = "Found $issueCount ${pluralize("issue", issueCount)}$breakdown"
        val acrossText = if (fileCount > 0) " across $fileCount ${pluralize("file", fileCount)}." else "."
        val errors = result.findings.count { it.severity == Severity.ERROR }
        val warnings = result.findings.count { it.severity == Severity.WARNING }

        val summaryColor =
            when {
                errors > 0 -> { text: String -> Ansi.red(text, color) }
                warnings > 0 -> { text: String -> Ansi.yellow(text, color) }
                issueCount == 0 -> { text: String -> Ansi.green(text, color) }
                else -> { text: String -> text }
            }

        output.appendLine("$scanned ${summaryColor("$foundText$acrossText")}$hiddenSuffix")
    }

    private fun formatSeverityBreakdown(findings: List<Finding>): String {
        val errors = findings.count { it.severity == Severity.ERROR }
        val warnings = findings.count { it.severity == Severity.WARNING }
        val infos = findings.count { it.severity == Severity.INFO }
        val parts = mutableListOf<String>()
        if (errors > 0) parts.add(Ansi.red("$errors ${pluralize("error", errors)}", color))
        if (warnings > 0) parts.add(Ansi.yellow("$warnings ${pluralize("warning", warnings)}", color))
        if (infos > 0) parts.add(Ansi.cyan("$infos info", color))
        return if (parts.isNotEmpty()) " (${parts.joinToString(", ")})" else ""
    }

    private fun formatHiddenCounts(result: AnalysisResult): String {
        val severityParts =
            Severity.entries.mapNotNull { severity ->
                val total = result.unfilteredCounts.getOrDefault(severity, 0)
                val shown = result.findings.count { it.severity == severity }
                val hidden = total - shown
                if (hidden > 0) "$hidden ${severity.name.lowercase()}" else null
            }
        val confidenceParts =
            Confidence.entries.mapNotNull { confidence ->
                val total = result.unfilteredConfidenceCounts.getOrDefault(confidence, 0)
                val shown = result.findings.count { it.confidence == confidence }
                val hidden = total - shown
                if (hidden > 0) "$hidden ${confidence.name.lowercase()}-confidence" else null
            }
        val parts = mutableListOf<String>()
        if (severityParts.isNotEmpty()) {
            parts.add("${severityParts.joinToString(", ")} hidden — use --severity info to show")
        }
        if (confidenceParts.isNotEmpty()) {
            parts.add("${confidenceParts.joinToString(", ")} hidden — use --confidence low to show")
        }
        return if (parts.isNotEmpty()) {
            " " + Ansi.dim("[${parts.joinToString("; ")}]", color)
        } else {
            ""
        }
    }

    private fun pluralize(
        word: String,
        count: Int,
    ): String = if (count == 1) word else "${word}s"

    private companion object {
        const val MS_PER_SECOND = 1000.0
        val JVM_EXTENSIONS = setOf("java", "kt", "kts", "groovy")
    }
}
