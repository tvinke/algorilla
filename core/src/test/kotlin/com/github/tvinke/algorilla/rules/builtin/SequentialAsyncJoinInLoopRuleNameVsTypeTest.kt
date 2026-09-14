package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
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
 * Canary property for looksLikeFutureCall.
 * The target was lowercased before a bare contains() against future-indicators (future,
 * promise, async, completable, deferred, task), no boundary either side. "task" is a common
 * enough word that "subtask" (a generic subtask concept, not necessarily async) satisfied it
 * too, and the lowercasing destroyed the camelCase signal a boundary check needs.
 */
internal class SequentialAsyncJoinInLoopRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = SequentialAsyncJoinInLoopRule()

    @Test
    fun `a blocking join on a genuine future-named target is still flagged`() {
        findingsFor("myTask") shouldHaveSize 1
        findingsFor("future") shouldHaveSize 1
    }

    @Test
    fun `a blocking join on a target merely containing task without a boundary is not flagged`() {
        findingsFor("subtask").shouldBeEmpty()
    }

    private fun findingsFor(target: String): List<Finding> {
        val call = FunctionCall("join", target, emptyList(), loc, emptyList())
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
