package com.github.tvinke.algorilla.rules.custom

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.RuleCategory
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RuleDslTest {
    private val loc = SourceLocation("Test.java", 1, 1)

    private fun loopNode(
        line: Int = 1,
        children: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ) = LoopNode(LoopKind.FOR_EACH, "items", SourceLocation("Test.java", line, 1), children)

    private fun functionCall(
        name: String = "doSomething",
        line: Int = 1,
    ) = FunctionCall(name, null, emptyList(), SourceLocation("Test.java", line, 1), emptyList())

    private fun fileRoot(children: List<com.github.tvinke.algorilla.model.IRNode>) = FileRoot("Test.java", Language.JAVA, loc, children)

    private fun context(fileRoot: FileRoot) =
        AnalysisContext(
            irTrees = mapOf("Test.java" to fileRoot),
            symbolTable = SymbolTable(),
            callGraph = CallGraph(),
            config = AnalysisConfig(),
        )

    @Nested
    inner class BuilderDefaults {
        @Test
        fun `name defaults to the rule id when not set`() {
            val built = rule("custom-001") {}
            built.id shouldBe "custom-001"
            built.name shouldBe "custom-001"
        }

        @Test
        fun `severity defaults to WARNING`() {
            val built = rule("custom-001") {}
            built.severity shouldBe Severity.WARNING
        }

        @Test
        fun `category defaults to REDUNDANCY`() {
            val built = rule("custom-001") {}
            built.category shouldBe RuleCategory.REDUNDANCY
        }

        @Test
        fun `languages defaults to all languages`() {
            val built = rule("custom-001") {}
            built.languages shouldBe Language.entries.toSet()
        }

        @Test
        fun `all builder fields can be overridden`() {
            val built =
                rule("custom-002") {
                    name = "My Rule"
                    severity = Severity.ERROR
                    category = RuleCategory.LOOP_AMPLIFIER
                    languages = setOf(Language.JAVA)
                }
            built.name shouldBe "My Rule"
            built.severity shouldBe Severity.ERROR
            built.category shouldBe RuleCategory.LOOP_AMPLIFIER
            built.languages shouldBe setOf(Language.JAVA)
        }
    }

    @Nested
    inner class OnNodeVisitor {
        @Test
        fun `onNode only fires for the matching node type`() {
            val built =
                rule("custom-003") {
                    onNode<LoopNode> { node, _ ->
                        report(node.location, "loop found")
                    }
                }
            val root = fileRoot(listOf(loopNode(), functionCall()))
            val findings = built.evaluate(context(root))

            findings shouldHaveSize 1
            findings.first().message shouldBe "loop found"
        }

        @Test
        fun `onNode visits descendant nodes, not just top-level children`() {
            val built =
                rule("custom-004") {
                    onNode<FunctionCall> { node, _ ->
                        report(node.location, "call: ${node.name}")
                    }
                }
            val nestedCall = functionCall("innerCall", line = 3)
            val root = fileRoot(listOf(loopNode(children = listOf(nestedCall))))
            val findings = built.evaluate(context(root))

            findings shouldHaveSize 1
            findings.first().message shouldBe "call: innerCall"
        }

        @Test
        fun `reports one finding per matching node`() {
            val built =
                rule("custom-005") {
                    onNode<LoopNode> { node, _ -> report(node.location, "loop") }
                }
            val root = fileRoot(listOf(loopNode(line = 1), loopNode(line = 2), functionCall()))
            val findings = built.evaluate(context(root))

            findings shouldHaveSize 2
        }

        @Test
        fun `evaluate returns no findings when no onNode visitor was registered`() {
            val built = rule("custom-006") {}
            val root = fileRoot(listOf(loopNode()))
            val findings = built.evaluate(context(root))

            findings.shouldBeEmpty()
        }
    }

    @Nested
    inner class FindingContent {
        @Test
        fun `report uses the rule's id, name, severity and location`() {
            val built =
                rule("custom-007") {
                    name = "Big Loop"
                    severity = Severity.INFO
                    onNode<LoopNode> { node, _ -> report(node.location, "too big") }
                }
            val loop = loopNode(line = 42)
            val findings = built.evaluate(context(fileRoot(listOf(loop))))

            findings shouldHaveSize 1
            val finding = findings.first()
            finding.ruleId shouldBe "custom-007"
            finding.ruleName shouldBe "Big Loop"
            finding.severity shouldBe Severity.INFO
            finding.location.line shouldBe 42
        }

        @Test
        fun `report falls back to the rule's default suggestion when none is given`() {
            val built =
                rule("custom-008") {
                    suggestion = "Split the loop body"
                    onNode<LoopNode> { node, _ -> report(node.location, "too big") }
                }
            val findings = built.evaluate(context(fileRoot(listOf(loopNode()))))

            findings.first().suggestion shouldBe "Split the loop body"
        }

        @Test
        fun `report uses a per-call suggestion override when given`() {
            val built =
                rule("custom-009") {
                    suggestion = "default suggestion"
                    onNode<LoopNode> { node, _ -> report(node.location, "too big", "specific suggestion") }
                }
            val findings = built.evaluate(context(fileRoot(listOf(loopNode()))))

            findings.first().suggestion shouldBe "specific suggestion"
        }
    }
}
