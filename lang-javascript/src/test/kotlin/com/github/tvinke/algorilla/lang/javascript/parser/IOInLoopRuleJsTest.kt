package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.IOInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class IOInLoopRuleJsTest {
    private val parser = JavaScriptLanguageParser()
    private val rule = IOInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect fetch inside for-of loop`() {
            val findings = analyzeFixture("io-in-loop/positive/fetch-in-loop.js")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "io-in-loop"
            findings.first().message shouldContain "fetch"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag fetch outside loop`() {
            val findings = analyzeFixture("io-in-loop/negative/fetch-outside-loop.js")

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
