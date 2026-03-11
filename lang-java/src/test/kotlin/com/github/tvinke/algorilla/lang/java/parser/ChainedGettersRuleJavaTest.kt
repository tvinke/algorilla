package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.ChainedGettersRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class ChainedGettersRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = ChainedGettersRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect cascading order-customer-address lookups`() {
            val findings = analyzeFixture("chained-getters/positive/cascading-order-lookups.java")

            // Two chains: getOrder→getCustomer (2-link) and getOrder→getCustomer→getAddress (3-link)
            findings shouldHaveSize 2
            findings.all { it.ruleId == "chained-getters" } shouldBe true
            findings.any { it.message.contains("getAddress") } shouldBe true
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag single getter call`() {
            val findings = analyzeFixture("chained-getters/negative/single-getter-call.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag chained getters without linear lookups`() {
            val findings = analyzeFixture("chained-getters/negative/chain-without-linear-lookup.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include chain evidence entries`() {
            val findings = analyzeFixture("chained-getters/positive/cascading-order-lookups.java")

            findings shouldHaveSize 2
            // Check the longest chain (3-link)
            val longest = findings.maxBy { it.evidence.size }
            longest.evidence.first().label shouldContain "starts the chain"
            longest.evidence.last().label shouldContain "uses result of previous"
            longest.evidence.size shouldBe 3
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val symbolTable = SymbolTable()
        fileRoot.findDescendants<FunctionDecl>().forEach { fn -> symbolTable.register(fn) }
        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
