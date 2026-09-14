package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign (cc #73). isBulkLoadCall's
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
}
