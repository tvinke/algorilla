package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

internal class SarifReporterTest {
    private val reporter = SarifReporter()

    private fun finding(confidence: Confidence = Confidence.MEDIUM) =
        Finding(
            ruleId = "nested-lookup",
            ruleName = "Nested Lookup",
            severity = Severity.WARNING,
            confidence = confidence,
            location = SourceLocation("/src/Main.java", 10, 1),
            message = "Linear lookup inside loop",
            suggestion = "Use a HashSet",
        )

    private fun report(vararg findings: Finding): String {
        val result =
            AnalysisResult(
                findings = findings.toList(),
                filesAnalyzed = 1,
                errors = emptyList(),
                elapsedMs = 100,
            )
        val sb = StringBuilder()
        reporter.report(result, sb)
        return sb.toString()
    }

    @Test
    fun `SARIF output includes rank for confidence`() {
        val sarif = report(finding(Confidence.HIGH))
        sarif shouldContain "\"rank\""
        sarif shouldContain "90.0"
    }

    @Test
    fun `MEDIUM confidence maps to rank 50`() {
        val sarif = report(finding(Confidence.MEDIUM))
        sarif shouldContain "50.0"
    }

    @Test
    fun `LOW confidence maps to rank 10`() {
        val sarif = report(finding(Confidence.LOW))
        sarif shouldContain "10.0"
    }
}
