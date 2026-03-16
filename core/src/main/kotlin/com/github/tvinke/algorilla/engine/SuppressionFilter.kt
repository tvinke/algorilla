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
 * - `// algorilla:ignore-file` (suppresses all rules for the entire file)
 * - `// algorilla:ignore-file rule-name` (suppresses a specific rule for the entire file)
 * - Block comments: `/* algorilla:ignore rule-name */`
 *
 * A suppression comment on the finding's line or the line immediately before it applies.
 * Rule aliases are also accepted so that old suppress comments keep working after renames.
 *
 * File-level suppression (`algorilla:ignore-file`) is checked in the first 5 lines of each file.
 */
public class SuppressionFilter {
    /**
     * Returns findings that are not suppressed by inline comments.
     */
    @Suppress("UnusedParameter") // irTrees kept in signature for future block-level suppression support
    public fun filter(
        findings: List<Finding>,
        irTrees: Map<String, FileRoot>,
        aliasIndex: Map<String, String> = emptyMap(),
    ): List<Finding> {
        val sourceLineCache = mutableMapOf<String, List<String>>()
        val fileSuppressCache = mutableMapOf<String, FileSuppression>()
        return findings.filter { finding ->
            !isSuppressed(finding, sourceLineCache, fileSuppressCache, aliasIndex)
        }
    }

    private fun isSuppressed(
        finding: Finding,
        sourceLineCache: MutableMap<String, List<String>>,
        fileSuppressCache: MutableMap<String, FileSuppression>,
        aliasIndex: Map<String, String>,
    ): Boolean {
        // Check file-level suppression first
        val mainLines = sourceLineCache.getOrPut(finding.location.file) { readSourceLines(finding.location.file) }
        val fileSuppression = fileSuppressCache.getOrPut(finding.location.file) { detectFileSuppression(mainLines) }
        if (isFileSuppressed(fileSuppression, finding.ruleId, finding.ruleName, aliasIndex)) return true

        // Group line indices by file (main finding + evidence from potentially different files)
        val linesByFile = mutableMapOf<String, MutableSet<Int>>()
        linesByFile.getOrPut(finding.location.file) { mutableSetOf() }.add(finding.location.line - 1)
        for (ev in finding.evidence) {
            linesByFile.getOrPut(ev.location.file) { mutableSetOf() }.add(ev.location.line - 1)
        }

        // Check each file's lines for suppression comments
        for ((file, indices) in linesByFile) {
            val lines = sourceLineCache.getOrPut(file) { readSourceLines(file) }
            if (lines.isEmpty()) continue

            // Also check file-level suppression for evidence files
            if (file != finding.location.file) {
                val evFileSuppression = fileSuppressCache.getOrPut(file) { detectFileSuppression(lines) }
                if (isFileSuppressed(evFileSuppression, finding.ruleId, finding.ruleName, aliasIndex)) return true
            }

            val linesToCheck =
                indices.flatMap { idx ->
                    listOfNotNull(lines.getOrNull(idx), lines.getOrNull(idx - 1))
                }
            if (linesToCheck.any { line -> matchesSuppression(line, finding.ruleId, finding.ruleName, aliasIndex) }) {
                return true
            }
        }
        return false
    }

    private fun readSourceLines(filePath: String): List<String> {
        val file = File(filePath)
        return if (file.exists()) file.readLines() else emptyList()
    }
}

private data class FileSuppression(
    val suppressAll: Boolean,
    val suppressedSpecifiers: List<String>,
)

private const val FILE_HEADER_LINES = 5

private val FILE_SUPPRESSION_PATTERN = Regex("""algorilla:ignore-file\s*([\w-]+)?""")

private fun detectFileSuppression(lines: List<String>): FileSuppression {
    val specifiers = mutableListOf<String>()
    var suppressAll = false
    for (i in 0 until minOf(FILE_HEADER_LINES, lines.size)) {
        val match = FILE_SUPPRESSION_PATTERN.find(lines[i]) ?: continue
        val specifier = match.groupValues[1]
        if (specifier.isEmpty()) {
            suppressAll = true
        } else {
            specifiers.add(specifier)
        }
    }
    return FileSuppression(suppressAll, specifiers)
}

private fun isFileSuppressed(
    suppression: FileSuppression,
    ruleId: String,
    ruleName: String,
    aliasIndex: Map<String, String>,
): Boolean {
    if (suppression.suppressAll) return true
    return suppression.suppressedSpecifiers.any { specifier ->
        specifier == ruleId || specifier == ruleName || aliasIndex[specifier] == ruleId
    }
}

private val SUPPRESSION_PATTERN = Regex("""algorilla:ignore(?!-file)\s*([\w-]+)?""")

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
