package com.github.tvinke.algorilla.lang.kotlin.parser

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

internal class RepeatedCollectionIterationRuleKotlinTest {
    private val parser = KotlinLanguageParser()
    private val rule = RepeatedCollectionIterationRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect two for-each loops over the same collection`() {
            val findings = analyzeFixture("repeated-collection-iteration/positive/two-sequences.kt")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "repeated-collection-iteration"
            findings.first().message shouldContain "orders"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag loops over different collections`() {
            val findings = analyzeFixture("repeated-collection-iteration/negative/different-sources.kt")

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
