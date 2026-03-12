package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
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

    private fun stubRule(findings: List<Finding>) =
        object : Rule {
            override val id = "test-rule"
            override val name = "Test Rule"
            override val severity = Severity.WARNING
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

    private fun finding(
        file: String,
        severity: Severity,
    ) = Finding(
        ruleId = "test-rule",
        ruleName = "Test Rule",
        severity = severity,
        location = SourceLocation(file, 10, 1),
        message = "Test finding",
        suggestion = "Fix it",
    )
}
