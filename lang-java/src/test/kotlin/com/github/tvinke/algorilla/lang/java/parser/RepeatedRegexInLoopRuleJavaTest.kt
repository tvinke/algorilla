package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RepeatedRegexInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RepeatedRegexInLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = RepeatedRegexInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect Pattern compile inside order validation loop`() {
            val findings = analyzeFixture("repeated-regex-in-loop/positive/pattern-compile-in-order-loop.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "repeated-regex-in-loop"
            findings.first().message shouldContain "Pattern"
            findings.first().message shouldContain "compile"
        }

        @Test
        fun `should detect Pattern compile inside product filtering loop`() {
            val findings = analyzeFixture("repeated-regex-in-loop/positive/new-regex-in-product-filter.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "compile"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag pattern compiled once outside loop`() {
            val findings = analyzeFixture("repeated-regex-in-loop/negative/pattern-compiled-outside-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag pattern compiled with variable argument`() {
            val findings = analyzeFixture("repeated-regex-in-loop/negative/pattern-compile-with-variable-arg.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include loop and bottleneck evidence`() {
            val findings = analyzeFixture("repeated-regex-in-loop/positive/pattern-compile-in-order-loop.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "for-each"
            evidence[1].label shouldContain "compile"
            evidence[1].complexity shouldContain "bottleneck"
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
