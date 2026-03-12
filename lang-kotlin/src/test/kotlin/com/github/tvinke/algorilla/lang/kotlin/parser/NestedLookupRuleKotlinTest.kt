package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.markScalarLookups
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class NestedLookupRuleKotlinTest {
    private val parser = KotlinLanguageParser()
    private val rule = NestedLookupRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect list contains inside for-in loop`() {
            val findings = analyzeFixture("nested-lookup/positive/list-contains-in-for.kt")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "nested-lookup"
            findings.first().message shouldContain "contains"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag set contains in loop`() {
            val findings = analyzeFixture("nested-lookup/negative/set-contains-in-loop.kt")

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
        val irTrees = markScalarLookups(mapOf(path to fileRoot), registry)
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
