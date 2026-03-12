package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.HeavyweightObjectPerInvocationRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class HeavyweightObjectPerInvocationRuleKotlinTest {
    private val parser = KotlinLanguageParser()
    private val rule = HeavyweightObjectPerInvocationRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect ObjectMapper created inside method body`() {
            val findings = analyzeFixture("expensive-construction/positive/mapper-in-method.kt")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "expensive-construction"
            findings.first().message shouldContain "ObjectMapper"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag non-heavyweight object creation`() {
            val findings = analyzeFixture("expensive-construction/negative/no-heavyweight-types.kt")

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
