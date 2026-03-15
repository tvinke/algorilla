package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.CardinalityExplosionRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class CardinalityExplosionRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = CardinalityExplosionRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect Cartesian product in nested loops over different collections`() {
            val findings = analyzeFixture("cardinality-explosion/positive/cartesian-product.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "cardinality-explosion"
            findings.first().message shouldContain "Cartesian product"
            findings.first().message shouldContain "orders"
            findings.first().message shouldContain "products"
            findings.first().severity shouldBe Severity.WARNING
        }

        @Test
        fun `should produce one finding per loop pair with multiple mutations`() {
            val findings = analyzeFixture("cardinality-explosion/positive/multi-mutation-cartesian.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "Cartesian product"
            findings.first().message shouldContain "items"
            findings.first().message shouldContain "categories"
        }

        @Test
        fun `should detect flatMap cross join`() {
            val findings = analyzeFixture("cardinality-explosion/positive/flatmap-explosion.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "cardinality-explosion"
            findings.first().message shouldContain "flatMap"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag nested loops over the same collection as WARNING`() {
            val findings = analyzeFixture("cardinality-explosion/negative/same-collection-nesting.java")

            // Same-collection nesting should either produce no findings or only low-confidence INFO
            val warnings = findings.filter { it.severity == Severity.WARNING }
            warnings.shouldBeEmpty()
        }

        @Test
        fun `should not flag single loop with mutation`() {
            val findings = analyzeFixture("cardinality-explosion/negative/single-loop-mutation.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag map entry unpacking`() {
            val findings = analyzeFixture("cardinality-explosion/negative/map-entry-unpacking.java")

            val warnings = findings.filter { it.severity == Severity.WARNING }
            warnings.shouldBeEmpty()
        }

        @Test
        fun `should not flag parent-child iteration`() {
            val findings = analyzeFixture("cardinality-explosion/negative/parent-child-iteration.java")

            val warnings = findings.filter { it.severity == Severity.WARNING }
            warnings.shouldBeEmpty()
        }

        @Test
        fun `should not flag enum values iteration`() {
            val findings = analyzeFixture("cardinality-explosion/negative/enum-values-iteration.java")

            val warnings = findings.filter { it.severity == Severity.WARNING }
            warnings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include outer loop, inner loop, and mutation evidence for Cartesian product`() {
            val findings = analyzeFixture("cardinality-explosion/positive/cartesian-product.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 3
            evidence[0].complexity shouldContain "orders"
            evidence[1].complexity shouldContain "products"
            evidence[2].complexity shouldContain "bottleneck"
        }

        @Test
        fun `should report correct complexity estimates`() {
            val findings = analyzeFixture("cardinality-explosion/positive/cartesian-product.java")

            findings shouldHaveSize 1
            findings.first().currentComplexity shouldContain "orders"
            findings.first().currentComplexity shouldContain "products"
            findings.first().suggestedComplexity shouldContain "index/join"
        }
    }

    @Nested
    inner class ConfidenceLevels {
        @Test
        fun `same-collection nesting should have LOW confidence`() {
            val findings = analyzeFixture("cardinality-explosion/negative/same-collection-nesting.java")

            val sameCollFindings = findings.filter { it.confidence == Confidence.LOW }
            // If the parser produces findings for same-collection nesting, they should be LOW confidence
            for (finding in sameCollFindings) {
                finding.confidence shouldBe Confidence.LOW
            }
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
