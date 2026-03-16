package com.github.tvinke.algorilla.lang.java.parser

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
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class IOInLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = IOInLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect JDBC calls inside for loop`() {
            val findings = analyzeFixture("io-in-loop/positive/jdbc-in-loop.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "io-in-loop"
            findings.first().message shouldContain "inside"
        }

        @Test
        fun `should detect Spring RestTemplate call inside for loop`() {
            val findings = analyzeFixture("io-in-loop/positive/spring-rest-in-loop.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "io-in-loop"
            findings.first().message shouldContain "getForObject"
        }

        @Test
        fun `should detect IO call inside stream map`() {
            val findings = analyzeFixture("io-in-loop/positive/map-with-io-call.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "io-in-loop"
        }

        @Test
        fun `should detect apiClient search in for loop`() {
            val findings = analyzeFixture("io-in-loop/positive/api-search-in-loop.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "io-in-loop"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag JDBC call outside loop`() {
            val findings = analyzeFixture("io-in-loop/negative/jdbc-outside-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag ByteArrayOutputStream or StringBuilder write in loop`() {
            val findings = analyzeFixture("io-in-loop/negative/in-memory-buffer-write.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag StringTokenizer nextToken in loop`() {
            val findings = analyzeFixture("io-in-loop/negative/string-tokenizer-in-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag abbreviated stTok variable as IO target`() {
            val findings = analyzeFixture("io-in-loop/negative/sttok-next-token-in-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag stream copy idiom with read and write`() {
            val findings = analyzeFixture("io-in-loop/regression/stream-copy-idiom-not-flagged.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag Command execute or Future cancel in loop`() {
            val findings = analyzeFixture("io-in-loop/regression/command-execute-not-io.java")

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
