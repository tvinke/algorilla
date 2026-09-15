package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign. isRemovalCall's
 * `nonListTargetsSuffixes(lang).any { suffix -> lower.endsWith(suffix) }` has the same
 * missing word boundary already fixed elsewhere this campaign: "vegetable" ends in "table"
 * (a non-list-target suffix) with no real boundary and isn't a Map/O(1) type in any sense,
 * so a genuine List.remove() call on it should still be flagged as O(n) removal-in-loop.
 */
internal class QuadraticRemovalRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = QuadraticRemovalRule()

    @Test
    fun `remove on a variable that merely ends in a Map suffix without a boundary is still flagged`() {
        findingsFor("vegetable") shouldHaveSize 1
    }

    @Test
    fun `remove on a genuine Map-suffixed variable is still excluded`() {
        findingsFor("lookupTable").shouldBeEmpty()
    }

    // isRemovalCall's other bare-contains check (non-list-targets-contains: "store",
    // "repository", "dao", ...) had the same missing boundary, confirmed by the #103 spike
    // review - "restoreList" contains "store" with no boundary at all, so a genuine
    // ArrayList.remove() in a loop over a restore-related list was silently excluded.
    @Test
    fun `remove on a variable merely containing a non-list-target word without a boundary is still flagged`() {
        findingsFor("restoreList") shouldHaveSize 1
    }

    @Test
    fun `remove on a genuine repository-style target is still excluded`() {
        findingsFor("userRepository").shouldBeEmpty()
        findingsFor("orderStore").shouldBeEmpty()
    }

    private fun findingsFor(target: String): List<Finding> {
        val arg = GenericNode("x", loc, emptyList())
        val call = FunctionCall("remove", target, listOf(arg), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(loop))
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
