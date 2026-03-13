package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ParameterFlowAnnotatorTest {
    private val loc = SourceLocation("Test.java", 1, 1)
    private val annotator = ParameterFlowAnnotator(SymbolTable())

    private fun fn(
        name: String = "process",
        params: List<Parameter> = listOf(Parameter("items", "List")),
        children: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Test.$name",
        parameters = params,
        location = loc,
        children = children,
    )

    @Nested
    inner class LoopIterationFlow {
        @Test
        fun `param used as iteratedVariable in for-each loop`() {
            val loop =
                LoopNode(
                    kind = LoopKind.FOR_EACH,
                    iteratedVariable = "items",
                    location = loc,
                    children = emptyList(),
                )
            val function = fn(children = listOf(loop))
            val flows = annotator.computeFlows(function)

            flows shouldHaveSize 1
            flows.first().paramName shouldBe "items"
            flows.first().flowsInto shouldHaveSize 1
            flows
                .first()
                .flowsInto
                .first()
                .shouldBeInstanceOf<FlowTarget.LoopIteration>()
        }

        @Test
        fun `param not referenced produces no flow`() {
            val loop =
                LoopNode(
                    kind = LoopKind.FOR_EACH,
                    iteratedVariable = "other",
                    location = loc,
                    children = emptyList(),
                )
            val function = fn(children = listOf(loop))
            val flows = annotator.computeFlows(function)

            flows.shouldBeEmpty()
        }
    }

    @Nested
    inner class MethodCallReceiverFlow {
        @Test
        fun `param as LookupCall targetVariable`() {
            val lookup =
                LookupCall(
                    kind = LookupKind.CONTAINS,
                    targetVariable = "items",
                    isO1 = false,
                    location = loc,
                    children = emptyList(),
                )
            val function = fn(children = listOf(lookup))
            val flows = annotator.computeFlows(function)

            flows shouldHaveSize 1
            val target = flows.first().flowsInto.first()
            target.shouldBeInstanceOf<FlowTarget.MethodCallReceiver>()
            (target as FlowTarget.MethodCallReceiver).methodName shouldBe "contains"
        }

        @Test
        fun `param as FunctionCall qualifiedTarget`() {
            val call =
                FunctionCall(
                    name = "size",
                    qualifiedTarget = "items",
                    location = loc,
                    children = emptyList(),
                )
            val function = fn(children = listOf(call))
            val flows = annotator.computeFlows(function)

            flows shouldHaveSize 1
            val target = flows.first().flowsInto.first()
            target.shouldBeInstanceOf<FlowTarget.MethodCallReceiver>()
        }
    }

    @Nested
    inner class FunctionArgumentFlow {
        @Test
        fun `param passed as argument to another function`() {
            val arg =
                FunctionCall(
                    name = "items",
                    qualifiedTarget = null,
                    location = loc,
                    children = emptyList(),
                )
            val call =
                FunctionCall(
                    name = "helper",
                    qualifiedTarget = null,
                    arguments = listOf(arg),
                    location = loc,
                    children = emptyList(),
                )
            val function = fn(children = listOf(call))
            val flows = annotator.computeFlows(function)

            flows shouldHaveSize 1
            val target = flows.first().flowsInto.first()
            target.shouldBeInstanceOf<FlowTarget.FunctionArgument>()
            (target as FlowTarget.FunctionArgument).calledFunction shouldBe "helper"
        }
    }

    @Nested
    inner class VariableAliasing {
        private val aliasedFunction =
            fn(
                children =
                    listOf(
                        filteredFromItems(),
                        LoopNode(LoopKind.FOR_EACH, "filtered", location = loc, children = emptyList()),
                    ),
            )

        @Test
        fun `aliased variable tracks back to param`() {
            val flows = annotator.computeFlows(aliasedFunction)
            flows shouldHaveSize 1
            flows.first().paramName shouldBe "items"
        }

        @Test
        fun `aliased loop iteration is detected`() {
            val targets = annotator.computeFlows(aliasedFunction).first().flowsInto
            targets.any { it is FlowTarget.LoopIteration } shouldBe true
        }

        @Test
        fun `original receiver call is also detected`() {
            val targets = annotator.computeFlows(aliasedFunction).first().flowsInto
            targets.any { it is FlowTarget.MethodCallReceiver } shouldBe true
        }

        private fun filteredFromItems() =
            VariableDecl(
                name = "filtered",
                typeName = "List",
                location = loc,
                children =
                    listOf(
                        FunctionCall(name = "filter", qualifiedTarget = "items", location = loc, children = emptyList()),
                    ),
            )
    }

    @Nested
    inner class MultipleParams {
        @Test
        fun `each param tracked independently`() {
            val params = listOf(Parameter("orders", "List"), Parameter("products", "List"))
            val loop1 = LoopNode(LoopKind.FOR_EACH, "orders", location = loc, children = emptyList())
            val loop2 = LoopNode(LoopKind.FOR_EACH, "products", location = loc, children = emptyList())
            val function = fn(params = params, children = listOf(loop1, loop2))
            val flows = annotator.computeFlows(function)

            flows shouldHaveSize 2
            flows[0].paramName shouldBe "orders"
            flows[1].paramName shouldBe "products"
        }
    }

    @Nested
    inner class NoParams {
        @Test
        fun `function with no params produces no flows`() {
            val function = fn(params = emptyList())
            val flows = annotator.computeFlows(function)
            flows.shouldBeEmpty()
        }
    }
}
