package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText

internal class AnalysisEngineTest {
    @TempDir
    lateinit var tempDir: Path

    private fun createTempFile(name: String): String {
        val file = tempDir.resolve(name)
        file.writeText("class Dummy {}")
        return file.toString()
    }

    private fun stubParser(filePath: String) =
        object : LanguageParser {
            override val language = Language.JAVA

            override fun canParse(filePath: String) = true

            override fun parse(filePath: String) =
                FileRoot(
                    filePath = filePath,
                    language = Language.JAVA,
                    location = SourceLocation(filePath, 1, 1),
                    children = emptyList(),
                )
        }

    private fun stubRule(
        findings: List<Finding>,
        ceiling: Confidence = Confidence.HIGH,
    ) = object : Rule {
        override val id = "test-rule"
        override val name = "Test Rule"
        override val severity = Severity.WARNING
        override val defaultConfidence = ceiling
        override val languages = Language.entries.toSet()
        override val category = RuleCategory.LOOP_AMPLIFIER
        override val subsumes = emptySet<String>()

        override fun evaluate(context: AnalysisContext) = findings
    }

    @Nested
    inner class SeverityFiltering {
        @Test
        fun `default minSeverity WARNING excludes INFO findings`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.INFO),
                    finding(file, Severity.WARNING),
                    finding(file, Severity.ERROR),
                )
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings)),
                    config = AnalysisConfig(minSeverity = Severity.WARNING),
                )

            val result = engine.analyze(listOf(file))

            result.findings shouldHaveSize 2
            result.findings.none { it.severity == Severity.INFO } shouldBe true
            result.unfilteredCounts[Severity.INFO] shouldBe 1
            result.unfilteredCounts[Severity.WARNING] shouldBe 1
            result.unfilteredCounts[Severity.ERROR] shouldBe 1
        }

        @Test
        fun `minSeverity INFO includes all findings`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.INFO),
                    finding(file, Severity.WARNING),
                    finding(file, Severity.ERROR),
                )
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings)),
                    config = AnalysisConfig(minSeverity = Severity.INFO),
                )

            val result = engine.analyze(listOf(file))

            result.findings shouldHaveSize 3
        }

        @Test
        fun `minSeverity ERROR excludes WARNING and INFO`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.INFO),
                    finding(file, Severity.WARNING),
                    finding(file, Severity.ERROR),
                )
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings)),
                    config = AnalysisConfig(minSeverity = Severity.ERROR),
                )

            val result = engine.analyze(listOf(file))

            result.findings shouldHaveSize 1
            result.findings.first().severity shouldBe Severity.ERROR
        }
    }

    @Nested
    inner class ConfidenceFiltering {
        @Test
        fun `default minConfidence MEDIUM excludes LOW findings`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.WARNING, Confidence.LOW),
                    finding(file, Severity.WARNING, Confidence.MEDIUM),
                    finding(file, Severity.WARNING, Confidence.HIGH),
                )
            // Use MEDIUM ceiling so MEDIUM findings stay MEDIUM (no promotion)
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings, ceiling = Confidence.MEDIUM)),
                    config = AnalysisConfig(minSeverity = Severity.INFO, minConfidence = Confidence.MEDIUM),
                )

            val result = engine.analyze(listOf(file))

            // HIGH gets capped to MEDIUM (ceiling), LOW filtered out
            result.findings shouldHaveSize 2
            result.findings.none { it.confidence == Confidence.LOW } shouldBe true
        }

        @Test
        fun `minConfidence HIGH excludes MEDIUM and LOW`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.WARNING, Confidence.LOW),
                    finding(file, Severity.WARNING, Confidence.MEDIUM),
                    finding(file, Severity.WARNING, Confidence.HIGH),
                )
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings)),
                    config = AnalysisConfig(minSeverity = Severity.INFO, minConfidence = Confidence.HIGH),
                )

            val result = engine.analyze(listOf(file))

            // MEDIUM promoted to HIGH (ceiling=HIGH), LOW stays LOW, HIGH stays HIGH
            // So 2 findings pass HIGH filter (original HIGH + promoted MEDIUM)
            result.findings shouldHaveSize 2
            result.findings.all { it.confidence == Confidence.HIGH } shouldBe true
        }

        @Test
        fun `minConfidence LOW includes all findings`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.WARNING, Confidence.LOW),
                    finding(file, Severity.WARNING, Confidence.MEDIUM),
                    finding(file, Severity.WARNING, Confidence.HIGH),
                )
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings)),
                    config = AnalysisConfig(minSeverity = Severity.INFO, minConfidence = Confidence.LOW),
                )

            val result = engine.analyze(listOf(file))

            result.findings shouldHaveSize 3
        }

        @Test
        fun `confidence and severity filters compose`() {
            val file = createTempFile("Test.java")
            val findings =
                listOf(
                    finding(file, Severity.INFO, Confidence.HIGH),
                    finding(file, Severity.WARNING, Confidence.LOW),
                    finding(file, Severity.WARNING, Confidence.HIGH),
                )
            val engine =
                AnalysisEngine(
                    parsers = listOf(stubParser(file)),
                    rules = listOf(stubRule(findings)),
                    config = AnalysisConfig(minSeverity = Severity.WARNING, minConfidence = Confidence.HIGH),
                )

            val result = engine.analyze(listOf(file))

            result.findings shouldHaveSize 1
            result.findings.first().severity shouldBe Severity.WARNING
            result.findings.first().confidence shouldBe Confidence.HIGH
        }
    }

    private var findingCounter = 0

    private fun finding(
        file: String,
        severity: Severity,
        confidence: Confidence = Confidence.MEDIUM,
    ) = Finding(
        ruleId = "test-rule",
        ruleName = "Test Rule",
        severity = severity,
        confidence = confidence,
        location = SourceLocation(file, 10 + findingCounter, 1),
        message = "Test finding ${++findingCounter}",
        suggestions = listOf(Suggestion.Freeform("Fix it")),
    )
}
