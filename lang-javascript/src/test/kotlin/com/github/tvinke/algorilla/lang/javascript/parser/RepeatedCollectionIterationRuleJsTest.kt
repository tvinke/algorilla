package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RepeatedCollectionIterationRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RepeatedCollectionIterationRuleJsTest {
    private val parser = JavaScriptLanguageParser()
    private val rule = RepeatedCollectionIterationRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect two for-of loops over the same array`() {
            val findings = analyzeFixture("repeated-collection-iteration/positive/two-foreach-loops.js")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "repeated-collection-iteration"
            findings.first().message shouldContain "orders"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag loops over different arrays`() {
            val findings = analyzeFixture("repeated-collection-iteration/negative/different-sources.js")

            findings.shouldBeEmpty()
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
