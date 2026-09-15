package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign. isBulkLoadCall's
 * `bulkLoadPrefixes.any { call.name.startsWith(it, ignoreCase = true) }` has the same
 * missing word boundary already fixed elsewhere this campaign: "findAllocation()" (a
 * single-entity fetch) starts with "findAll" with no real boundary and gets misread as a
 * bulk-load call.
 */
internal class FullScanForSingleLookupRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val registry = LanguageSemanticsRegistry.loadDefaults()
    private val bulkLoadPrefixes = registry.bulkLoadPrefixes(Language.JAVA)
    private val domTargets = registry.domTargetNames(Language.JAVA)

    @Test
    fun `findAllocation is not misread as a bulk-load call just because of the prefix`() {
        val call = FunctionCall("findAllocation", "allocationRepository", listOf(GenericNode("id", loc, emptyList())), loc, emptyList())
        isBulkLoadCall(call, bulkLoadPrefixes, domTargets) shouldBe false
    }

    @Test
    fun `a genuine findAllOrders bulk-load call is still recognized`() {
        val call = FunctionCall("findAllOrders", "orderRepository", emptyList(), loc, emptyList())
        isBulkLoadCall(call, bulkLoadPrefixes, domTargets) shouldBe true
    }

    // dom-target-names' "dom"/"el" entries had no word boundary at all. "random"/"freedom"
    // contain "dom", "model"/"channel" contain "el", none of them DOM/test-framework targets.
    @Test
    fun `a target merely containing dom or el without a boundary is still a genuine bulk-load call`() {
        val randomCall = FunctionCall("findAllOrders", "randomOrderRepository", emptyList(), loc, emptyList())
        isBulkLoadCall(randomCall, bulkLoadPrefixes, domTargets) shouldBe true
        val modelCall = FunctionCall("findAllOrders", "modelOrderRepository", emptyList(), loc, emptyList())
        isBulkLoadCall(modelCall, bulkLoadPrefixes, domTargets) shouldBe true
    }

    @Test
    fun `a genuine DOM wrapper target is still excluded`() {
        val call = FunctionCall("findAllOrders", "wrapper", emptyList(), loc, emptyList())
        isBulkLoadCall(call, bulkLoadPrefixes, domTargets) shouldBe false
    }

    /**
     * "wrapper" is on the DOM-target exclusion list by name alone - but a field genuinely
     * declared as a repository type just happens to be called "wrapper" here, and the
     * exclusion should not apply once the real type is known.
     */
    @Test
    fun `a repository field merely named like a DOM target is not excluded once its declared type is known`() {
        val call = FunctionCall("findAllOrders", "wrapper", emptyList(), loc, emptyList())
        val typeEnv = typeEnvFor("wrapper", "OrderRepository")
        isBulkLoadCall(call, bulkLoadPrefixes, domTargets, typeEnv) shouldBe true
    }

    @Test
    fun `a genuine DOM wrapper is still excluded once its declared type confirms it, even under a different name`() {
        val call = FunctionCall("findAllOrders", "cmp", emptyList(), loc, emptyList())
        val typeEnv = typeEnvFor("cmp", "Wrapper")
        isBulkLoadCall(call, bulkLoadPrefixes, domTargets, typeEnv) shouldBe false
    }

    /**
     * "wrapper" is on the DOM-target exclusion list by name alone - the name heuristic
     * correctly excludes it. But its only type evidence here comes from an initializer's
     * method-name suffix ("helper.getResultList()" ends in "List") - the lowest-trust
     * NAME_HEURISTIC source, same one isCollection/isO1 already refuse to act on. A raw
     * typeOf().simpleName read that irrelevant "List" guess as a real declared type, found
     * it doesn't match domTargets, and let the call through as a bulk-load call - overriding
     * a genuine DOM-wrapper exclusion instead of falling back to the name check.
     */
    @Test
    fun `a DOM-wrapper-named variable with only a name-heuristic-inferred type is still excluded`() {
        val call = FunctionCall("findAllOrders", "wrapper", emptyList(), loc, emptyList())
        val typeEnv = typeEnvWithNameHeuristicType("wrapper")
        isBulkLoadCall(call, bulkLoadPrefixes, domTargets, typeEnv) shouldBe false
    }

    private fun typeEnvWithNameHeuristicType(varName: String): TypeEnvironment {
        val listyInit = FunctionCall("getResultList", "helper", emptyList(), loc, emptyList())
        val varDecl = VariableDecl(varName, null, initializer = listyInit, location = loc, children = listOf(listyInit))
        val fn =
            FunctionDecl(
                name = "handler",
                qualifiedName = "Fixture.handler",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl),
            )
        return TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
    }

    private fun typeEnvFor(
        varName: String,
        declaredType: String,
    ): TypeEnvironment {
        val fn =
            FunctionDecl(
                name = "handler",
                qualifiedName = "Fixture.handler",
                parameters = listOf(Parameter(varName, declaredType)),
                declaringClass = "Fixture",
                location = loc,
                children = emptyList(),
            )
        return TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
    }
}
