package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.InLoopCollectionBuildingRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class InLoopCollectionBuildingRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = InLoopCollectionBuildingRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect addAll inside warehouse aggregation loop`() {
            val findings = analyzeFixture("in-loop-collection-building/positive/add-all-in-order-aggregation.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "in-loop-collection-building"
            findings.first().message shouldContain "addAll"
        }

        @Test
        fun `should detect putAll inside config merge loop`() {
            val findings = analyzeFixture("in-loop-collection-building/positive/put-all-in-config-merge.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "putAll"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag addAll called outside loop`() {
            val findings = analyzeFixture("in-loop-collection-building/negative/add-all-outside-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag single add in loop`() {
            val findings = analyzeFixture("in-loop-collection-building/negative/single-add-in-loop.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include loop and copy bottleneck evidence`() {
            val findings = analyzeFixture("in-loop-collection-building/positive/add-all-in-order-aggregation.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "for-each"
            evidence[1].label shouldContain "addAll"
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
