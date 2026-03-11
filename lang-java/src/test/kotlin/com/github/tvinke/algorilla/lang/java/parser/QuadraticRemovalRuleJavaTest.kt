package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.QuadraticRemovalRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class QuadraticRemovalRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = QuadraticRemovalRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect remove inside loop`() {
            val findings = analyzeFixture("quadratic-removal/positive/remove-in-loop.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "quadratic-removal"
            findings.first().message shouldContain "remove"
            findings.first().message shouldContain "items"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag map remove`() {
            val findings = analyzeFixture("quadratic-removal/negative/map-remove.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence chain for remove in loop`() {
            val findings = analyzeFixture("quadratic-removal/positive/remove-in-loop.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 2
            evidence[0].complexity shouldContain "O("
            evidence[1].label shouldContain "remove"
            evidence[1].label shouldContain "inside loop"
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
