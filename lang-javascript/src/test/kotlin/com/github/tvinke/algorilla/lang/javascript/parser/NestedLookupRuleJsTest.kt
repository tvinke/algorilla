package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class NestedLookupRuleJsTest {
    private val parser = JavaScriptParser()
    private val rule = NestedLookupRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect array includes inside for-of loop`() {
            val findings = analyzeFixture("nested-lookup/positive/array-includes-in-loop.js")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "nested-lookup"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag Set has in loop when variable assigned from new Set`() {
            val findings = analyzeFixture("nested-lookup/negative/set-has-in-loop.js")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag Map has in loop when variable assigned from new Map`() {
            val findings = analyzeFixture("nested-lookup/negative/map-has-in-loop.js")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag TypeScript typed Set parameter`() {
            val findings = analyzeFixture("nested-lookup/negative/typed-set-in-loop.ts")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag TypeScript typed Map parameter`() {
            val findings = analyzeFixture("nested-lookup/negative/typed-map-in-loop.ts")

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
