package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Parameter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Direct unit tests for [FunctionDecl.isRecursive], built entirely from hand-constructed IR
 * (no parser involved), so `core`'s scoped PIT run actually exercises this function.
 *
 * Before this test existed, `isRecursive` was only reached indirectly through the
 * `lang-java` fixtures in `UnmemoizedRecursionRuleJavaTest`/`PrecisionRegressionTest`, which
 * sit outside `core`'s pitest scope — every mutant on this function came back NO_COVERAGE.
 * [NameVsTypeRecursionPropertyTest] covers the same cc #64/#71 edge cases but only ever
 * calls [FunctionCall.isSelfCallOf] directly on a hand-built call/target pair; it never
 * builds a [FunctionDecl] with real descendant [FunctionCall]s, so `isRecursive`'s own
 * `findDescendants<FunctionCall>()` walk was never actually invoked. These tests close that
 * gap by nesting the call a level deep in the body, the way a real `if`/block would.
 */
internal class RecursionDetectorTest {
    @Test
    fun `recognizes a genuine self-call nested inside the body`() {
        // fun walk(node: Node) { if (...) { walk(child) } } — the self-call sits inside a
        // nested statement, not as a direct child of the FunctionDecl, so this only passes
        // if findDescendants actually walks the tree instead of just the top-level children.
        val fn = walkFunction(body = statement(selfCall("walk", argCount = 1)))

        fn.isRecursive() shouldBe true
    }

    @Test
    fun `does not classify a function with no self-call as recursive`() {
        // fun walk(node: Node) { visit(node); process(node) } — same shape, different names.
        val fn =
            walkFunction(
                body =
                    statement(
                        call("visit", null, argCount = 1),
                        call("process", null, argCount = 1),
                    ),
            )

        fn.isRecursive() shouldBe false
    }

    @Test
    fun `does not mistake a super delegation for recursion`() {
        // cc #64 (Broadleaf): super.getSectionKey() dispatches to the superclass's own
        // implementation, not a repeat of this method — exercised here via isRecursive(),
        // not isSelfCallOf() directly.
        val fn = decl("getSectionKey", "AdminUserManagementController", parameters = listOf(Parameter("pathVars", "Map")))
        val withSuperCall = fn.withBody(statement(call("getSectionKey", "super", argCount = 1)))

        withSuperCall.isRecursive() shouldBe false
    }

    @Test
    fun `does not mistake a call on another object for recursion`() {
        // The generic form of the same bug: jobExecutor.setWaitTimeInMillis(...) called from
        // inside JobConfig.setWaitTimeInMillis(...) is delegation, not recursion.
        val fn = decl("setWaitTimeInMillis", "JobConfig", parameters = listOf(Parameter("millis", "int")))
        val delegating = fn.withBody(statement(call("setWaitTimeInMillis", "jobExecutor", argCount = 1)))

        delegating.isRecursive() shouldBe false
    }

    @Test
    fun `falls back to true without a symbol table even when a sibling overload would disambiguate it away`() {
        // cc #64/#71 (Fineract): without a symbolTable the ambiguity guard can't run, so a
        // same-name, same-arity call is reported as recursive — matching isSelfCallOf's own
        // documented fallback behaviour.
        val fn = loanFunction(sameArityParams = true)
        val withSelfCall = fn.withBody(statement(selfCall(fn.name, argCount = fn.parameters.size)))

        withSelfCall.isRecursive() shouldBe true
    }

    @Test
    fun `uses the symbol table to rule out an ambiguous sibling overload`() {
        val fn = loanFunction(sameArityParams = true)
        val sibling = loanFunction(sameArityParams = true).copy(qualifiedName = "${fn.qualifiedName}.sibling")
        val withSelfCall = fn.withBody(statement(selfCall(fn.name, argCount = fn.parameters.size)))
        val symbolTable =
            SymbolTable().also {
                it.register(withSelfCall)
                it.register(sibling)
            }

        withSelfCall.isRecursive(symbolTable) shouldBe false
    }

    @Test
    fun `stays true via the symbol table when there is no ambiguous sibling`() {
        val fn = loanFunction(sameArityParams = true)
        val withSelfCall = fn.withBody(statement(selfCall(fn.name, argCount = fn.parameters.size)))
        val symbolTable = SymbolTable().also { it.register(withSelfCall) }

        withSelfCall.isRecursive(symbolTable) shouldBe true
    }

    @Test
    fun `does not classify a same-name call with a different arity as recursion`() {
        val fn = walkFunction(body = statement(call("walk", null, argCount = 2)))

        fn.isRecursive() shouldBe false
    }

    private fun walkFunction(body: IRNode) = decl("walk", "TreeWalker", parameters = listOf(Parameter("node", "Node"))).withBody(body)

    private fun loanFunction(sameArityParams: Boolean) =
        decl(
            "modifyLoanApprovedAmount",
            "LoansApiResource",
            parameters =
                listOf(
                    Parameter("loanId", "Long"),
                    Parameter(if (sameArityParams) "uriInfo" else "loanExternalId", "UriInfo"),
                    Parameter("apiRequestBodyAsJson", "String"),
                ),
        )

    private fun FunctionDecl.withBody(body: IRNode) = copy(children = listOf(body))

    /** Wraps children in a nested, non-call node — the way a real `if`/block statement would. */
    private fun statement(vararg children: IRNode) = GenericNode(nodeType = "block", location = loc, children = children.toList())

    private fun selfCall(
        name: String,
        argCount: Int,
    ) = call(name, null, argCount)
}
