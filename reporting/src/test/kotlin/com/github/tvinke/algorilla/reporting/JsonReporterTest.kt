package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class JsonReporterTest {
    private val reporter = JsonReporter()
    private val lenientJson = Json { ignoreUnknownKeys = true }

    private fun finding(
        ruleId: String = "nested-lookup",
        severity: Severity = Severity.WARNING,
        message: String = "Linear lookup inside loop",
    ) = Finding(
        ruleId = ruleId,
        ruleName = "Nested Lookup",
        severity = severity,
        location = SourceLocation("/src/Main.java", 10, 1),
        message = message,
        suggestion = "Use a HashSet",
    )

    private fun report(findings: List<Finding> = listOf(finding())): String {
        val result =
            AnalysisResult(
                findings = findings,
                filesAnalyzed = 1,
                errors = emptyList(),
                elapsedMs = 100,
            )
        val sb = StringBuilder()
        reporter.report(result, sb)
        return sb.toString()
    }

    @Nested
    inner class SchemaVersion {
        @Test
        fun `output includes schemaVersion field`() {
            val json = report()
            json shouldContain "\"schemaVersion\""
        }

        @Test
        fun `schemaVersion is 1`() {
            val json = report()
            val root = Json.parseToJsonElement(json).jsonObject
            root["schemaVersion"]!!.jsonPrimitive.int shouldBe 1
        }

        @Test
        fun `output includes algorillaVersion field`() {
            val json = report()
            json shouldContain "\"algorillaVersion\""
        }
    }

    @Nested
    inner class ConfidenceField {
        @Test
        fun `output includes confidence field`() {
            val json = report()
            json shouldContain "\"confidence\""
            json shouldContain "\"MEDIUM\""
        }
    }

    @Nested
    inner class Structure {
        @Test
        fun `output includes summary`() {
            val json = report()
            json shouldContain "\"summary\""
            json shouldContain "\"totalFindings\""
        }

        @Test
        fun `output includes findings array`() {
            val json = report()
            json shouldContain "\"findings\""
        }

        @Test
        fun `empty findings produces valid JSON`() {
            val json = report(emptyList())
            val root = Json.parseToJsonElement(json).jsonObject
            root["schemaVersion"]!!.jsonPrimitive.int shouldBe 1
        }

        @Test
        fun `unknown fields in output can be ignored by consumers`() {
            val json = report()
            val root = lenientJson.parseToJsonElement(json).jsonObject
            root.containsKey("schemaVersion") shouldBe true
        }
    }
}
