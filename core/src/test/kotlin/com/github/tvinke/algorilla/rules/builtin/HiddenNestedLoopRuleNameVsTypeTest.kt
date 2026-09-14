package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
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
 * Canary property for the testharnas campaign. isStringOrCopyMethod's
 * `hiddenLoopSkipPrefixes.any { lower.startsWith(it) }` has the same missing word boundary
 * as everywhere else this campaign: a call literally named "reader()"/"writer()" (an
 * unrelated method, nothing to do with stream reading/writing) starts with skip-prefixes
 * "read"/"write" with no real camelCase boundary, so checkForHiddenLoop returns before
 * even trying to resolve it - a genuine hidden-nested-loop bug inside that method would be
 * silently invisible.
 */
internal class HiddenNestedLoopRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = HiddenNestedLoopRule()

    @Test
    fun `a call named reader or writer is not skipped just because of the prefix`() {
        findingsFor("reader") shouldHaveSize 1
        findingsFor("writer") shouldHaveSize 1
    }

    @Test
    fun `a genuine read or write call is still skipped`() {
        findingsFor("readLine").shouldBeEmpty()
        findingsFor("writeLine").shouldBeEmpty()
    }

    // isStringOrCopyMethod's other bare-contains check (hidden-loop-skip-keywords: "char",
    // "uri", "pattern", ...) had the same missing boundary on both sides, confirmed by the
    // #103 spike review - "processCharge" contains "char" with no boundary at all, so a
    // genuine hidden-nested-loop bug inside a billing method was silently invisible.
    @Test
    fun `a call merely containing a skip-keyword without a boundary is not skipped`() {
        findingsFor("processCharge") shouldHaveSize 1
    }

    @Test
    fun `a genuine char-iteration call is still skipped`() {
        findingsFor("toCharArray").shouldBeEmpty()
    }

    private fun resolvableMethodWithHiddenLoop(name: String): FunctionDecl {
        val innerCall1 = FunctionCall("process", "element", emptyList(), loc, emptyList())
        val innerCall2 = FunctionCall("validate", "element", emptyList(), loc, emptyList())
        val innerLoop =
            LoopNode(
                kind = LoopKind.FOR_EACH,
                iteratedVariable = "elements",
                location = loc,
                children = listOf(innerCall1, innerCall2),
            )
        return FunctionDecl(
            name = name,
            qualifiedName = "Utils.$name",
            parameters = emptyList(),
            declaringClass = "Utils",
            location = loc,
            children = listOf(innerLoop),
        )
    }

    private fun findingsFor(outerCallName: String): List<Finding> {
        val resolvedFn = resolvableMethodWithHiddenLoop(outerCallName)
        val outerCall = FunctionCall(outerCallName, null, emptyList(), loc, emptyList())
        val outerLoop =
            LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "orders", location = loc, children = listOf(outerCall))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(outerLoop))
        val symbolTable = SymbolTable().also { it.register(resolvedFn) }
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
