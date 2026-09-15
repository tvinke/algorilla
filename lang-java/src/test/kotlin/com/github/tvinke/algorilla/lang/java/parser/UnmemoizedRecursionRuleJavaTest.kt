package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.engine.markScalarLookups
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.UnmemoizedRecursionRule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class UnmemoizedRecursionRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = UnmemoizedRecursionRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect unmemoized fibonacci`() {
            val findings = analyzeFixture("unmemoized-recursion/positive/fibonacci.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "unmemoized-recursion"
            findings.first().confidence shouldBe Confidence.HIGH
            findings.first().message shouldContain "recursive calls without memoization"
        }

        @Test
        fun `should detect recursive call inside loop`() {
            val findings = analyzeFixture("unmemoized-recursion/positive/recursive-count.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "unmemoized-recursion"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag memoized fibonacci`() {
            val findings = analyzeFixture("unmemoized-recursion/negative/memoized-fibonacci.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag tree traversal with child accessors`() {
            val findings = analyzeFixture("unmemoized-recursion/negative/tree-traversal.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag super call delegation as recursion`() {
            val findings = analyzeFixture("unmemoized-recursion/negative/super-call-delegation.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag a same-arity sibling overload as recursion`() {
            val findings = analyzeFixture("unmemoized-recursion/negative/sibling-overload-same-arity.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag delegation to a differently named object as recursion`() {
            val findings = analyzeFixture("unmemoized-recursion/negative/delegate-to-other-object.java")

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class ArityGuardDoesNotSuppressRealRecursion {
        @Test
        fun `should still flag recursion when an unrelated different-arity overload exists`() {
            val findings = analyzeFixture("unmemoized-recursion/positive/different-arity-overload-still-recursive.java")

            findings.shouldNotBeEmpty()
            findings.first().ruleId shouldBe "unmemoized-recursion"
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)
        val registry = LanguageSemanticsRegistry.loadDefaults()
        val irTrees = markScalarLookups(mapOf(path to fileRoot), registry).irTrees
        val symbolTable = SymbolTable()
        fileRoot.findDescendants<FunctionDecl>().forEach { fn -> symbolTable.register(fn) }
        val context =
            AnalysisContext(
                irTrees = irTrees,
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
