package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RegexRecompilationInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RegexRecompilationInLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = RegexRecompilationInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect matches and split inside loop`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/positive/matches-in-loop.java")

            findings shouldHaveSize 2
            findings.all { it.ruleId == "regex-recompilation-in-loop" } shouldBe true
            findings.any { it.message.contains("matches") } shouldBe true
            findings.any { it.message.contains("split") } shouldBe true
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag non-regex string methods`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/negative/no-regex-methods.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag split with single-char non-metachar argument`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/negative/single-char-split.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should still flag split with regex metachar argument`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/positive/regex-metachar-split.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "split"
        }

        @Test
        fun `should not flag Map replaceAll in loop`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/negative/map-replace-all.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence chain for regex in loop`() {
            val findings = analyzeFixture("regex-recompilation-in-loop/positive/matches-in-loop.java")

            findings shouldHaveSize 2
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].complexity shouldContain "O("
            evidence[1].label shouldContain "inside loop"
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
