package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.markScalarLookups
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.IOInLoopRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class IOInLoopRuleGroovyTest {
    private val parser = GroovyLanguageParser()
    private val rule = IOInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect couchDb find inside for loop`() {
            val findings = analyzeFixture("io-in-loop/positive/couchdb-find-in-loop.groovy")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "io-in-loop"
        }

        @Test
        fun `should detect couchDb find inside collect closure`() {
            val findings = analyzeFixture("io-in-loop/positive/collect-closure-with-db-find.groovy")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "io-in-loop"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag GDK find with closure predicate`() {
            val findings = analyzeFixture("io-in-loop/negative/gdk-find-with-closure.groovy")

            findings.shouldBeEmpty()
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.DEFAULT
        val irTrees = markScalarLookups(mapOf(path to fileRoot), registry).irTrees
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
