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
    ) = Finding(
        ruleId = ruleId,
        ruleName = "Nested Lookup",
        severity = Severity.WARNING,
        location = SourceLocation(file, line, 1),
        message = message,
        suggestion = "Use a HashSet",
    )

    private fun result(vararg findings: Finding) =
        AnalysisResult(
            findings = findings.toList(),
            filesAnalyzed = 1,
            errors = emptyList(),
            elapsedMs = 100,
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
