package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.engine.AnalysisError
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ConsoleReporterTest {
    private val reporter = ConsoleReporter(color = false)

    private fun finding(
        ruleId: String = "nested-lookup",
        file: String = "/src/Main.java",
        line: Int = 10,
        message: String = "Linear lookup inside loop",
        severity: Severity = Severity.WARNING,
    ) = Finding(
        ruleId = ruleId,
        ruleName = "Nested Lookup",
        severity = severity,
        location = SourceLocation(file, line, 1),
        message = message,
        suggestions = listOf(Suggestion.Freeform("Use a HashSet")),
    )

    private fun result(
        vararg findings: Finding,
        unfilteredCounts: Map<Severity, Int> = emptyMap(),
        unfilteredConfidenceCounts: Map<Confidence, Int> = emptyMap(),
    ) = AnalysisResult(
        findings = findings.toList(),
        filesAnalyzed = 1,
        errors = emptyList(),
        elapsedMs = 100,
        unfilteredCounts = unfilteredCounts,
        unfilteredConfidenceCounts = unfilteredConfidenceCounts,
    )

    @Nested
    inner class FingerprintDisplay {
        @Test
        fun `shows fingerprint hash for each finding`() {
            val f = finding()
            val output = StringBuilder()
            reporter.report(result(f), output)

            val hash = Baseline.fingerprintOf(f).contentHash
            output.toString() shouldContain "# $hash"
        }

        @Test
        fun `shows different hashes for different findings`() {
            val f1 = finding(ruleId = "nested-lookup", message = "First finding")
            val f2 = finding(ruleId = "sort-for-last", message = "Second finding")
            val output = StringBuilder()
            reporter.report(result(f1, f2), output)

            val hash1 = Baseline.fingerprintOf(f1).contentHash
            val hash2 = Baseline.fingerprintOf(f2).contentHash
            val text = output.toString()
            text shouldContain "# $hash1"
            text shouldContain "# $hash2"
        }
    }

    @Nested
    inner class FileGroupOrdering {
        @Test
        fun `files with higher severity findings appear first`() {
            val warningInFileA = finding(file = "/src/A.java", severity = Severity.WARNING)
            val errorInFileB = finding(file = "/src/B.java", severity = Severity.ERROR)
            val output = StringBuilder()
            reporter.report(result(warningInFileA, errorInFileB), output)

            val text = output.toString()
            val posB = text.indexOf("/src/B.java")
            val posA = text.indexOf("/src/A.java")
            assert(posB < posA) { "File with ERROR should appear before file with WARNING" }
        }

        @Test
        fun `files with same max severity are sorted alphabetically`() {
            val warningInZ = finding(file = "/src/Z.java", severity = Severity.WARNING)
            val warningInA = finding(file = "/src/A.java", severity = Severity.WARNING)
            val output = StringBuilder()
            reporter.report(result(warningInZ, warningInA), output)

            val text = output.toString()
            val posA = text.indexOf("/src/A.java")
            val posZ = text.indexOf("/src/Z.java")
            assert(posA < posZ) { "Files with same severity should be sorted alphabetically" }
        }

        @Test
        fun `findings within a file group maintain severity order`() {
            val error = finding(file = "/src/Main.java", line = 20, severity = Severity.ERROR)
            val warning = finding(file = "/src/Main.java", line = 10, severity = Severity.WARNING)
            val output = StringBuilder()
            // Engine sorts by severity desc, so ERROR comes first in the input
            reporter.report(result(error, warning), output)

            val text = output.toString()
            val posError = text.indexOf(" error ")
            val posWarning = text.indexOf(" warning ")
            assert(posError < posWarning) { "ERROR findings should appear before WARNING within same file" }
        }
    }

    @Nested
    inner class HiddenCounts {
        @Test
        fun `shows hidden info count when findings were filtered`() {
            val warning = finding(severity = Severity.WARNING)
            val output = StringBuilder()
            val r =
                result(
                    warning,
                    unfilteredCounts = mapOf(Severity.WARNING to 1, Severity.INFO to 3),
                )
            reporter.report(r, output)

            val text = output.toString()
            text shouldContain "3 info hidden"
            text shouldContain "--severity info to show"
        }

        @Test
        fun `no hidden suffix when all findings are shown`() {
            val warning = finding(severity = Severity.WARNING)
            val output = StringBuilder()
            val r =
                result(
                    warning,
                    unfilteredCounts = mapOf(Severity.WARNING to 1),
                )
            reporter.report(r, output)

            output.toString() shouldNotContain "hidden"
        }
    }

    @Nested
    inner class ConfidenceHiddenCounts {
        @Test
        fun `shows hidden confidence count when findings were filtered`() {
            val warning = finding(severity = Severity.WARNING)
            val output = StringBuilder()
            val r =
                result(
                    warning,
                    unfilteredConfidenceCounts = mapOf(Confidence.MEDIUM to 1, Confidence.LOW to 2),
                )
            reporter.report(r, output)

            val text = output.toString()
            text shouldContain "2 low-confidence hidden"
            text shouldContain "--confidence low to show"
        }

        @Test
        fun `no confidence hidden suffix when all confidence levels shown`() {
            val warning = finding(severity = Severity.WARNING)
            val output = StringBuilder()
            val r =
                result(
                    warning,
                    unfilteredConfidenceCounts = mapOf(Confidence.MEDIUM to 1),
                )
            reporter.report(r, output)

            output.toString() shouldNotContain "confidence hidden"
        }
    }

    @Nested
    inner class ParseFailures {
        @Test
        fun `shows parse failure count in summary`() {
            val r =
                AnalysisResult(
                    findings = emptyList(),
                    filesAnalyzed = 10,
                    errors =
                        listOf(
                            AnalysisError.Parse("Bad.kt", "syntax error"),
                            AnalysisError.Parse("Broken.kt", "unexpected token"),
                        ),
                    elapsedMs = 50,
                )
            val output = StringBuilder()
            reporter.report(r, output)
            output.toString() shouldContain "2 parse failures"
        }

        @Test
        fun `hides parse failure count when zero`() {
            val output = StringBuilder()
            reporter.report(result(), output)
            output.toString() shouldNotContain "parse failure"
        }
    }

    @Nested
    inner class EmptyResults {
        @Test
        fun `no findings shows summary only without hashes`() {
            val output = StringBuilder()
            reporter.report(result(), output)

            output.toString() shouldNotContain "# "
            output.toString() shouldContain "Found 0 issues"
        }
    }

    @Nested
    inner class SimplifyForDisplay {
        @Test
        fun `short names pass through unchanged`() {
            val result = ConsoleReporter.simplifyForDisplay("O(n × m)")
            assert(result == "O(n × m)") { "Expected O(n × m) but got $result" }
        }

        @Test
        fun `simple expressions unchanged`() {
            assert(ConsoleReporter.simplifyForDisplay("O(n log n)") == "O(n log n)")
            assert(ConsoleReporter.simplifyForDisplay("O(1)") == "O(1)")
            assert(ConsoleReporter.simplifyForDisplay("O(n)") == "O(n)")
        }

        @Test
        fun `parenthesized args are stripped`() {
            val result =
                ConsoleReporter.simplifyForDisplay(
                    "O(getOrDefault(type,taskMappers) × items)",
                )
            result shouldContain "getOrDefault"
            result shouldNotContain "type,taskMappers"
        }

        @Test
        fun `long dotted chains use last segment`() {
            val result =
                ConsoleReporter.simplifyForDisplay(
                    "O(com.example.service.InvoiceProcessor × n)",
                )
            result shouldContain "InvoiceProcessor"
            result shouldNotContain "com.example.service"
        }

        @Test
        fun `extreme length is truncated with ellipsis`() {
            val longName = "a".repeat(40)
            val result = ConsoleReporter.simplifyForDisplay("O($longName × n)")
            result shouldContain "\u2026"
        }

        @Test
        fun `non-O expression passes through as-is`() {
            val result = ConsoleReporter.simplifyForDisplay("2x lookup")
            assert(result == "2x lookup") { "Expected '2x lookup' but got $result" }
        }
    }

    @Nested
    inner class AcceptHint {
        @Test
        fun `first finding has accept hint`() {
            val f1 = finding(ruleId = "nested-lookup", message = "First")
            val f2 = finding(ruleId = "sort-for-last", message = "Second")
            val output = StringBuilder()
            reporter.report(result(f1, f2), output)

            val text = output.toString()
            val hash1 = Baseline.fingerprintOf(f1).contentHash
            text shouldContain "accept with --accept $hash1"
        }

        @Test
        fun `second finding does not have accept hint`() {
            val f1 = finding(ruleId = "nested-lookup", message = "First")
            val f2 = finding(ruleId = "sort-for-last", message = "Second")
            val output = StringBuilder()
            reporter.report(result(f1, f2), output)

            val text = output.toString()
            val hash2 = Baseline.fingerprintOf(f2).contentHash
            // hash2 should appear but without the accept hint
            text shouldContain "# $hash2"
            // Count occurrences of "accept with --accept" — should be exactly 1
            val acceptCount = "accept with --accept".toRegex().findAll(text).count()
            assert(acceptCount == 1) { "Expected exactly 1 accept hint but found $acceptCount" }
        }

        @Test
        fun `summary includes tip when findings exist`() {
            val output = StringBuilder()
            reporter.report(result(finding()), output)
            output.toString() shouldContain "Tip: Accept reviewed findings with --accept <hash>"
            output.toString() shouldContain "// algorilla:ignore"
        }

        @Test
        fun `summary does not include tip when no findings`() {
            val output = StringBuilder()
            reporter.report(result(), output)
            output.toString() shouldNotContain "Tip:"
        }
    }

    @Nested
    inner class ScanOverview {
        @Test
        fun `overview appears for more than 10 findings`() {
            val findings =
                (1..11).map { i ->
                    finding(
                        ruleId = if (i <= 6) "nested-lookup" else "sort-for-last",
                        file = "/src/File${i % 3}.java",
                        line = i * 10,
                        message = "Finding $i",
                    )
                }
            val output = StringBuilder()
            reporter.report(result(*findings.toTypedArray()), output)

            val text = output.toString()
            text shouldContain "Overview"
            text shouldContain "Top rules:"
            text shouldContain "Hotspot:"
        }

        @Test
        fun `overview does not appear for 10 or fewer findings`() {
            val findings =
                (1..10).map { i ->
                    finding(line = i * 10, message = "Finding $i")
                }
            val output = StringBuilder()
            reporter.report(result(*findings.toTypedArray()), output)

            output.toString() shouldNotContain "Overview"
        }
    }
}
