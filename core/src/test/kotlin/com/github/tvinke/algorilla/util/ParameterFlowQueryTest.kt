package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ParameterFlowQueryTest {
    private val loc = SourceLocation("Test.java", 1, 1)

    private fun callerFn(
        parameterFlows: List<ParameterFlow> = emptyList(),
        children: List<com.github.tvinke.algorilla.model.IRNode> = emptyList(),
    ): FunctionDecl {
        val fn =
            FunctionDecl(
                name = "caller",
                qualifiedName = "Test.caller",
                parameters = listOf(Parameter("items", "List")),
                location = loc,
                children = children,
            )
        fn.parameterFlows = parameterFlows
        return fn
    }

    private fun calleeFn(
        name: String = "helper",
        parameterFlows: List<ParameterFlow> = emptyList(),
    ): FunctionDecl {
        val fn =
            FunctionDecl(
                name = name,
                qualifiedName = "Test.$name",
                parameters = listOf(Parameter("data", "List")),
                location = loc,
                children =
                    listOf(
                        LoopNode(LoopKind.FOR_EACH, "data", location = loc, children = emptyList()),
                    ),
            )
        fn.parameterFlows = parameterFlows
        return fn
    }

    private fun symbolTableWith(vararg decls: FunctionDecl): SymbolTable {
        val st = SymbolTable()
        for (decl in decls) st.register(decl)
        return st
    }

    @Nested
    inner class ParameterFlowsThrough {
        @Test
        fun `detects flow through helper into IO`() {
            val ioTarget = FlowTarget.MethodCallReceiver("save", loc)
            val helperFlows =
                listOf(
                    ParameterFlow(0, "data", setOf(ioTarget)),
                )
            val helper = calleeFn(parameterFlows = helperFlows)

            val callerFlows =
                listOf(
                    ParameterFlow(
                        0,
                        "items",
                        setOf(FlowTarget.FunctionArgument("helper", loc)),
                    ),
                )
            val caller = callerFn(parameterFlows = callerFlows)
            val call = FunctionCall("helper", null, location = loc, children = emptyList())

            val st = symbolTableWith(caller, helper)
            val evidence =
                ParameterFlowQuery.parameterFlowsThrough(
                    call,
                    caller,
                    st,
                ) { it is FlowTarget.MethodCallReceiver && it.methodName == "save" }

            evidence.shouldNotBeNull()
            evidence.paramName shouldBe "items"
            evidence.steps shouldHaveSize 1
            evidence.steps.first().calledFunction shouldBe "helper"
        }

        @Test
        fun `returns null when no flow matches`() {
            val helperFlows =
                listOf(
                    ParameterFlow(0, "data", setOf(FlowTarget.LoopIteration(loc))),
                )
            val helper = calleeFn(parameterFlows = helperFlows)

            val callerFlows =
                listOf(
                    ParameterFlow(
                        0,
                        "items",
                        setOf(FlowTarget.FunctionArgument("helper", loc)),
                    ),
                )
            val caller = callerFn(parameterFlows = callerFlows)
            val call = FunctionCall("helper", null, location = loc, children = emptyList())

            val st = symbolTableWith(caller, helper)
            val evidence =
                ParameterFlowQuery.parameterFlowsThrough(
                    call,
                    caller,
                    st,
                ) { it is FlowTarget.MethodCallReceiver && it.methodName == "save" }

            evidence.shouldBeNull()
        }

        @Test
        fun `returns null when caller has no parameter flows`() {
            val caller = callerFn()
            val call = FunctionCall("helper", null, location = loc, children = emptyList())
            val st = symbolTableWith(caller)

            val evidence =
                ParameterFlowQuery.parameterFlowsThrough(
                    call,
                    caller,
                    st,
                ) { true }

            evidence.shouldBeNull()
        }
    }

    @Nested
    inner class FindRedundantIterations {
        private fun twoCalleeCaller() =
            callerFn(
                parameterFlows =
                    listOf(
                        ParameterFlow(
                            0,
                            "items",
                            setOf(
                                FlowTarget.FunctionArgument("methodA", loc),
                                FlowTarget.FunctionArgument("methodB", loc),
                            ),
                        ),
                    ),
            )

        private val twoCalls =
            listOf(
                FunctionCall("methodA", null, location = loc, children = emptyList()),
                FunctionCall("methodB", null, location = loc, children = emptyList()),
            )

        private fun iteratingCallee(name: String) =
            calleeFn(
                name = name,
                parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.LoopIteration(loc)))),
            )

        private fun nonIteratingCallee(name: String) =
            calleeFn(
                name = name,
                parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.MethodCallReceiver("size", loc)))),
            )

        @Test
        fun `detects two callees iterating the same param`() {
            val caller = twoCalleeCaller()
            val st = symbolTableWith(caller, iteratingCallee("methodA"), iteratingCallee("methodB"))
            val redundant = ParameterFlowQuery.findRedundantIterations(caller, twoCalls, st)

            redundant shouldHaveSize 1
            redundant.first().paramName shouldBe "items"
            redundant.first().iteratingCallees shouldHaveSize 2
        }

        @Test
        fun `returns empty when only one callee iterates`() {
            val caller = twoCalleeCaller()
            val st = symbolTableWith(caller, iteratingCallee("methodA"), nonIteratingCallee("methodB"))
            val redundant = ParameterFlowQuery.findRedundantIterations(caller, twoCalls, st)

            redundant shouldHaveSize 0
        }
    }
}
