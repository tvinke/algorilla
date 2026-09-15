package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

/**
 * Regression coverage found by the /simplify altitude pass on the #137/#138/#141 batch:
 * unlike [ExpensiveSortComparatorRule] and [NPlusOneRepositoryCallRule] (which at least carried
 * `language` as a parameter and simply forgot to forward it), [SortForLastRule] never threaded
 * [com.github.tvinke.algorilla.model.FileRoot.language] through its scan chain
 * (`scanNode`/`checkFunction`/`checkSortWithCrossMethodAccess`/`checkAccessWithCrossMethodSort`)
 * at all, despite declaring `languages = Language.entries.toSet()` - structurally the same root
 * cause as #138/#141, just missed entirely rather than dropped at one call site.
 *
 * `sortedBy` is a Kotlin stdlib call absent from Java's `unresolvableNames` skiplist entirely
 * (see [com.github.tvinke.algorilla.graph.CallGraphBuilderLanguageTest] for the same
 * divergence). Under the bug, a Kotlin source's `.sortedBy { ... }` call near a sort got
 * resolved as an ordinary user function, and a coincidentally-named local `sortedBy()`
 * containing a `.first()` access surfaced as a false "sort for first" finding.
 */
internal class SortForLastRuleLanguageTest {
    private val loc = SourceLocation("Fixture.kt", 1, 1)
    private val rule = SortForLastRule()

    @Test
    fun `does not cross-method-resolve Kotlin's sortedBy() as a user function when linking a sort to a cross-method access`() {
        val sort = SortCall(kind = SortKind.SORT, comparatorBody = null, location = loc, children = emptyList())
        val call =
            FunctionCall(
                name = "sortedBy",
                qualifiedTarget = null,
                arguments = emptyList(),
                location = SourceLocation("Fixture.kt", 2, 1),
                children = emptyList(),
            )
        val caller = functionDecl("process", sort, call)
        // A coincidentally-named local "sortedBy" that (if ever resolved into) would report a
        // hidden first-element access - this must never be reached for a Kotlin source.
        val access = CollectionAccess(kind = AccessKind.FIRST, location = loc, children = emptyList())
        val localSortedByFn = functionDecl("sortedBy", access)

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

        // Before the fix: language was never threaded through this rule at all, so
        // resolveAndFindWithConfidence resolved "sortedBy" against Java's (non-matching)
        // skiplist instead of Kotlin's - wrongly following it into "localSortedByFn".
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
