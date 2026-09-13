package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isRepositoryFetch and
 * isLazyCollectionGetter both classify a method name by a bare prefix list with no
 * word-boundary check, the same shape already fixed in RedundantExpensiveCallRule,
 * IOInLoopRule and NPlusOneRepositoryCallRule this campaign:
 *
 * - isRepositoryFetch's `fetchPrefixes.any { call.name.startsWith(it, ignoreCase = true) }`
 *   treats "findAllocation" (a single-entity fetch) the same as a genuine "findAll" bulk
 *   query, wrongly marking its result variable as a bulk-loaded entity collection.
 * - isLazyCollectionGetter's `name.startsWith("get")` has no boundary either: "getaways()"
 *   (a domain method, e.g. Trip.getaways()) is read as a getter on property "aways", which
 *   passes the plural-name heuristic and gets misclassified as a lazy collection getter.
 */
internal class LazyLoadingInLoopRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = LazyLoadingInLoopRule()

    @Test
    fun `a single-entity fetch method is not misread as a bulk findAll just because of the prefix`() {
        findingsFor(fetchMethod = "findAllocation", getterMethod = "getOrders").shouldBeEmpty()
    }

    @Test
    fun `a genuine findAll bulk fetch still establishes the entity variable`() {
        findingsFor(fetchMethod = "findAllOrders", getterMethod = "getOrders") shouldHaveSize 1
    }

    @Test
    fun `a domain method that merely starts with 'get' is not misread as a getter`() {
        // "getaways" (Trip.getaways()) - starts with "get" with no real word boundary,
        // and "aways" happens to look like a plural, non-scalar property name.
        findingsFor(fetchMethod = "findAllOrders", getterMethod = "getaways").shouldBeEmpty()
    }

    @Test
    fun `a genuine camelCase getter is still recognized`() {
        findingsFor(fetchMethod = "findAllOrders", getterMethod = "getOrders") shouldHaveSize 1
    }

    /**
     * Documented finding (cc #73), not fixed here: isLazyCollectionGetter's second
     * condition (`collGetterNames.any { lower.contains(it) }`) has no scalar-suffix
     * exclusion at all, unlike its sibling condition just above it. `getChildrenCount()`
     * genuinely contains "Children" as a real capitalized word (not a text-matching
     * artifact — a word-boundary check would still accept it), but it returns a scalar
     * count, not the children collection itself. Distinguishing that needs a
     * scalar-aggregate-suffix exclusion (Count/Size/Total/...) designed for this specific
     * check, not a mechanical boundary fix like the others in this file — a new heuristic
     * decision, not a restoration of an existing one, so left as a finding.
     */
    @Test
    fun `getXxxCount is misread as a lazy collection getter via the unguarded collGetterNames check (documented gap)`() {
        findingsFor(fetchMethod = "findAllOrders", getterMethod = "getChildrenCount") shouldHaveSize 1
    }

    private fun findingsFor(
        fetchMethod: String,
        getterMethod: String,
    ): List<Finding> {
        val fetchCall = FunctionCall(fetchMethod, "orderRepository", emptyList(), loc, emptyList())
        val entityVar = VariableDecl("orders", null, initializer = fetchCall, location = loc, children = listOf(fetchCall))
        val getterCall = FunctionCall(getterMethod, "element", emptyList(), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "orders", location = loc, children = listOf(getterCall))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(entityVar, loop),
            )
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
