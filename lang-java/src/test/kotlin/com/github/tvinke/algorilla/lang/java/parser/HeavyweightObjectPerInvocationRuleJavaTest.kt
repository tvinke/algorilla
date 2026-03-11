package com.github.tvinke.algorilla.lang.java.parser

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

internal class HeavyweightObjectPerInvocationRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = HeavyweightObjectPerInvocationRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect ObjectMapper in method body`() {
            val findings = analyzeFixture("heavyweight-object-per-invocation/positive/mapper-in-method.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "expensive-construction"
            findings.first().message shouldContain "ObjectMapper"
            findings.first().message shouldContain "serialize"
        }

        @Test
        fun `should detect Gson in method body`() {
            val findings = analyzeFixture("heavyweight-object-per-invocation/positive/gson-in-method.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "Gson"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag non-heavyweight object creation`() {
            val findings = analyzeFixture("heavyweight-object-per-invocation/negative/no-heavyweight-types.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should provide evidence for ObjectMapper in method`() {
            val findings = analyzeFixture("heavyweight-object-per-invocation/positive/mapper-in-method.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 1
            evidence[0].label shouldContain "ObjectMapper"
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
