package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Regression coverage for #141: [CallGraphBuilder.resolveCallee] used to call
 * [com.github.tvinke.algorilla.util.CrossMethodResolver.resolve] without a language, so it
 * always defaulted to [Language.JAVA]'s `unresolvable-names` skiplist regardless of
 * [FileRoot.language] - Kotlin, Groovy and JS sources got their entire call graph built
 * against Java's stdlib-call exclusions.
 *
 * `sortedBy` is a Kotlin stdlib call - present in Kotlin's `unresolvableNames` skiplist so it's
 * never mistaken for a call to a user-defined function - but absent from Java's skiplist
 * entirely. Under the bug, a Kotlin source's `.sortedBy { ... }` call was resolved as an
 * ordinary simple-name lookup, and a coincidentally-named local function elsewhere in the
 * project got wired up as its callee - a spurious call-graph edge for a plain stdlib call. See
 * [com.github.tvinke.algorilla.rules.builtin.IOInLoopRuleLanguageTest] for the same divergence
 * one layer up, in [com.github.tvinke.algorilla.util.ParameterFlowQuery].
 */
internal class CallGraphBuilderLanguageTest {
    private val loc = SourceLocation("Fixture.kt", 1, 1)

    @Test
    fun `does not wire a Kotlin stdlib sortedBy() call to a coincidentally-named local function`() {
        // A local function that happens to share Kotlin's stdlib "sortedBy" name - if the call
        // graph ever resolved into it, that would be a spurious edge for an ordinary stdlib call.
        val coincidentallyNamed = decl("sortedBy")
        val caller = decl("caller", call("sortedBy"))

        val graph = buildGraph(mapOf("Fixture.kt" to listOf(caller, coincidentallyNamed)), Language.KOTLIN)

        // Before the fix: resolveCallee always resolved against Java's skiplist, which doesn't
        // contain "sortedBy" - so this wrongly resolved to "coincidentallyNamed" and reported
        // one edge, "caller" -> "sortedBy", for what is really just a stdlib call.
        graph.edgeCount() shouldBe 0
    }

    /** Same fixture under Language.JAVA, where "sortedBy" genuinely is an ordinary, resolvable name. */
    @Test
    fun `still resolves an ordinary same-named Java call`() {
        val callee = decl("sortedBy")
        val caller = decl("caller", call("sortedBy"))

        val graph = buildGraph(mapOf("Fixture.java" to listOf(caller, callee)), Language.JAVA)

        graph.callees("caller") shouldBe setOf("sortedBy")
        graph.edgeCount() shouldBe 1
    }

    private fun decl(
        name: String,
        vararg children: IRNode,
    ) = FunctionDecl(name = name, qualifiedName = name, parameters = emptyList(), location = loc, children = children.toList())

    private fun call(name: String) =
        FunctionCall(name = name, qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())

    private fun buildGraph(
        fileDecls: Map<String, List<FunctionDecl>>,
        language: Language,
    ): CallGraph {
        val symbolTable = SymbolTable()
        val irTrees = mutableMapOf<String, FileRoot>()
        for ((file, decls) in fileDecls) {
            decls.forEach { symbolTable.register(it) }
            irTrees[file] = FileRoot(file, language, SourceLocation(file, 1, 1), decls)
        }
        return CallGraphBuilder(symbolTable).build(irTrees)
    }
}
