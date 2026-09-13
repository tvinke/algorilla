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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.forAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73) — this is the file the campaign
 * is named after: isCompileCall's `call.qualifiedTarget?.contains("Pattern")` is the exact
 * "receiver-text-as-type-proof" shape from the #71 RCA (ExpensiveCallbackRule's
 * `qualifiedTarget?.contains("Pattern")`/`contains("Regex")` checks, same file family).
 * `contains` matches anywhere in the string, so a totally unrelated class whose name
 * merely contains "Pattern"/"Regex" — `DateTimePatternValidator`, `NamingRegexResolver` —
 * gets its own, unrelated `compile()` method misread as `java.util.regex.Pattern.compile()`.
 */
internal class RepeatedRegexInLoopRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = RepeatedRegexInLoopRule()

    private val unrelatedClassesContainingRegexWords =
        listOf(
            "DateTimePatternValidator",
            "NamingRegexResolver",
            "PatternMatcherService",
        )

    @Test
    fun `a class whose name merely contains 'Pattern' or 'Regex' does not trigger a false regex-compile finding`() {
        runBlocking {
            forAll(Arb.of(unrelatedClassesContainingRegexWords)) { target ->
                findingsFor(target).isEmpty()
            }
        }
    }

    @Test
    fun `a real Pattern-compile call is still flagged`() {
        findingsFor("Pattern") shouldHaveSize 1
    }

    @Test
    fun `a fully-qualified Pattern reference is still flagged`() {
        findingsFor("java.util.regex.Pattern") shouldHaveSize 1
    }

    @Test
    fun `a real Regex-compile call is still flagged`() {
        findingsFor("Regex") shouldHaveSize 1
    }

    /**
     * hasConstantArgument's ALL_CAPS-or-qualified check (`text.all { it == '_' ||
     * it.isUpperCase() || it == '.' }`) is a shape heuristic too, but a narrower one: it
     * only ever promotes a finding's confidence signal (constant vs. per-iteration
     * argument), not a name-vs-type classification, and every character in a real
     * constant reference like "MY_PATTERN" or "FOO.PATTERN" already satisfies it by
     * construction. No counter-example found worth pinning as a bug; left as-is.
     */
    @Test
    fun `a qualified constant reference is still recognized as hoistable`() {
        val arg = GenericNode("FOO.PATTERN", loc, emptyList())
        val call = FunctionCall("compile", "Pattern", listOf(arg), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(loop))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )
        rule.evaluate(context) shouldHaveSize 1
    }

    private fun findingsFor(qualifiedTarget: String): List<Finding> {
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", qualifiedTarget, listOf(arg), loc, emptyList())
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
