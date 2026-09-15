package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.signatureKey
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.TypeEnvironment
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign. isPartitionedIteration's
 * job is to tell "the inner loop iterates a per-element child collection of the outer
 * loop" (O(sum), suppress) apart from "two genuinely unrelated collections" (O(product),
 * keep flagging). Its de-pluralize-and-prefix-match check (`innerBase.startsWith(outerClean,
 * ignoreCase = true)`) had no word-boundary: "cars" -> outerClean "car" matches any inner
 * base starting with "car" — "career", "cargo", "carton" — none of which have anything to
 * do with individual cars. A genuine Cartesian-product bug over `cars` x `career` would be
 * silently suppressed. Same shape as the camelCase-prefix bugs elsewhere in this campaign,
 * just applied to a de-pluralized element name instead of a method name.
 */
internal class CardinalityExplosionRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = CardinalityExplosionRule()
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    private data class UnrelatedPrefixCase(
        val outerVar: String,
        val innerVar: String,
    )

    private val unrelatedPrefixCollisions =
        listOf(
            UnrelatedPrefixCase("cars", "career.getSomething()"),
            UnrelatedPrefixCase("items", "itemizedList.getStuff()"),
            UnrelatedPrefixCase("cats", "category.getName()"),
        )

    @Test
    fun `an unrelated variable sharing only a text prefix with the outer element is still a Cartesian product`() {
        unrelatedPrefixCollisions.forEach { case ->
            nestedLoopFindings(case.outerVar, case.innerVar) shouldHaveSize 1
        }
    }

    @Test
    fun `an exact singular-plural element match is still recognized as partitioned iteration`() {
        // Regression guard matching the existing parent-child-iteration.java fixture:
        // departments -> department.getEmployees() must stay suppressed.
        nestedLoopFindings("departments", "department.getEmployees()").shouldBeEmpty()
    }

    @Test
    fun `a compound element name at a real word boundary is still recognized as partitioned iteration`() {
        // departmentHead is still "a department", just a more specific per-element name -
        // the boundary check (uppercase right after the shared prefix) must allow this,
        // not just the exact-match case.
        nestedLoopFindings("departments", "departmentHead.getStaff()").shouldBeEmpty()
    }

    /**
     * classifyMutation treats any single-letter receiver name calling add() as a scalar
     * accumulator (BigDecimal-style), never a collection. Without a declared type to check
     * against, a short-named List (`l.add(x)`) in a genuine Cartesian nesting still falls
     * back to that name heuristic and gets misread as SCALAR_ACCUMULATION, suppressing the
     * whole group. That's expected when the receiver's type genuinely can't be resolved —
     * see the tests below for the case where a TypeEnvironment IS available, which now
     * overrides the name heuristic instead of being silently wrong.
     */
    @Test
    fun `a single-letter receiver with no resolvable type still falls back to the scalar name heuristic`() {
        nestedLoopFindings("orders", "products", mutationTarget = "l").shouldBeEmpty()
    }

    @Test
    fun `a scalar-hint-named receiver with a declared collection type is flagged, not misread as scalar`() {
        // "l" (single-letter) AND wouldn't even need the scalar-hint path to be misread,
        // but pick a name that actively looks like a scalar accumulator (sum) to prove the
        // declared type wins over the name heuristic outright, not just over the single-char case.
        nestedLoopFindingsWithDeclaredType("orders", "products", mutationTarget = "sum", declaredType = "List") shouldHaveSize 1
    }

    @Test
    fun `a scalar-hint-named receiver with a declared non-collection type still stays excluded`() {
        nestedLoopFindingsWithDeclaredType(
            "orders",
            "products",
            mutationTarget = "sum",
            declaredType = "BigDecimal",
        ).shouldBeEmpty()
    }

    /**
     * "results" gets an inferred type here purely from its initializer's method-name suffix
     * (`repository.getOrderList()` ends in "List") - the lowest-trust inference strategy
     * TypeEnvironment has, the same one isCollection/isO1 already refuse to act on. A receiver
     * with only this weak evidence must still fall through to the name-based heuristic below,
     * not get short-circuited into SCALAR_ACCUMULATION just because *some* type was inferred.
     */
    @Test
    fun `a receiver whose only type evidence is a method-name-suffix guess still falls back to the name heuristic`() {
        nestedLoopFindingsWithNameHeuristicType("orders", "products", mutationTarget = "results") shouldHaveSize 1
    }

    /**
     * Documented finding, not fixed here: scanFlatMap matches the bare method name
     * "flatMap" with no registry lookup and no receiver-type confirmation — the one hardcoded
     * name check in this file (the others already consult YAML-driven sets). A class with
     * its own unrelated flatMap() method would collide the same way a real Stream one does.
     * Fixing this properly means adding a language-aware flatmap-equivalent-methods YAML
     * section (Groovy's collectMany, other frameworks' operators) - real research, not a
     * quick local tweak, so left as a finding.
     */
    @Test
    fun `flatMap is matched by bare method name regardless of receiver (documented gap)`() {
        val innerCall = FunctionCall("stream", "other", emptyList(), loc, emptyList())
        val flatMapCall =
            FunctionCall(
                "flatMap",
                "unrelatedBuilder",
                listOf(GenericNode("lambda", loc, listOf(innerCall))),
                loc,
                listOf(GenericNode("lambda", loc, listOf(innerCall))),
            )
        rule.evaluate(fixtureContext(listOf(flatMapCall))) shouldHaveSize 1
    }

    // classifyMutation's scalarHints check had the identical missing-boundary bug, on a
    // receiver name instead of an element name: "consumerList" contains "sum" with no
    // boundary at all, misreading a genuine List.add() as a BigDecimal-style scalar
    // accumulation and suppressing the real Cartesian-product finding entirely.
    @Test
    fun `a genuine collection receiver merely containing a scalar hint without a boundary is still flagged`() {
        nestedLoopFindings("orders", "products", mutationTarget = "consumerList") shouldHaveSize 1
    }

    @Test
    fun `a genuine scalar accumulator receiver is still excluded`() {
        nestedLoopFindings("orders", "products", mutationTarget = "runningSum").shouldBeEmpty()
    }

    // isPartitionedIteration's map-entry-unpacking check (Case 1) had a bare `contains`
    // on the unanchored "values" entry - "entry.getMetaValues()" contains "values" with no
    // boundary of its own (the dot only anchors the start of "getMetaValues" as a whole, not
    // the "Values" tail inside it), so an unrelated getter was silently read as map-entry-
    // value access and the real Cartesian-product finding got suppressed.
    @Test
    fun `a call merely containing 'values' inside a longer method name is still flagged, not misread as map-entry-value access`() {
        nestedLoopFindings("grouped.entrySet()", "entry.getMetaValues()") shouldHaveSize 1
    }

    @Test
    fun `a genuine entry-value access still suppresses the map-entry-unpacking finding`() {
        nestedLoopFindings("grouped.entrySet()", "entry.getValue()").shouldBeEmpty()
    }

    // determineEffectiveSeverity's smallCollectionHints check had the same missing-boundary
    // bug - "type" is short enough that "prototypes"/"genotype"/"stereotype" all satisfied
    // it with no boundary at all, demoting a real Cartesian-product finding to INFO between
    // two otherwise unrelated variables.
    @Test
    fun `a variable merely containing 'type' without a boundary does not demote severity to INFO`() {
        val findings = nestedLoopFindings("prototypes", "products")
        findings shouldHaveSize 1
        findings.first().severity shouldBe Severity.WARNING
    }

    @Test
    fun `a genuine type-named variable still demotes severity to INFO`() {
        val findings = nestedLoopFindings("orderType", "products")
        findings shouldHaveSize 1
        findings.first().severity shouldBe Severity.INFO
    }

    private fun nestedLoopFindings(
        outerVar: String,
        innerVar: String,
        mutationTarget: String = "resultList",
    ): List<Finding> {
        val mutationCall =
            FunctionCall("add", mutationTarget, listOf(GenericNode("x", loc, emptyList())), loc, emptyList())
        val innerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = innerVar, location = loc, children = listOf(mutationCall))
        val outerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = outerVar, location = loc, children = listOf(innerLoop))
        return rule.evaluate(fixtureContext(listOf(outerLoop)))
    }

    /**
     * Same nested-loop-plus-mutation shape as [nestedLoopFindings], but wraps it in a
     * [FunctionDecl] whose [mutationTarget] parameter carries [declaredType] — so a
     * [TypeEnvironment] can actually be resolved and consulted for the receiver, instead
     * of falling back to the name-only heuristic.
     */
    private fun nestedLoopFindingsWithDeclaredType(
        outerVar: String,
        innerVar: String,
        mutationTarget: String,
        declaredType: String,
    ): List<Finding> {
        val mutationCall =
            FunctionCall("add", mutationTarget, listOf(GenericNode("x", loc, emptyList())), loc, emptyList())
        val innerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = innerVar, location = loc, children = listOf(mutationCall))
        val outerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = outerVar, location = loc, children = listOf(innerLoop))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = listOf(Parameter(mutationTarget, declaredType)),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(outerLoop),
            )
        val typeEnv = TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        return rule.evaluate(
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
                typeEnvironments = mapOf(signatureKey(fn) to typeEnv),
            ),
        )
    }

    /**
     * Same nested-loop-plus-mutation shape, but [mutationTarget] gets no declared parameter
     * type at all - its only type evidence comes from a `VariableDecl` initialized by a call
     * named to end in "List" (`getOrderList()`), which TypeEnvironment infers as a
     * NAME_HEURISTIC-sourced "List" guess, not a real declared type.
     */
    private fun nestedLoopFindingsWithNameHeuristicType(
        outerVar: String,
        innerVar: String,
        mutationTarget: String,
    ): List<Finding> {
        val fetchCall = FunctionCall("getOrderList", "repository", emptyList(), loc, emptyList())
        val varDecl = VariableDecl(mutationTarget, null, initializer = fetchCall, location = loc, children = listOf(fetchCall))
        val mutationCall =
            FunctionCall("add", mutationTarget, listOf(GenericNode("x", loc, emptyList())), loc, emptyList())
        val innerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = innerVar, location = loc, children = listOf(mutationCall))
        val outerLoop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = outerVar, location = loc, children = listOf(innerLoop))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(varDecl, outerLoop),
            )
        val typeEnv = TypeEnvironment.build(fn, emptyMap(), Language.JAVA, registry)
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        return rule.evaluate(
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
                typeEnvironments = mapOf(signatureKey(fn) to typeEnv),
            ),
        )
    }

    private fun fixtureContext(children: List<com.github.tvinke.algorilla.model.IRNode>): AnalysisContext {
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = children)
        return AnalysisContext(
            irTrees = mapOf("Fixture.java" to fileRoot),
            symbolTable = SymbolTable(),
            callGraph = CallGraph(),
            config = AnalysisConfig(),
            registry = registry,
        )
    }
}
