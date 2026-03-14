package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.InLoopCollectionBuildingRule
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class InLoopCollectionBuildingRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = InLoopCollectionBuildingRule()

    @Nested
    inner class InPlaceMutationCases {
        @Test
        fun `should not flag addAll - in-place mutation`() {
            // ArrayList.addAll() is an in-place mutation, not copy-on-modify
            val findings = analyzeFixture("in-loop-collection-building/positive/add-all-in-order-aggregation.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag putAll - in-place mutation`() {
            // HashMap.putAll() is an in-place mutation, not copy-on-modify
            val findings = analyzeFixture("in-loop-collection-building/positive/put-all-in-config-merge.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag addAll called outside loop`() {
            val findings = analyzeFixture("in-loop-collection-building/negative/add-all-outside-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag single add in loop`() {
            val findings = analyzeFixture("in-loop-collection-building/negative/single-add-in-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag toLowerCase in loop`() {
            val findings = analyzeFixture("in-loop-collection-building/negative/to-lower-case-in-loop.java")

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
