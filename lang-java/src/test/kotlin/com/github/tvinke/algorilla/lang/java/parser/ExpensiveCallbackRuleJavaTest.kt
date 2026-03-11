package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.ExpensiveCallbackRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class ExpensiveCallbackRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = ExpensiveCallbackRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect regex compilation inside filter callback`() {
            val findings = analyzeFixture("expensive-callback/positive/regex-in-filter.java")

            findings.any { it.ruleId == "expensive-callback" && it.message.contains("Regex compilation") } shouldBe true
        }

        @Test
        fun `should detect linear lookup inside map callback`() {
            val findings = analyzeFixture("expensive-callback/positive/lookup-in-map.java")

            findings.any { it.ruleId == "expensive-callback" && it.message.contains("contains") } shouldBe true
        }

        @Test
        fun `should detect nested iteration inside reduce callback`() {
            val findings = analyzeFixture("expensive-callback/positive/nested-iteration-in-reduce.java")

            findings.any { it.ruleId == "expensive-callback" && it.message.contains("filter") } shouldBe true
        }

        @Test
        fun `should detect date parsing inside forEach callback`() {
            val findings = analyzeFixture("expensive-callback/positive/date-in-foreach.java")

            findings.any { it.ruleId == "expensive-callback" } shouldBe true
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag simple callback without expensive operations`() {
            val findings = analyzeFixture("expensive-callback/negative/simple-callback.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class Aliases {
        @Test
        fun `should have date-in-callback alias`() {
            rule.aliases shouldBe listOf("date-in-callback")
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence chain for regex in filter callback`() {
            val findings = analyzeFixture("expensive-callback/positive/regex-in-filter.java")

            val finding = findings.first { it.ruleId == "expensive-callback" }
            finding.evidence.size shouldBe 2
            finding.evidence[0].label shouldContain "filter"
            finding.evidence[1].label shouldContain "inside callback"
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
