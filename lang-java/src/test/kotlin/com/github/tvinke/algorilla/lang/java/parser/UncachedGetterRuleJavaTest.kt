package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.UncachedGetterRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class UncachedGetterRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = UncachedGetterRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect repeated getProduct calls with same argument`() {
            val findings = analyzeFixture("uncached-getter/positive/repeated-get-product-in-order-handler.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "uncached-getter"
            findings.first().message shouldContain "getProduct"
            findings.first().message shouldContain "times"
        }

        @Test
        fun `should detect repeated findCustomer calls with same argument`() {
            val findings = analyzeFixture("uncached-getter/positive/repeated-find-customer.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "findCustomer"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag getter called only once`() {
            val findings = analyzeFixture("uncached-getter/negative/getter-called-once.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag getter called with different arguments`() {
            val findings = analyzeFixture("uncached-getter/negative/getter-with-different-args.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag excluded generic get method`() {
            val findings = analyzeFixture("uncached-getter/negative/excluded-generic-get.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include first call and duplicate evidence`() {
            val findings = analyzeFixture("uncached-getter/positive/repeated-get-product-in-order-handler.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence.any { it.label.contains("1st call") } shouldBe true
            evidence.any { it.label.contains("duplicate") } shouldBe true
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
