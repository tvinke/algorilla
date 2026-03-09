package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.model.Severity
import java.io.File

/**
 * Reads source files and renders code snippets with severity-colored highlighting
 * for console output. Caches file contents to avoid re-reading.
 */
internal class SnippetRenderer(
    private val color: Boolean,
    private val contextLines: Int = 1,
) {
    private val fileCache = mutableMapOf<String, List<String>>()

    fun render(
        filePath: String,
        line: Int,
        severity: Severity,
        output: Appendable,
    ) {
        val lines = readLines(filePath) ?: return
        if (line < 1 || line > lines.size) return

        val funcLine = findEnclosingFunction(lines, line)
        val startLine = (line - contextLines).coerceAtLeast(1)
        val endLine = (line + contextLines).coerceAtMost(lines.size)
        val showFuncSeparately = funcLine != null && funcLine.first < startLine
        val contextSources = (startLine..endLine).map { lines[it - 1] }
        val commonIndent = computeCommonIndent(contextSources)
        val allLineNums = if (showFuncSeparately) listOf(funcLine!!.first) + (startLine..endLine) else (startLine..endLine).toList()
        val gutterWidth = allLineNums.max().toString().length

        output.appendLine()
        renderFuncHeader(funcLine, showFuncSeparately, gutterWidth, output)
        renderContextLines(lines, startLine, endLine, line, showFuncSeparately, commonIndent, gutterWidth, severity, output)
        output.appendLine()
    }

    private fun renderFuncHeader(
        funcLine: Pair<Int, String>?,
        showFuncSeparately: Boolean,
        gutterWidth: Int,
        output: Appendable,
    ) {
        if (!showFuncSeparately || funcLine == null) return
        renderGutterLine(funcLine.first, funcLine.second.trimStart(), gutterWidth, output)
        output.appendLine(Ansi.dim("$INDENT${" ".repeat(gutterWidth)} \u2502", color))
    }

    @Suppress("LongParameterList")
    private fun renderContextLines(
        lines: List<String>,
        startLine: Int,
        endLine: Int,
        highlightLine: Int,
        skipLeadingBlanks: Boolean,
        commonIndent: Int,
        gutterWidth: Int,
        severity: Severity,
        output: Appendable,
    ) {
        var skipping = skipLeadingBlanks
        for (i in startLine..endLine) {
            val sourceLine = lines[i - 1]
            if (skipping && sourceLine.isBlank() && i != highlightLine) continue
            skipping = false
            renderGutterLine(i, sourceLine.dedent(commonIndent), gutterWidth, output, highlight = i == highlightLine, severity = severity)
        }
    }

    private fun renderGutterLine(
        lineNum: Int,
        text: String,
        gutterWidth: Int,
        output: Appendable,
        highlight: Boolean = false,
        severity: Severity = Severity.INFO,
    ) {
        val gutter = Ansi.dim("${lineNum.toString().padStart(gutterWidth)} \u2502 ", color)
        val content =
            if (highlight) {
                highlightForSeverity(text, severity)
            } else {
                Ansi.dim(text, color)
            }
        output.appendLine("$INDENT$gutter$content")
    }

    private fun highlightForSeverity(
        text: String,
        severity: Severity,
    ): String =
        when (severity) {
            Severity.ERROR -> Ansi.red(text, color)
            Severity.WARNING -> Ansi.yellow(text, color)
            Severity.INFO -> Ansi.boldWhite(text, color)
        }

    private fun readLines(filePath: String): List<String>? =
        fileCache.getOrPut(filePath) {
            val file = File(filePath)
            if (!file.isFile) return null
            file.readLines()
        }

    companion object {
        private const val INDENT = "          "

        private val FUNC_PATTERN =
            Regex(
                """^\s*((?:public|private|protected|internal|static|final|abstract|override|suspend|fun|def|async|function|export)\s+)""",
            )

        internal fun findEnclosingFunction(
            lines: List<String>,
            findingLine: Int,
        ): Pair<Int, String>? {
            for (i in (findingLine - 2) downTo 0) {
                val src = lines[i]
                if (FUNC_PATTERN.containsMatchIn(src)) {
                    return Pair(i + 1, src)
                }
            }
            return null
        }

        private fun computeCommonIndent(lines: List<String>): Int =
            lines
                .filter { it.isNotBlank() }
                .minOfOrNull { it.length - it.trimStart().length }
                ?: 0

        private fun String.dedent(amount: Int): String = if (amount > 0 && length >= amount) substring(amount) else this
    }
}
