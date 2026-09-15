package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

/**
 * Regression coverage found by the /simplify altitude pass on the #137/#138/#141 batch:
 * [NPlusOneRepositoryCallRule.checkCallInLoop] has `language` in scope (used on the surrounding
 * lines for [isSingleRecordFetch]/[matchesRepoPattern]) but its
 * [com.github.tvinke.algorilla.util.CrossMethodResolver.resolveAndFindWithConfidence] call
 * omitted it - the exact hardcoded-Java bug #138/#141 fixed elsewhere in this same batch, missed
 * one call away from where the confidence fix landed.
 *
 * `sortedBy` is a Kotlin stdlib call absent from Java's `unresolvableNames` skiplist entirely
 * (see [com.github.tvinke.algorilla.graph.CallGraphBuilderLanguageTest] for the same
 * divergence). Under the bug, a loop calling `.sortedBy { ... }` on a Kotlin source got resolved
 * as an ordinary user function, and a coincidentally-named local `sortedBy()` containing a
 * `userRepository.findByEmail()` surfaced as a false N+1 finding.
 */
internal class NPlusOneRepositoryCallRuleLanguageTest {
    private val loc = SourceLocation("Fixture.kt", 1, 1)
    private val rule = NPlusOneRepositoryCallRule()

    @Test
    fun `does not cross-method-resolve Kotlin's sortedBy() as a user function when checking for a hidden N+1 fetch`() {
        val call = FunctionCall(name = "sortedBy", qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val caller = functionDecl("process", loop)
        // A coincidentally-named local "sortedBy" that (if ever resolved into) would report a
        // hidden single-record fetch - this must never be reached for a Kotlin source.
        val fetch =
            FunctionCall(
                name = "findByEmail",
                qualifiedTarget = "userRepository",
                arguments = emptyList(),
                location = loc,
                children = emptyList(),
            )
        val localSortedByFn = functionDecl("sortedBy", fetch)

        val symbolTable =
            SymbolTable().also {
                it.register(caller)
                it.register(localSortedByFn)
            }
        val fileRoot = FileRoot(filePath = "Fixture.kt", language = Language.KOTLIN, location = loc, children = listOf(caller))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.kt" to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )

        val findings = rule.evaluate(context)

        // Before the fix: resolveAndFindWithConfidence never received `language`, so it resolved
        // "sortedBy" against Java's (non-matching) skiplist instead of Kotlin's - wrongly
        // following it into "localSortedByFn".
        findings.shouldBeEmpty()
    }

    private fun functionDecl(
        name: String,
        vararg body: IRNode,
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Fixture.$name",
        parameters = emptyList(),
        declaringClass = "Fixture",
        location = loc,
        children = body.toList(),
    )
}
