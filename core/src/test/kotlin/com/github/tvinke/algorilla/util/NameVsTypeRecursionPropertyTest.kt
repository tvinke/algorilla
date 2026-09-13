package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Parameter
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.forAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Canary property for cc #64/#71: a classification helper that matches on method name must
 * not change its verdict just because the name matched — it has to keep telling apart a
 * genuine self-call from a same-name call that isn't one, whether the difference is the
 * receiver (Broadleaf's `super.getSectionKey()`) or an unresolved sibling overload
 * (Fineract's `modifyLoanApprovedAmount`). This is the property that would have failed
 * against [isSelfCallOf]'s predecessor, the bare `it.name == fn.name` check.
 */
internal class NameVsTypeRecursionPropertyTest {
    /** One half of a pair: a call, the target it's being checked against, and the symbol table available. */
    private data class ClassifiedCall(
        val call: FunctionCall,
        val target: FunctionDecl,
        val symbolTable: SymbolTable,
    )

    private data class Scenario(
        val label: String,
        val genuineSelfCall: ClassifiedCall,
        val sameNameNotASelfCall: ClassifiedCall,
    )

    @Test
    fun `classification does not change when the name matches but the receiver or overload set does not`() {
        runBlocking {
            forAll(Arb.of(scenarios)) { scenario ->
                val genuineCall = scenario.genuineSelfCall
                val impostorCall = scenario.sameNameNotASelfCall
                val genuine = genuineCall.call.isSelfCallOf(genuineCall.target, genuineCall.symbolTable)
                val impostor = impostorCall.call.isSelfCallOf(impostorCall.target, impostorCall.symbolTable)
                genuine && !impostor
            }
        }
    }

    @Test
    fun `still recognizes receiver and arity without a symbol table`() {
        // No symbolTable means the ambiguity guard can't run — isSelfCallOf falls back to
        // receiver and arity alone, which is enough for the plain super-call and
        // delegate-to-other-object cases (just not for the sibling-overload one).
        val fn = decl("resolveKey", "Sub", listOf(Parameter("vars", "Map")))
        call("resolveKey", null, argCount = 1).isSelfCallOf(fn) shouldBe true
        call("resolveKey", "super", argCount = 1).isSelfCallOf(fn) shouldBe false
    }

    private val scenarios: List<Scenario> =
        listOf(broadleafSuperDelegation(), delegateToOtherObject(), fineractSiblingOverload())

    // cc #64: super.getSectionKey() is not recursion — super dispatches to the superclass's
    // own implementation, it does not repeat this method's call.
    private fun broadleafSuperDelegation(): Scenario {
        val fn = decl("getSectionKey", "AdminUserManagementController", listOf(Parameter("pathVars", "Map")))
        val table = SymbolTable().also { it.register(fn) }
        return Scenario(
            label = "broadleaf-super-delegation",
            genuineSelfCall = ClassifiedCall(call("getSectionKey", null, argCount = 1), fn, table),
            sameNameNotASelfCall = ClassifiedCall(call("getSectionKey", "super", argCount = 1), fn, table),
        )
    }

    // The generic form of the same bug: a call on some other object with the same method
    // name (e.g. `jobExecutor.setWaitTimeInMillis(...)` from inside `setWaitTimeInMillis`).
    private fun delegateToOtherObject(): Scenario {
        val fn = decl("setWaitTimeInMillis", "JobConfig", listOf(Parameter("millis", "int")))
        val table = SymbolTable().also { it.register(fn) }
        return Scenario(
            label = "delegate-to-other-object",
            genuineSelfCall = ClassifiedCall(call("setWaitTimeInMillis", null, argCount = 1), fn, table),
            sameNameNotASelfCall = ClassifiedCall(call("setWaitTimeInMillis", "jobExecutor", argCount = 1), fn, table),
        )
    }

    // cc #64/#71 (Fineract): the exact same call and target classify differently depending
    // on whether the symbol table reveals a same-arity sibling overload — proof that arity
    // alone (like name alone) is not enough, and resolving the overload set matters.
    private fun fineractSiblingOverload(): Scenario {
        val fn =
            decl(
                "modifyLoanApprovedAmount",
                "LoansApiResource",
                listOf(Parameter("loanId", "Long"), Parameter("uriInfo", "UriInfo"), Parameter("apiRequestBodyAsJson", "String")),
            )
        val sibling =
            decl(
                "modifyLoanApprovedAmount",
                "LoansApiResource",
                listOf(Parameter("loanId", "Long"), Parameter("loanExternalId", "ExternalId"), Parameter("apiRequestBodyAsJson", "String")),
            )
        val callNode = call("modifyLoanApprovedAmount", null, argCount = 3)
        val withoutSibling = SymbolTable().also { it.register(fn) }
        val withSibling =
            SymbolTable().also {
                it.register(fn)
                it.register(sibling)
            }
        return Scenario(
            label = "fineract-sibling-overload",
            genuineSelfCall = ClassifiedCall(callNode, fn, withoutSibling),
            sameNameNotASelfCall = ClassifiedCall(callNode, fn, withSibling),
        )
    }
}
