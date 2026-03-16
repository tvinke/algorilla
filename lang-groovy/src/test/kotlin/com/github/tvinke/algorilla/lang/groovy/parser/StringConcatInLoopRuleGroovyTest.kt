package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.StringConcatInLoopRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class StringConcatInLoopRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = StringConcatInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect concat inside loop`() {
            val findings = analyzeFixture("string-concat-in-loop/positive/concat-in-loop.groovy")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "string-concat-in-loop"
            findings.first().message shouldContain "concat"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag StringBuilder append`() {
            val findings = analyzeFixture("string-concat-in-loop/negative/string-builder.groovy")

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
        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
