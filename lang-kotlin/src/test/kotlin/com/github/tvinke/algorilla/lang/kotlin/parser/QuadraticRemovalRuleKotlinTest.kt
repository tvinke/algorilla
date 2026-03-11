package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.QuadraticRemovalRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class QuadraticRemovalRuleKotlinTest {
    private val parser = KotlinParser()
    private val rule = QuadraticRemovalRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect list remove in loop`() {
            val findings = analyzeFixture("quadratic-removal/positive/list-remove-in-loop.kt")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "quadratic-removal"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag HashSet remove in loop`() {
            val findings = analyzeFixture("quadratic-removal/negative/hashset-remove-in-loop.kt")

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
