package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.TypeSource
import com.github.tvinke.algorilla.model.VariableDecl
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.forAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). TypeEnvironment's inference
 * strategies each carry a [TypeSource], and the whole point of that provenance is that
 * low-trust, name-shaped evidence must not be treated as proof once a caller asks a
 * yes/no question ("is this an O(1) type? a collection?"). [TypeEnvironmentTest] already
 * pins that [TypeEnvironment.isO1] rejects [TypeSource.NAME_HEURISTIC] on purpose — this
 * is the #64/#66/#70/#71 pattern (a helper that changes its verdict because a name
 * matched, not because the thing actually resolved to what the name suggests) showing up
 * again, in a different helper.
 */
internal class TypeEnvironmentNameVsTypePropertyTest {
    private val registry = LanguageSemanticsRegistry.loadDefaults()
    private val loc = SourceLocation("Test.java", 1, 1)

    private fun fn(children: List<IRNode>) =
        FunctionDecl(
            name = "test",
            qualifiedName = "TestClass.test",
            parameters = emptyList(),
            location = loc,
            children = children,
        )

    private fun varDecl(
        name: String,
        children: List<IRNode>,
    ) = VariableDecl(name, null, location = loc, children = children)

    private fun functionCall(
        name: String,
        qualifiedTarget: String? = null,
    ) = FunctionCall(name, qualifiedTarget, emptyList(), loc, emptyList())

    private data class NameHeuristicCase(
        val methodName: String,
        val expectedSimpleName: String,
    )

    // Each of these is a plausible domain method whose name happens to end in a
    // collection-shaped suffix, without the call carrying any other evidence of its
    // actual return type (no declared type, no constructor, no factory, no chain, no
    // known return type anywhere) — exactly the branch that falls through to
    // inferFromMethodNameSuffix.
    private val nameHeuristicCases =
        listOf(
            NameHeuristicCase("getWorkflowStateMap", "Map"),
            NameHeuristicCase("getAllowedSet", "Set"),
            NameHeuristicCase("getRecentOrderList", "List"),
            NameHeuristicCase("computeEventStream", "Stream"),
        )

    /**
     * cc #73 finding: isCollection/isList made no such exclusion — a variable whose
     * *only* evidence is a method name ending in Map/Set/List/Stream (e.g. a getter on a
     * domain value object that happens to be called `getWorkflowStateMap()` but returns a
     * `WorkflowStateMap` record, not a real `java.util.Map`) was silently trusted as a
     * real collection by isCollection/isList. That's a real reach: NestedLookupRule's
     * `isTypeConfirmedCollection` and IOInLoopRule's `isInMemoryTarget` both gate on
     * isCollection as positive proof a variable is a genuine, iterable collection.
     */
    @Test
    fun `name-heuristic types are not trusted as proof of collection-ness, like isO1 already requires`() {
        runBlocking {
            forAll(Arb.of(nameHeuristicCases)) { case ->
                val env =
                    TypeEnvironment.build(
                        fn(listOf(varDecl("target", listOf(functionCall(case.methodName))))),
                        emptyMap(),
                        Language.JAVA,
                        registry,
                    )
                val type = env.typeOf("target")!!
                type.source == TypeSource.NAME_HEURISTIC &&
                    type.simpleName == case.expectedSimpleName &&
                    !env.isCollection("target") &&
                    !env.isList("target")
            }
        }
    }

    @Test
    fun `a declared List still passes isCollection and isList (heuristic exclusion does not swallow real types)`() {
        val env =
            TypeEnvironment.build(
                fn(listOf(VariableDecl("target", "ArrayList", location = loc, children = emptyList()))),
                emptyMap(),
                Language.JAVA,
                registry,
            )
        env.isCollection("target") shouldBe true
        env.isList("target") shouldBe true
    }

    /**
     * inferFromCrossFileReturnType (L1b) falls back to an unqualified suffix match
     * (`entries.filter { it.key.endsWith(".$methodName)" } }`) only when it is
     * unambiguous. This property locks in that the ambiguity guard survives regardless
     * of which two classes happen to declare the same method name — the fallback must
     * stay silent (null) rather than guess between them.
     */
    @Test
    fun `cross-file suffix fallback stays silent when two classes share a method name, whoever they are`() {
        runBlocking {
            forAll(Arb.of(crossFileAmbiguityCases)) { case ->
                val context =
                    TypeContext(
                        globalMethodReturnTypes =
                            mapOf(
                                "${case.first}.${case.methodName}" to "List",
                                "${case.second}.${case.methodName}" to "Set",
                            ),
                    )
                val env =
                    TypeEnvironment.build(
                        fn(listOf(varDecl("items", listOf(functionCall(case.methodName))))),
                        context,
                        Language.JAVA,
                        registry,
                    )
                env.typeOf("items") == null
            }
        }
    }

    private data class CrossFileAmbiguityCase(
        val first: String,
        val second: String,
        val methodName: String,
    )

    private val crossFileAmbiguityCases =
        listOf(
            CrossFileAmbiguityCase("UserService", "OrderService", "getAll"),
            CrossFileAmbiguityCase("ReportBuilder", "InvoiceBuilder", "build"),
            CrossFileAmbiguityCase("LeftRepository", "RightRepository", "findAll"),
        )

    @Test
    fun `cross-file suffix fallback resolves the single unambiguous candidate`() {
        val context = TypeContext(globalMethodReturnTypes = mapOf("UserService.getAll" to "List"))
        val env =
            TypeEnvironment.build(
                fn(listOf(varDecl("items", listOf(functionCall("getAll"))))),
                context,
                Language.JAVA,
                registry,
            )
        env.typeOf("items")!!.simpleName shouldBe "List"
        env.typeOf("items")!!.source shouldBe TypeSource.CROSS_FILE_RETURN
    }

    /**
     * Documented finding (cc #73), not fixed here: inferChainEnd infers a type from the
     * *name* of the last call in a chain (`toList`, `toArray`, `collect`, ...) with no
     * check that the receiver is actually a Stream/Collection pipeline at all. A class
     * that happens to define its own method literally called `toList()` — e.g. a
     * domain-specific serializer — gets the exact same CHAIN_END-sourced "List" type as a
     * real `.stream().collect(toList())`. Unlike NAME_HEURISTIC, CHAIN_END is *not*
     * excluded anywhere (isO1/isCollection/isList all trust it). This property pins the
     * current, name-driven behavior rather than silently relying on it: widening the
     * exclusion to CHAIN_END is a bigger, cross-cutting trust change (it would affect
     * every rule that calls isCollection/isO1) and needs the Fitness/Veto loop to
     * validate before it lands, not a local TypeEnvironment patch — see the reply to
     * 'main' for the full writeup.
     */
    @Test
    fun `chain-end terminal-op matching trusts the call name alone, not the receiver (documented gap)`() {
        val env =
            TypeEnvironment.build(
                fn(
                    listOf(
                        varDecl(
                            "serialized",
                            listOf(functionCall("toList", qualifiedTarget = "MyCustomSerializer")),
                        ),
                    ),
                ),
                emptyMap(),
                Language.JAVA,
                registry,
            )
        val type = env.typeOf("serialized")!!
        type.simpleName shouldBe "List"
        type.source shouldBe TypeSource.CHAIN_END
        // Because CHAIN_END is trusted everywhere NAME_HEURISTIC is not, this reads as a
        // real List downstream even though "MyCustomSerializer.toList()" has nothing to
        // do with java.util.List.
        env.isCollection("serialized") shouldBe true
    }
}
