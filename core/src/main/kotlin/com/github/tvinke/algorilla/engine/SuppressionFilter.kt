package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.rules.Finding
import java.io.File

/**
 * Filters findings that are suppressed via inline comments.
 *
 * Supported patterns:
 * - `// algorilla:ignore` (suppresses all rules on that line)
 * - `// algorilla:ignore rule-name` (suppresses a specific rule)
 * - Block comments: `/* algorilla:ignore rule-name */`
 *
 * A suppression comment on the finding's line or the line immediately before it applies.
 * Rule aliases are also accepted so that old suppress comments keep working after renames.
 */
public class SuppressionFilter {
    /**
     * Returns findings that are not suppressed by inline comments.
     */
    @Suppress("UnusedParameter")
    public fun filter(
        findings: List<Finding>,
        irTrees: Map<String, FileRoot>,
        aliasIndex: Map<String, String> = emptyMap(),
    ): List<Finding> {
        val sourceLineCache = mutableMapOf<String, List<String>>()
        return findings.filter { finding ->
            val lines = sourceLineCache.getOrPut(finding.location.file) { readSourceLines(finding.location.file) }
            !isSuppressed(finding, lines, aliasIndex)
        }
    }

    private fun isSuppressed(
        finding: Finding,
        lines: List<String>,
        aliasIndex: Map<String, String>,
    ): Boolean {
        if (lines.isEmpty()) return false
        val lineIndices = mutableSetOf(finding.location.line - 1)
        finding.evidence.forEach { lineIndices.add(it.location.line - 1) }
        val linesToCheck = lineIndices.flatMap { idx -> listOfNotNull(lines.getOrNull(idx), lines.getOrNull(idx - 1)) }
        return linesToCheck.any { line -> matchesSuppression(line, finding.ruleId, finding.ruleName, aliasIndex) }
    }

    private fun readSourceLines(filePath: String): List<String> {
        val file = File(filePath)
        return if (file.exists()) file.readLines() else emptyList()
    }
}

private val SUPPRESSION_PATTERN = Regex("""algorilla:ignore\s*(\S+)?""")

private fun matchesSuppression(
    line: String,
    ruleId: String,
    ruleName: String,
    aliasIndex: Map<String, String>,
): Boolean {
    val match = SUPPRESSION_PATTERN.find(line) ?: return false
    val specifier = match.groupValues[1]
    if (specifier.isEmpty()) return true
    // Match by canonical ID, rule name, or resolved alias
    return specifier == ruleId || specifier == ruleName || aliasIndex[specifier] == ruleId
}
