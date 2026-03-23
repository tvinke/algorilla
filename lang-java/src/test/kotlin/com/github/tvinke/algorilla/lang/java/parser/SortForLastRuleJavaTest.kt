package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.SortForLastRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class SortForLastRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = SortForLastRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect sort followed by findFirst`() {
            val findings = analyzeFixture("sort-for-last/positive/sort-then-first.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "sort-for-last"
            findings.first().currentComplexity shouldBe "O(n log n)"
            findings.first().suggestedComplexity shouldBe "O(n)"
        }
    }

    @Nested
    inner class DeduplicationCases {
        @Test
        fun `should produce one finding per sort even with multiple accesses`() {
            val findings = analyzeFixture("sort-for-last/positive/sort-then-multiple-access.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "sort-for-last"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag sort without first or last access`() {
            val findings = analyzeFixture("sort-for-last/negative/sort-then-iterate.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag sort followed by get with median index`() {
            val findings = analyzeFixture("sort-for-last/negative/sort-then-median.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag sort followed by get with arbitrary variable index`() {
            val findings = analyzeFixture("sort-for-last/negative/sort-then-arbitrary-index.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence chain for sort followed by findFirst`() {
            val findings = analyzeFixture("sort-for-last/positive/sort-then-first.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "sort"
            evidence[1].label shouldContain "sort is wasted"
            evidence[0].complexity shouldContain "O(n log n)"
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
