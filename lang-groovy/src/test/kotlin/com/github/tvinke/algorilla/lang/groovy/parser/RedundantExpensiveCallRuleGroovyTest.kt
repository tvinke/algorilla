package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.RedundantExpensiveCallRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class RedundantExpensiveCallRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = RedundantExpensiveCallRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect replaceAll called twice with same arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/positive/same-args.groovy")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "redundant-expensive-call"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag replaceAll with different arguments`() {
            val findings = analyzeFixture("redundant-expensive-call/negative/different-args.groovy")

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
