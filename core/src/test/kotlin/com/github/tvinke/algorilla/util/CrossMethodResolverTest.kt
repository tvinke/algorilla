package com.github.tvinke.algorilla.util

import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CrossMethodResolverTest {
    private val loc = SourceLocation("test.java", 1, 1)

    @Test
    fun `should resolve call via variable type when direct lookup fails`() {
        val table = SymbolTable()
        val fetchUsers = makeDecl("fetchUsers", "UserService.fetchUsers", "UserService")
        table.register(fetchUsers)
        table.registerType("service", "UserService")

        val call = makeCall("fetchUsers", target = "service")

        CrossMethodResolver.resolve(call, table) shouldBe
            ResolutionResult.Resolved(fetchUsers, ResolutionConfidence.EXACT)
    }

    @Test
    fun `should return Unresolved when variable type is unknown`() {
        val table = SymbolTable()
        val call = makeCall("fetchUsers", target = "service")

        CrossMethodResolver.resolve(call, table) shouldBe ResolutionResult.Unresolved
    }

    @Test
    fun `should prefer direct match over type lookup`() {
        val table = SymbolTable()
        val directMatch = makeDecl("process", "service.process", "service")
        val typeMatch = makeDecl("process", "OrderService.process", "OrderService")
        table.register(directMatch)
        table.register(typeMatch)
        table.registerType("service", "OrderService")

        val call = makeCall("process", target = "service")

        CrossMethodResolver.resolve(call, table) shouldBe
            ResolutionResult.Resolved(directMatch, ResolutionConfidence.EXACT)
    }

    // Table test, not a property test - see CODING_GUIDELINES.md's property-vs-example-test
    // distinction. This verifies how bestMatch classifies EXACT vs.
    // AMBIGUOUS_OVERLOAD_BEST_GUESS against a small, curated set of named resolution shapes
    // (unique name, uniquely narrowed by arity, genuinely ambiguous same-arity overload), not
    // an open input domain a generator would explore - a property test would need an
    // independently computed oracle for "is this resolution exact," which doesn't exist
    // separately from the classification logic under test here.
    // Pins worstOf's behavior directly - it's implemented as maxOf() over the enum's
    // declaration order (EXACT before AMBIGUOUS_OVERLOAD_BEST_GUESS specifically so "worse"
    // sorts higher), which nothing else exercises directly; every other test only observes it
    // indirectly through a rule's finding confidence.
    @Test
    fun `worstOf is symmetric and AMBIGUOUS always wins over EXACT`() {
        val exact = ResolutionConfidence.EXACT
        val ambiguous = ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS

        worstOf(exact, exact) shouldBe exact
        worstOf(ambiguous, ambiguous) shouldBe ambiguous
        worstOf(exact, ambiguous) shouldBe ambiguous
        worstOf(ambiguous, exact) shouldBe ambiguous
    }

    @Test
    fun `classifies resolution confidence by candidate and param-count uniqueness`() {
        confidenceScenarios().forEach { scenario ->
            val table = SymbolTable()
            scenario.candidates.forEach { table.register(it) }
            val call = makeCall(scenario.candidates.first().name, target = null, argCount = scenario.callArgCount)

            val result = CrossMethodResolver.resolve(call, table)

            withClue(scenario.description) {
                val resolved = result as? ResolutionResult.Resolved
                resolved?.confidence shouldBe scenario.expectedConfidence
            }
        }
    }

    private data class ConfidenceScenario(
        val description: String,
        val candidates: List<FunctionDecl>,
        val callArgCount: Int,
        val expectedConfidence: ResolutionConfidence,
    )

    private fun confidenceScenarios(): List<ConfidenceScenario> {
        val uniqueByName = makeDecl("uniqueName", "Service.uniqueName", "Service")
        val oneArgOverload = makeDecl("save", "Service.save", "Service", paramCount = 1)
        val twoArgOverload = makeDecl("save", "Service.save", "Service", paramCount = 2)
        val sameArityOverloadA = makeDecl("merge", "ServiceA.merge", "ServiceA", paramCount = 2)
        val sameArityOverloadB = makeDecl("merge", "ServiceB.merge", "ServiceB", paramCount = 2)

        return listOf(
            ConfidenceScenario(
                description = "a single candidate by name is EXACT, call arity irrelevant",
                candidates = listOf(uniqueByName),
                callArgCount = 0,
                expectedConfidence = ResolutionConfidence.EXACT,
            ),
            ConfidenceScenario(
                description = "an overload uniquely narrowed by parameter count is EXACT",
                candidates = listOf(oneArgOverload, twoArgOverload),
                callArgCount = 1,
                expectedConfidence = ResolutionConfidence.EXACT,
            ),
            ConfidenceScenario(
                description = "two same-arity overloads with no other way to disambiguate is a guess",
                candidates = listOf(sameArityOverloadA, sameArityOverloadB),
                callArgCount = 2,
                expectedConfidence = ResolutionConfidence.AMBIGUOUS_OVERLOAD_BEST_GUESS,
            ),
        )
    }

    @Test
    fun `resolveAndFind should not leak into an unrelated class's same-named method`() {
        // Mirrors a real shape: two unrelated classes (ClassA, ClassB) both implement a
        // shared Mapper interface and both declare merge(...) with identical arity.
        // ClassA.convert() calls merge() with an implicit receiver (no qualifiedTarget).
        // Only ClassA.merge() is actually reachable from the outer call — ClassB.merge()
        // is a coincidental same-name/same-arity method on a completely different class.
        val table = SymbolTable()

        val fourArgs = List(4) { makeArg() }
        val threeArgs = List(3) { makeArg() }
        val fourParams = List(4) { makeParam() }
        val threeParams = List(3) { makeParam() }

        val repoACall = makeCall("getById", "repoA")
        val repoBCall = makeCall("getBySku", "repoB")

        val classAMerge =
            makeDecl("merge", "ClassA.merge", "ClassA", paramCount = 4, children = listOf(repoACall))
        val classBMerge =
            makeDecl("merge", "ClassB.merge", "ClassB", paramCount = 4, children = listOf(repoBCall))
        val implicitMergeCall = makeCall("merge", target = null, argCount = 4)
        val classAConvert =
            makeDecl("convert", "ClassA.convert", "ClassA", paramCount = 3, children = listOf(implicitMergeCall))

        // Register ClassB's merge() first so an unguarded "first candidate wins"
        // fallback would pick the wrong class if the enclosing-class context were lost.
        table.register(classBMerge)
        table.register(classAMerge)
        table.register(classAConvert)
        table.registerType("fieldA", "ClassA")

        val outerCall = makeCall("convert", "fieldA", argCount = 3)

        val found =
            CrossMethodResolver.resolveAndFind<FunctionCall>(
                outerCall,
                table,
                maxDepth = 2,
            ) { it.qualifiedTarget != null && it.name.startsWith("get") }

        found shouldBe repoACall
    }

    private fun makeDecl(
        name: String,
        qualifiedName: String,
        declaringClass: String,
        paramCount: Int = 0,
        children: List<IRNode> = emptyList(),
    ) = FunctionDecl(
        name = name,
        qualifiedName = qualifiedName,
        parameters = List(paramCount) { index -> Parameter(name = "p$index", typeName = "Any") },
        declaringClass = declaringClass,
        location = loc,
        children = children,
    )

    private fun makeCall(
        name: String,
        target: String?,
        argCount: Int = 0,
    ) = FunctionCall(
        name = name,
        qualifiedTarget = target,
        arguments = List(argCount) { dummyArgument() },
        location = loc,
        children = emptyList(),
    )

    private fun dummyArgument(): IRNode = GenericNode(nodeType = "arg", location = loc, children = emptyList())
}
