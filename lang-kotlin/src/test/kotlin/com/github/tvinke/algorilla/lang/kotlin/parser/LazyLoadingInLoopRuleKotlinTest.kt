package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.LazyLoadingInLoopRule
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class LazyLoadingInLoopRuleKotlinTest {
    private val parser = KotlinLanguageParser()
    private val rule = LazyLoadingInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect lazy collection getter inside loop over repository results`() {
            val findings = analyzeFixture("lazy-loading-in-loop/positive/entity-getter.kt")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "lazy-loading-in-loop"
            findings.first().message shouldContain "getLineItems"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag scalar getter inside loop`() {
            val findings = analyzeFixture("lazy-loading-in-loop/negative/scalar-getter.kt")

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
