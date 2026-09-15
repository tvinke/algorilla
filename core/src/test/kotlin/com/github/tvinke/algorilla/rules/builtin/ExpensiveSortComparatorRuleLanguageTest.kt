package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

/**
 * Regression coverage found by the /simplify altitude pass on the #137/#138/#141 batch:
 * [ExpensiveSortComparatorRule.checkCrossMethodDate] has `language` in scope (used two lines
 * earlier for [isDateType]/[isDateParseCall]) but its two
 * [com.github.tvinke.algorilla.util.CrossMethodResolver.resolveAndFindWithConfidence] calls
 * omitted it entirely - the exact hardcoded-Java bug #138/#141 fixed elsewhere in this same
 * batch, missed one call away from where the confidence fix landed.
 *
 * `sortedBy` is a Kotlin stdlib call absent from Java's `unresolvableNames` skiplist entirely
 * (see [com.github.tvinke.algorilla.graph.CallGraphBuilderLanguageTest] for the same
 * divergence). Under the bug, a Kotlin comparator body calling `.sortedBy { ... }` got resolved
 * as an ordinary user function, and a coincidentally-named local `sortedBy()` containing a
 * `new Date()` surfaced as a false "date operation inside sort comparator" finding.
 */
internal class ExpensiveSortComparatorRuleLanguageTest {
    private val loc = SourceLocation("Fixture.kt", 1, 1)
    private val rule = ExpensiveSortComparatorRule()

    @Test
    fun `does not cross-method-resolve Kotlin's sortedBy() as a user function when checking for hidden date ops`() {
        val call = FunctionCall(name = "sortedBy", qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
        val sort =
            SortCall(kind = SortKind.SORT, comparatorBody = listOf(call), location = loc, children = emptyList())
        // A coincidentally-named local "sortedBy" that (if ever resolved into) would report a
        // hidden `new Date()` - this must never be reached for a Kotlin source.
        val dateCreation = ObjectCreation(typeName = "Date", location = loc, children = emptyList())
        val localSortedByFn = functionDecl("sortedBy", dateCreation)

        val symbolTable = SymbolTable().also { it.register(localSortedByFn) }
        val fileRoot = FileRoot(filePath = "Fixture.kt", language = Language.KOTLIN, location = loc, children = listOf(sort))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.kt" to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )

        val findings = rule.evaluate(context)

        // Before the fix: the two resolveAndFindWithConfidence calls in checkCrossMethodDate
        // never passed `language`, so they resolved "sortedBy" against Java's (non-matching)
        // skiplist instead of Kotlin's - wrongly following it into "localSortedByFn".
        findings.shouldBeEmpty()
    }

    private fun functionDecl(
        name: String,
        vararg body: com.github.tvinke.algorilla.model.IRNode,
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Fixture.$name",
        parameters = emptyList(),
        declaringClass = "Fixture",
        location = loc,
        children = body.toList(),
    )
}
