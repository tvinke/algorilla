package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.markScalarLookups
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RepeatedLinearScanRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RepeatedLinearScanRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = RepeatedLinearScanRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect multiple filters on same collection`() {
            val findings = analyzeFixture("repeated-linear-scan/positive/multiple-filters.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "repeated-linear-scan"
            findings.first().message shouldContain "users"
            findings.first().currentComplexity shouldBe "O(n \u00d7 2)"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag single lookup`() {
            val findings = analyzeFixture("repeated-linear-scan/negative/single-lookup.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag indexOf on String variable`() {
            val findings = analyzeFixture("repeated-linear-scan/negative/string-index-of.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag indexOf on String field`() {
            val findings = analyzeFixture("repeated-linear-scan/negative/string-field-index-of.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag contains on Set field`() {
            val findings = analyzeFixture("repeated-linear-scan/negative/set-field-contains.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence entries for each scan`() {
            val findings = analyzeFixture("repeated-linear-scan/positive/multiple-filters.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].label shouldContain "users"
            evidence[0].label shouldContain "1st"
            evidence[1].label shouldContain "users"
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.loadDefaults()
        val irTrees = markScalarLookups(mapOf(path to fileRoot), registry)
        val context =
            AnalysisContext(
                irTrees = irTrees,
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
