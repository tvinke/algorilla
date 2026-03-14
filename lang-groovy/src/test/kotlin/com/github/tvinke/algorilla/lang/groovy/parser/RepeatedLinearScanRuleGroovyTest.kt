package com.github.tvinke.algorilla.lang.groovy.parser

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

internal class RepeatedLinearScanRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = RepeatedLinearScanRule()

    @Nested
    inner class FullScanCases {
        @Test
        fun `should detect repeated groupBy and unique on same collection`() {
            val findings = analyzeFixture("repeated-linear-scan/positive/repeated-groupby-collectentries.groovy")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "repeated-linear-scan"
            findings.first().message shouldContain "orders"
            findings.first().message shouldContain "2 times"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag single groupBy`() {
            val findings = analyzeFixture("repeated-linear-scan/negative/single-groupby.groovy")

            findings.shouldBeEmpty()
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.loadDefaults()
        val irTrees = markScalarLookups(mapOf(path to fileRoot), registry).irTrees
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
