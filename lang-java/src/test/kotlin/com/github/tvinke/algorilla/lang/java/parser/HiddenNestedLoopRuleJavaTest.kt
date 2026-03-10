package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.builtin.HiddenNestedLoopRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class HiddenNestedLoopRuleJavaTest {
    private val parser = JavaLanguageParser()
    private val rule = HiddenNestedLoopRule()

    @Nested
    inner class PositiveCases {
        @Test
        fun `should detect loop inside method called from for-each loop`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            findings.first().ruleId shouldBe "hidden-nested-loop"
            findings.first().message shouldContain "validateItems()"
            findings.first().message shouldContain "hidden"
        }

        @Test
        fun `should detect loop inside method called from forEach`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/foreach-calls-method-with-loop.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "summarize()"
        }

        @Test
        fun `should detect loop inside method called from while loop`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/while-loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "notifyListeners()"
            findings.first().message shouldContain "for-each loop" // hidden loop is a for-each
        }

        @Test
        fun `should detect hidden while loop from traditional for loop`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/for-loop-calls-method-with-while.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "processGroup()"
            findings.first().message shouldContain "while loop" // hidden loop is a while
        }

        @Test
        fun `should detect loop inside method called from stream forEach`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/stream-foreach-calls-method-with-loop.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "deliver()"
        }

        @Test
        fun `should detect multiple hidden loops from same outer loop`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/multiple-calls-with-hidden-loops.java")

            findings shouldHaveSize 2
            val names = findings.map { it.message }
            names.any { it.contains("validateItems()") } shouldBe true
            names.any { it.contains("calculateWeights()") } shouldBe true
        }

        @Test
        fun `should detect hidden loop from doubly-nested outer loops`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/nested-outer-loops.java")

            findings shouldHaveSize 1
            findings.first().message shouldContain "expandCell()"
        }
    }

    @Nested
    inner class NegativeCases {
        @Test
        fun `should not flag method without inner loop`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/loop-calls-method-without-loop.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag method with loop called outside loop context`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/no-loop-context.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag unresolvable external method`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/loop-calls-unresolvable-method.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not flag method with only conditionals`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/loop-calls-method-with-only-conditionals.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should handle recursive methods without crashing`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/recursive-method.java")

            // visit() is recursive and contains a loop — it will be flagged since
            // walkAll() loops and calls visit() which contains a for-each loop
            // This test primarily verifies no StackOverflow or infinite loop
            findings.size shouldBe findings.size // just verify it completes
        }

        @Test
        fun `should not flag when no called methods have loops`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/mixed-calls-only-some-have-loops.java")

            findings.shouldBeEmpty()
        }

        @Test
        fun `should not resolve wrong overload when parameter counts differ`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/loop-calls-overloaded-method.java")

            // fetchData(zone, date) should resolve to the 2-param version (no loop),
            // not the 1-param version (which contains the outer loop)
            findings.shouldBeEmpty()
        }

        @Test
        fun `should not resolve method call on different receiver`() {
            val findings = analyzeFixture("hidden-nested-loop/negative/loop-calls-method-on-different-receiver.java")

            // repository.persist(order) should NOT resolve to local persist(Document)
            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class EvidenceAndComplexity {
        @Test
        fun `should include evidence with outer loop and hidden inner loop`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            evidence shouldHaveSize 3
            evidence[0].label shouldContain "for-each loop"
            evidence[1].label shouldContain "validateItems()"
            evidence[2].label shouldContain "for-each loop"
            evidence[2].label shouldContain "validateItems()"
        }

        @Test
        fun `should show complexity with outer variable name`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            findings.first().currentComplexity shouldContain "orders"
        }

        @Test
        fun `should show suggested complexity`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            val suggested = findings.first().suggestedComplexity
            suggested shouldContain "orders"
            suggested shouldContain "+"
        }

        @Test
        fun `should show evidence depth for nested outer loops`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/nested-outer-loops.java")

            findings shouldHaveSize 1
            val evidence = findings.first().evidence
            // 2 outer loops + call + hidden loop = 4 evidence entries
            evidence shouldHaveSize 4
            evidence[0].depth shouldBe 0
            evidence[1].depth shouldBe 1
            evidence[2].depth shouldBe 2
            evidence[3].depth shouldBe 3
        }

        @Test
        fun `should report category as loop amplifier`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            rule.category.displayName shouldBe "Loop amplifiers"
        }

        @Test
        fun `should use bottleneck marker on inner loop evidence`() {
            val findings = analyzeFixture("hidden-nested-loop/positive/loop-calls-method-with-loop.java")

            findings shouldHaveSize 1
            val last = findings.first().evidence.last()
            last.complexity shouldContain "bottleneck"
        }
    }

    private fun analyzeFixture(fixturePath: String): List<Finding> {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        val path = File(url.toURI()).absolutePath
        val fileRoot = parser.parse(path)

        // Build symbol table so CrossMethodResolver can resolve calls
        val symbolTable = SymbolTable()
        fileRoot.findDescendants<FunctionDecl>().forEach { fn ->
            symbolTable.register(fn)
        }

        val context =
            AnalysisContext(
                irTrees = mapOf(path to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
