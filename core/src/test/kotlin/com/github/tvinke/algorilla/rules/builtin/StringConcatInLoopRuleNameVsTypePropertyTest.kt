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
 * Canary property for the testharnas campaign (cc #73). isConcatCall's `call.name ==
 * "concat"` is an exact match, not a boundary-vulnerable prefix/suffix/contains check, and
 * this rule already resolves the receiver's real type first (isNonStringReceiver) before
 * ever falling back to "type unknown, flag conservatively" - the correct order, unlike the
 * isCollectionLookup bug found elsewhere this campaign. No bug found; these lock in the
 * documented behavior (declared non-String type excluded, unknown type still flagged).
 */
internal class StringConcatInLoopRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = StringConcatInLoopRule()

    @Test
    fun `concat on a known non-String type is excluded`() {
        findingsFor(target = "attrs", declaredType = "ImmutableAttributes").shouldBeEmpty()
    }

    @Test
    fun `concat on a known String type is flagged`() {
        findingsFor(target = "result", declaredType = "String") shouldHaveSize 1
    }

    @Test
    fun `concat on an unresolved type is conservatively flagged`() {
        findingsFor(target = "value", declaredType = null) shouldHaveSize 1
    }

    private fun findingsFor(
        target: String,
        declaredType: String?,
    ): List<Finding> {
        val arg = GenericNode("x", loc, emptyList())
        val call = FunctionCall("concat", target, listOf(arg), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(loop))
        val symbolTable = SymbolTable()
        if (declaredType != null) symbolTable.registerType(target, declaredType)
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = symbolTable,
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        return rule.evaluate(context)
    }
}
