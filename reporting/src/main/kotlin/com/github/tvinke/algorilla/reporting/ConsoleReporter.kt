package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.engine.AnalysisError
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
@Suppress("LargeClass") // Console output logic — each section is a method
public class ConsoleReporter(
    private val color: Boolean = false,
    private val baseDir: String? = null,
    private val sourceRoots: List<String> = emptyList(),
    private val limit: Int = 0,
) : Reporter {
    private var firstFindingShown = false
    private var projectRoot: java.io.File? = null

    @Suppress("LongMethod")
    override fun report(
        result: AnalysisResult,
        output: Appendable,
    ) {
        if (result.findings.isEmpty()) {
            formatSummary(result, output)
            return
        }

        firstFindingShown = false
        projectRoot = result.projectRoot

        val totalFindings = result.findings.size
        val isLimited = limit > 0 && limit < totalFindings

        if (!isLimited && totalFindings > OVERVIEW_THRESHOLD) {
            formatOverview(result, output)
        }

        val displayFindings = if (isLimited) result.findings.take(limit) else result.findings

        val snippetRenderer = SnippetRenderer(color)
        val grouped =
            displayFindings
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

        if (isLimited) {
            output.appendLine("Showing $limit of $totalFindings findings. Run without --limit to see all.")
        }
        formatSummary(result, output, isLimited)
    }

    @Suppress("LongMethod")
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
        finding.suggestedCode?.let { sc ->
            val fw = sc.framework?.let { " ($it)" } ?: ""
            output.appendLine(Ansi.dim("      $fw", color))
            for (codeLine in sc.code.lines()) {
                output.appendLine(Ansi.dim("        $codeLine", color))
            }
        }
        output.appendLine("      ${Ansi.dim("\u2197 ${ReporterConstants.ruleUrl(finding.ruleId)}", color)}")
        snippetRenderer.render(finding.location.file, finding.location.line, finding.severity, output)
        formatEvidence(finding, snippetRenderer, output)
        val hash = Baseline.fingerprintOf(finding, projectRoot).contentHash
        if (!firstFindingShown) {
            output.appendLine(
                "      ${Ansi.dim("# $hash  \u2190 accept with --accept $hash", color)}",
            )
            firstFindingShown = true
        } else {
            output.appendLine("      ${Ansi.dim("# $hash", color)}")
        }
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
        val currentRaw = finding.currentComplexity ?: return ""
        val suggestedRaw = finding.suggestedComplexity ?: return ""
        val sep = Ansi.dim(" \u00b7 ", color)
        val current = simplifyForDisplay(currentRaw)
        val suggested = simplifyForDisplay(suggestedRaw)
        return "$sep${Ansi.yellow("$current \u2192 $suggested", color)}"
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

    @Suppress("LongMethod")
    private fun formatSummary(
        result: AnalysisResult,
        output: Appendable,
        isLimited: Boolean = false,
    ) {
        val fileCount =
            result.findings
                .map { it.location.file }
                .toSet()
                .size
        val cacheInfo = if (result.filesCached > 0) Ansi.dim(" (${result.filesCached} cached)", color) else ""
        val parseFailures = result.errors.count { it is AnalysisError.Parse }
        val parseInfo =
            if (parseFailures > 0) {
                Ansi.dim(" ($parseFailures parse ${pluralize("failure", parseFailures)})", color)
            } else {
                ""
            }
        val elapsedStr = String.format(Locale.US, "%.1f", result.elapsedMs / MS_PER_SECOND)
        val breakdown = formatSeverityBreakdown(result.findings)
        val hiddenSuffix = formatHiddenCounts(result)

        val scanned = "Scanned ${result.filesAnalyzed} files$cacheInfo$parseInfo in ${elapsedStr}s."
        val issueCount = result.findings.size
        val newLabel = if (result.baselinedCount > 0) "new " else ""
        val baselineText =
            if (result.baselinedCount > 0) " (${result.baselinedCount} baselined)" else ""
        val foundText = "Found $issueCount ${newLabel}${pluralize("issue", issueCount)}$breakdown$baselineText"
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

        output.appendLine("$scanned ${summaryColor("$foundText$acrossText")}")
        if (result.acceptedCount > 0) {
            output.appendLine(
                Ansi.dim(
                    "  ${result.acceptedCount} accepted (reviewed) \u2014 see .algorilla/ignore-list.json",
                    color,
                ),
            )
        }
        if (hiddenSuffix.isNotEmpty()) {
            output.appendLine(hiddenSuffix)
        }
        if (result.findings.isNotEmpty() && !isLimited) {
            output.appendLine(
                Ansi.dim(
                    "Tip: --accept <hash> to mark reviewed, // algorilla:ignore to suppress in code",
                    color,
                ),
            )
        }
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
        // Both unfiltered maps count from the same pool before filtering.
        // Use the larger total to get the true pre-filter count.
        val totalUnfiltered =
            maxOf(
                result.unfilteredCounts.values.sum(),
                result.unfilteredConfidenceCounts.values.sum(),
            )
        val totalShown = result.findings.size
        val totalHidden = totalUnfiltered - totalShown
        if (totalHidden <= 0) return ""

        // Determine which filters contributed by checking what each removed
        val hiddenBySeverity =
            Severity.entries.sumOf { severity ->
                val total = result.unfilteredCounts.getOrDefault(severity, 0)
                val shown = result.findings.count { it.severity == severity }
                total - shown
            }
        val hiddenByConfidence =
            Confidence.entries.sumOf { confidence ->
                val total = result.unfilteredConfidenceCounts.getOrDefault(confidence, 0)
                val shown = result.findings.count { it.confidence == confidence }
                total - shown
            }

        val hints = mutableListOf<String>()
        if (hiddenBySeverity > 0) hints.add("--severity info")
        if (hiddenByConfidence > 0) hints.add("--confidence low")

        val hint = if (hints.isNotEmpty()) " \u2014 use ${hints.joinToString(" or ")} to show" else ""
        return Ansi.dim("  $totalHidden hidden by filters$hint", color)
    }

    @Suppress("LongMethod")
    private fun formatOverview(
        result: AnalysisResult,
        output: Appendable,
    ) {
        val line = "\u2500".repeat(OVERVIEW_LINE_WIDTH)
        output.appendLine(Ansi.dim("\u2500\u2500 Overview $line", color))

        // Severity counts
        val severityCounts =
            Severity.entries.mapNotNull { sev ->
                val count = result.findings.count { it.severity == sev }
                if (count > 0) "$count ${sev.name.lowercase()}" else null
            }
        val fileCount =
            result.findings
                .map { it.location.file }
                .toSet()
                .size
        output.appendLine(
            "  ${severityCounts.joinToString(" \u00b7 ")} across $fileCount ${pluralize("file", fileCount)}",
        )

        // Top 3 rules
        val topRules =
            result.findings
                .groupingBy { it.ruleId }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(TOP_RULES_COUNT)
                .joinToString(" \u00b7 ") { "${it.key} (${it.value})" }
        output.appendLine("  Top rules: $topRules")

        // Hotspot file
        val hotspot =
            result.findings
                .groupingBy { it.location.file }
                .eachCount()
                .maxByOrNull { it.value }
        if (hotspot != null) {
            val fileName = hotspot.key.substringAfterLast('/')
            output.appendLine("  Hotspot: $fileName (${hotspot.value} ${pluralize("finding", hotspot.value)})")
        }

        // Dominant rule note: when a single INFO rule accounts for >30% of findings
        val ruleCounts = result.findings.groupingBy { it.ruleId to it.severity }.eachCount()
        val totalFindings = result.findings.size
        val dominantInfo =
            ruleCounts.entries
                .filter { it.key.second == Severity.INFO }
                .maxByOrNull { it.value }
        if (dominantInfo != null && dominantInfo.value > totalFindings * DOMINANT_RULE_THRESHOLD) {
            output.appendLine(
                Ansi.dim(
                    "  Note: ${dominantInfo.key.first} accounts for ${dominantInfo.value} findings — " +
                        "use --severity warning to focus on higher-impact issues",
                    color,
                ),
            )
        }

        output.appendLine(Ansi.dim(line + "\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500", color))
        output.appendLine()
        output.appendLine(
            Ansi.dim("Tip: Use --limit 5 to focus on the top findings first", color),
        )
        output.appendLine()
    }

    private fun pluralize(
        word: String,
        count: Int,
    ): String = if (count == 1) word else "${word}s"

    internal companion object {
        const val MS_PER_SECOND = 1000.0
        const val OVERVIEW_THRESHOLD = 10
        private const val OVERVIEW_LINE_WIDTH = 32
        private const val TOP_RULES_COUNT = 3
        private const val DOMINANT_RULE_THRESHOLD = 0.3
        private const val MAX_SEGMENT_LENGTH = 25
        private const val TRUNCATED_LENGTH = 22
        val JVM_EXTENSIONS = setOf("java", "kt", "kts", "groovy")

        private val OPERATOR_PATTERN = Regex("""(\s*[×+^]\s*|\s+log\s+)""")
        private val PARENS_PATTERN = Regex("""\([^)]*\)""")

        @JvmStatic
        internal fun simplifyForDisplay(expr: String): String {
            // Handle O(...) wrapper
            val inner =
                if (expr.startsWith("O(") && expr.endsWith(")")) {
                    expr.substring(2, expr.length - 1)
                } else {
                    return expr
                }

            // Split on operators, preserving them
            val parts = OPERATOR_PATTERN.split(inner)
            val operators = OPERATOR_PATTERN.findAll(inner).map { it.value }.toList()

            val simplified =
                parts.mapIndexed { index, segment ->
                    val s = simplifySegment(segment.trim())
                    if (index < operators.size) s + operators[index] else s
                }

            return "O(${simplified.joinToString("")})"
        }

        private fun simplifySegment(segment: String): String {
            if (segment.length <= MAX_SEGMENT_LENGTH) return segment

            // Strip parenthesized argument lists
            var result = PARENS_PATTERN.replace(segment, "")
            if (result.length <= MAX_SEGMENT_LENGTH) return result

            // Take last segment after final dot
            if ('.' in result) {
                result = result.substringAfterLast('.')
            }
            if (result.length <= MAX_SEGMENT_LENGTH) return result

            // Truncate
            return result.take(TRUNCATED_LENGTH) + "\u2026"
        }
    }
}
