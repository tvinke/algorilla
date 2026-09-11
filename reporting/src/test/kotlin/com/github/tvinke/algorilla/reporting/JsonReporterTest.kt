package com.github.tvinke.algorilla.reporting

import com.github.tvinke.algorilla.engine.AnalysisResult
import com.github.tvinke.algorilla.model.GroupType
import com.github.tvinke.algorilla.model.GroupVisibility
import com.github.tvinke.algorilla.model.IssueGroup
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
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
        suggestions = listOf(Suggestion.Freeform("Use a HashSet")),
    )

    private fun report(
        findings: List<Finding> = listOf(finding()),
        issueGroups: List<IssueGroup> = emptyList(),
    ): String {
        val result =
            AnalysisResult(
                findings = findings,
                filesAnalyzed = 1,
                errors = emptyList(),
                elapsedMs = 100,
                issueGroups = issueGroups,
            )
        val sb = StringBuilder()
        reporter.report(result, sb)
        return sb.toString()
    }

    private fun issueGroup(
        anchor: String = "OrderService.process",
        findings: List<Finding> = listOf(finding()),
    ) = IssueGroup(
        id = "$anchor:same_method",
        anchor = anchor,
        groupType = GroupType.SAME_METHOD,
        visibility = GroupVisibility.UNKNOWN,
        pathContext = null,
        maxCardinality = null,
        representativeFinding = findings.first(),
        contributingFindings = findings,
    )

    @Nested
    inner class SchemaVersion {
        @Test
        fun `output includes schemaVersion field`() {
            val json = report()
            json shouldContain "\"schemaVersion\""
        }

        @Test
        fun `schemaVersion is 2`() {
            val json = report()
            val root = Json.parseToJsonElement(json).jsonObject
            root["schemaVersion"]!!.jsonPrimitive.int shouldBe 2
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
    inner class RuleUrl {
        @Test
        fun `output includes ruleUrl field`() {
            val json = report()
            json shouldContain "\"ruleUrl\""
            json shouldContain "https://tvinke.github.io/algorilla/rules/nested-lookup"
        }

        @Test
        fun `ruleUrl reflects the finding ruleId`() {
            val json = report(listOf(finding(ruleId = "io-in-loop")))
            json shouldContain "https://tvinke.github.io/algorilla/rules/io-in-loop"
        }
    }

    @Nested
    inner class Fingerprints {
        @Test
        fun `output includes fingerprint field for each finding`() {
            val json = report()
            json shouldContain "\"fingerprint\""
        }

        @Test
        fun `fingerprint is a non-empty hex string`() {
            val json = report()
            val root = Json.parseToJsonElement(json).jsonObject
            val findings = root["findings"]!!.jsonArray
            val fingerprint = findings[0].jsonObject["fingerprint"]!!.jsonPrimitive.content
            fingerprint.length shouldBe 16
            fingerprint.all { it in '0'..'9' || it in 'a'..'f' } shouldBe true
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
            root["schemaVersion"]!!.jsonPrimitive.int shouldBe 2
        }

        @Test
        fun `unknown fields in output can be ignored by consumers`() {
            val json = report()
            val root = lenientJson.parseToJsonElement(json).jsonObject
            root.containsKey("schemaVersion") shouldBe true
        }
    }

    @Nested
    inner class IssueGroups {
        @Test
        fun `output includes issueGroups array when groups present`() {
            val f = finding()
            val json = report(listOf(f), listOf(issueGroup(findings = listOf(f))))
            json shouldContain "\"issueGroups\""
            val root = Json.parseToJsonElement(json).jsonObject
            root["issueGroups"]!!.jsonArray.size shouldBe 1
        }

        @Test
        fun `issueGroups is empty array when no groups`() {
            val json = report()
            val root = Json.parseToJsonElement(json).jsonObject
            root["issueGroups"]!!.jsonArray.size shouldBe 0
        }

        @Test
        fun `issue group contains anchor and groupType`() {
            val f = finding()
            val json = report(listOf(f), listOf(issueGroup(findings = listOf(f))))
            val root = Json.parseToJsonElement(json).jsonObject
            val group = root["issueGroups"]!!.jsonArray[0].jsonObject
            group["anchor"]!!.jsonPrimitive.content shouldBe "OrderService.process"
            group["groupType"]!!.jsonPrimitive.content shouldBe "SAME_METHOD"
        }

        @Test
        fun `issue group contains findingCount`() {
            val findings = listOf(finding(ruleId = "io-in-loop"), finding(ruleId = "nested-lookup"))
            val json = report(findings, listOf(issueGroup(findings = findings)))
            val root = Json.parseToJsonElement(json).jsonObject
            val group = root["issueGroups"]!!.jsonArray[0].jsonObject
            group["findingCount"]!!.jsonPrimitive.int shouldBe 2
        }

        @Test
        fun `issue group references findings by fingerprint`() {
            val f = finding()
            val json = report(listOf(f), listOf(issueGroup(findings = listOf(f))))
            val root = Json.parseToJsonElement(json).jsonObject
            val group = root["issueGroups"]!!.jsonArray[0].jsonObject
            val contributingFingerprints = group["contributingFindings"]!!.jsonArray
            contributingFingerprints.size shouldBe 1
            val fingerprint = contributingFingerprints[0].jsonPrimitive.content
            fingerprint.length shouldBe 16
        }
    }
}
