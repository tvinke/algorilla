package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.ExpensiveSortComparatorRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class ExpensiveSortComparatorRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = ExpensiveSortComparatorRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect lookup inside sort comparator`() {
            val findings = analyzeFixture("expensive-sort-comparator/positive/lookup-in-comparator.java")

            findings.size shouldBe 2
            findings.all { it.ruleId == "expensive-sort-comparator" } shouldBe true
            findings.first().currentComplexity shouldBe "O(n\u00b2 log n)"
            findings.first().suggestedComplexity shouldBe "O(n log n)"
        }

        @Test
        fun `should detect Date creation inside sort comparator`() {
            val findings = analyzeFixture("date-in-sort/positive/date-creation-in-comparator.java")

            findings.size shouldBe 2
            findings.all { it.ruleId == "expensive-sort-comparator" } shouldBe true
            findings.first().message shouldContain "Date"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag simple comparator`() {
            val findings = analyzeFixture("expensive-sort-comparator/negative/simple-comparator.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag comparator without date creation`() {
            val findings = analyzeFixture("date-in-sort/negative/no-date-in-comparator.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence chain for lookup in comparator`() {
            val findings = analyzeFixture("expensive-sort-comparator/positive/lookup-in-comparator.java")

            findings shouldHaveSize 2
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "sort"
            evidence[0].label shouldContain "comparator"
            evidence[1].label shouldContain "inside comparator"
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
