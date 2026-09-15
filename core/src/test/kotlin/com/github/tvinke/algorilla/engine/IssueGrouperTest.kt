package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.GroupType
import com.github.tvinke.algorilla.model.GroupVisibility
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class IssueGrouperTest {
    private val file = "/src/main/java/com/example/OrderService.java"

    private fun finding(
        ruleId: String = "nested-lookup",
        line: Int = 10,
        file: String = this.file,
        severity: Severity = Severity.WARNING,
        confidence: Confidence = Confidence.HIGH,
    ) = Finding(
        ruleId = ruleId,
        ruleName = ruleId.replace("-", " ").replaceFirstChar { it.uppercase() },
        severity = severity,
        location = SourceLocation(file, line, 1),
        message = "Finding at $file:$line",
        suggestions = listOf(Suggestion.Freeform("Fix it")),
        confidence = confidence,
    )

    /** Creates a FunctionDecl spanning from startLine to endLine via a dummy child node. */
    private fun functionDecl(
        name: String,
        qualifiedName: String,
        file: String = this.file,
        startLine: Int = 1,
        endLine: Int = 50,
    ): FunctionDecl =
        FunctionDecl(
            name = name,
            qualifiedName = qualifiedName,
            location = SourceLocation(file, startLine, 1),
            parameters = emptyList(),
            children = listOf(GenericNode("end-marker", SourceLocation(file, endLine, 1), emptyList())),
        )

    private fun fileRoot(
        filePath: String,
        functions: List<FunctionDecl>,
    ): FileRoot =
        FileRoot(
            filePath = filePath,
            language = Language.JAVA,
            location = SourceLocation(filePath, 1, 1),
            children = functions,
        )

    @Nested
    inner class SameMethodGrouping {
        @Test
        fun `findings in the same method are grouped together`() {
            val fn = functionDecl("process", "OrderService.process", startLine = 1, endLine = 50)
            val irTrees = mapOf(file to fileRoot(file, listOf(fn)))
            val findings =
                listOf(
                    finding(ruleId = "io-in-loop", line = 10),
                    finding(ruleId = "nested-lookup", line = 20),
                    finding(ruleId = "n-plus-one-query", line = 30),
                )
            val callGraph = CallGraph()

            val groups = groupFindings(findings, irTrees, callGraph)

            groups shouldHaveSize 1
            groups[0].contributingFindings shouldHaveSize 3
            groups[0].groupType shouldBe GroupType.SAME_METHOD
            groups[0].anchor shouldBe "OrderService.process"
        }

        @Test
        fun `findings in different methods produce separate groups`() {
            val fn1 = functionDecl("create", "OrderService.create", startLine = 1, endLine = 25)
            val fn2 = functionDecl("delete", "OrderService.delete", startLine = 30, endLine = 50)
            val irTrees = mapOf(file to fileRoot(file, listOf(fn1, fn2)))
            val findings =
                listOf(
                    finding(ruleId = "io-in-loop", line = 10),
                    finding(ruleId = "io-in-loop", line = 15),
                    finding(ruleId = "nested-lookup", line = 35),
                    finding(ruleId = "nested-lookup", line = 40),
                )
            val callGraph = CallGraph()

            val groups = groupFindings(findings, irTrees, callGraph)

            groups shouldHaveSize 2
            groups.all { it.contributingFindings.size == 2 } shouldBe true
        }
    }

    @Nested
    inner class CallEdgeGrouping {
        @Test
        fun `findings in caller and callee are grouped when call edge exists`() {
            val caller = functionDecl("process", "OrderService.process", startLine = 1, endLine = 25)
            val callee = functionDecl("validate", "OrderService.validate", startLine = 30, endLine = 50)
            val irTrees = mapOf(file to fileRoot(file, listOf(caller, callee)))
            val findings =
                listOf(
                    finding(ruleId = "io-in-loop", line = 10),
                    finding(ruleId = "io-in-loop", line = 15),
                    finding(ruleId = "nested-lookup", line = 35),
                )
            val callGraph = CallGraph()
            callGraph.addEdge("OrderService.process", "OrderService.validate")

            val groups = groupFindings(findings, irTrees, callGraph)

            // callee has 1 finding, not enough to be anchor, so assigned to caller
            groups shouldHaveSize 1
            groups[0].contributingFindings shouldHaveSize 3
            groups[0].groupType shouldBe GroupType.SAFE_CALL_EDGE
        }
    }

    @Nested
    inner class AnchorNonTransitivity {
        @Test
        fun `two anchors sharing a callee remain separate groups`() {
            val anchor1 = functionDecl("createOrder", "OrderService.createOrder", startLine = 1, endLine = 20)
            val anchor2 = functionDecl("deleteOrder", "OrderService.deleteOrder", startLine = 25, endLine = 45)
            val shared = functionDecl("save", "OrderService.save", startLine = 50, endLine = 60)
            val irTrees = mapOf(file to fileRoot(file, listOf(anchor1, anchor2, shared)))
            val findings =
                listOf(
                    finding(ruleId = "io-in-loop", line = 5),
                    finding(ruleId = "n-plus-one-query", line = 10),
                    finding(ruleId = "io-in-loop", line = 30),
                    finding(ruleId = "n-plus-one-query", line = 35),
                    finding(ruleId = "nested-lookup", line = 55),
                )
            val callGraph = CallGraph()
            callGraph.addEdge("OrderService.createOrder", "OrderService.save")
            callGraph.addEdge("OrderService.deleteOrder", "OrderService.save")

            val groups = groupFindings(findings, irTrees, callGraph)

            // save(1 finding) should go to one anchor, but both anchors stay separate
            groups.filter { it.contributingFindings.size >= 2 } shouldHaveSize 2
        }
    }

    @Nested
    inner class RepresentativeSelection {
        @Test
        fun `representative is the finding with highest severity then confidence`() {
            val findings =
                listOf(
                    finding(ruleId = "info-rule", severity = Severity.INFO, confidence = Confidence.HIGH, line = 5),
                    finding(ruleId = "warning-high", severity = Severity.WARNING, confidence = Confidence.HIGH, line = 10),
                    finding(ruleId = "warning-medium", severity = Severity.WARNING, confidence = Confidence.MEDIUM, line = 15),
                )

            val rep = selectRepresentative(findings)

            rep.ruleId shouldBe "warning-high"
        }

        @Test
        fun `among equal severity and confidence, shorter message wins`() {
            val findings =
                listOf(
                    finding(ruleId = "verbose-rule", line = 10).let {
                        it.copy(message = "A very long and detailed message about the problem that goes on and on")
                    },
                    finding(ruleId = "concise-rule", line = 20).let {
                        it.copy(message = "Short message")
                    },
                )

            val rep = selectRepresentative(findings)

            rep.ruleId shouldBe "concise-rule"
        }
    }

    @Nested
    inner class GroupVisibilityClassification {
        @Test
        fun `single-method groups have UNKNOWN visibility`() {
            val fn = functionDecl("process", "OrderService.process", startLine = 1, endLine = 50)
            val irTrees = mapOf(file to fileRoot(file, listOf(fn)))
            val findings =
                listOf(
                    finding(line = 10),
                    finding(line = 20),
                )
            val callGraph = CallGraph()

            val groups = groupFindings(findings, irTrees, callGraph)

            groups[0].visibility shouldBe GroupVisibility.UNKNOWN
        }

        @Test
        fun `call-edge groups have RESOLVED_SAFE visibility`() {
            val caller = functionDecl("process", "OrderService.process", startLine = 1, endLine = 25)
            val callee = functionDecl("validate", "OrderService.validate", startLine = 30, endLine = 50)
            val irTrees = mapOf(file to fileRoot(file, listOf(caller, callee)))
            val findings =
                listOf(
                    finding(line = 10),
                    finding(line = 15),
                    finding(line = 35),
                )
            val callGraph = CallGraph()
            callGraph.addEdge("OrderService.process", "OrderService.validate")

            val groups = groupFindings(findings, irTrees, callGraph)

            groups[0].visibility shouldBe GroupVisibility.RESOLVED_SAFE
        }
    }

    @Nested
    inner class EmptyInput {
        @Test
        fun `no findings produces no groups`() {
            val groups = groupFindings(emptyList(), emptyMap(), CallGraph())
            groups shouldHaveSize 0
        }

        @Test
        fun `single finding produces one singleton group`() {
            val fn = functionDecl("process", "OrderService.process", startLine = 1, endLine = 50)
            val irTrees = mapOf(file to fileRoot(file, listOf(fn)))
            val findings = listOf(finding(line = 10))

            val groups = groupFindings(findings, irTrees, CallGraph())

            groups shouldHaveSize 1
            groups[0].contributingFindings shouldHaveSize 1
        }
    }

    @Nested
    inner class CachedMethodHints {
        @Test
        fun `findings without IR trees use cached method hints for grouping`() {
            // No IR trees — simulates a fully cached run
            val findings =
                listOf(
                    finding(ruleId = "io-in-loop", line = 10),
                    finding(ruleId = "nested-lookup", line = 20),
                )
            val hints =
                mapOf(
                    "$file:10" to "OrderService.process",
                    "$file:20" to "OrderService.process",
                )

            val groups = groupFindings(findings, emptyMap(), CallGraph(), cachedMethodHints = hints)

            groups shouldHaveSize 1
            groups[0].anchor shouldBe "OrderService.process"
            groups[0].contributingFindings shouldHaveSize 2
        }

        @Test
        fun `findings without IR trees and without hints fall back to class from filename`() {
            val findings =
                listOf(
                    finding(ruleId = "io-in-loop", line = 10),
                    finding(ruleId = "nested-lookup", line = 20),
                )

            val groups = groupFindings(findings, emptyMap(), CallGraph())

            groups shouldHaveSize 1
            // Falls back to class name from file path
            groups[0].anchor shouldBe "OrderService"
            groups[0].contributingFindings shouldHaveSize 2
        }
    }
}
