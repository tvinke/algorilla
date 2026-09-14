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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isCompileCall here is the exact
 * same code as RepeatedRegexInLoopRule's — `qualifiedTarget?.contains("Pattern")` /
 * `contains("Regex")` matches anywhere in the receiver name, so a class like
 * DateTimePatternValidator.compile() (nothing to do with java.util.regex.Pattern) gets
 * misread as a regex compile inside a callback, both producing a false "regex compiled per
 * callback invocation" finding here AND (line 134) wrongly suppressing whatever the
 * generic-expensive-callback check would otherwise have said about that same call.
 */
internal class ExpensiveCallbackRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = ExpensiveCallbackRule()

    private val unrelatedClassesContainingRegexWords =
        listOf(
            "DateTimePatternValidator",
            "NamingRegexResolver",
        )

    @Test
    fun `a class whose name merely contains 'Pattern' or 'Regex' does not trigger a false regex-compile finding`() {
        unrelatedClassesContainingRegexWords.forEach { target ->
            findingsFor(target).none { it.message.contains("Regex compilation", ignoreCase = true) } shouldBe true
        }
    }

    @Test
    fun `a real Pattern-compile call inside a callback is still flagged`() {
        val findings = findingsFor("Pattern")
        findings shouldHaveSize 1
    }

    private fun findingsFor(qualifiedTarget: String): List<Finding> {
        val arg = GenericNode("\"[0-9]+\"", loc, emptyList())
        val call = FunctionCall("compile", qualifiedTarget, listOf(arg), loc, emptyList())
        val callback = LoopNode(kind = LoopKind.HIGHER_ORDER, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(callback))
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
