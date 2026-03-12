package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.baseline.Baseline
import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
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
        suggestion = "Use a HashSet",
    )

    private fun result(
        vararg findings: Finding,
        unfilteredCounts: Map<Severity, Int> = emptyMap(),
    ) = AnalysisResult(
        findings = findings.toList(),
        filesAnalyzed = 1,
        errors = emptyList(),
        elapsedMs = 100,
        unfilteredCounts = unfilteredCounts,
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
    inner class EmptyResults {
        @Test
        fun `no findings shows summary only without hashes`() {
            val output = StringBuilder()
            reporter.report(result(), output)

            output.toString() shouldNotContain "# "
            output.toString() shouldContain "Found 0 issues"
        }
    }
}
