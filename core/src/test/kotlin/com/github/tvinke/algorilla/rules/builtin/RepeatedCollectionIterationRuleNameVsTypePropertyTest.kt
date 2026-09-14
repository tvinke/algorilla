package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign (cc #73). checkStreamPipelines' pattern-A
 * check (`it.first.name == "stream"`) is a bare, hardcoded, single-language method name,
 * even though this rule runs on every language. Documented finding, not fixed here:
 *
 * - Real Java `.stream()` calls are correctly caught (confirmed below).
 * - Kotlin/Groovy/JS don't idiomatically call a method literally named "stream" for
 *   repeated-pipeline iteration (Kotlin's collections don't need java.util.stream at all,
 *   JS arrays have no such method) - Pattern A silently never fires for those languages.
 *   Pattern B (repeated for-each loops, a separate, language-agnostic check in this same
 *   file) already covers those languages, so this isn't a broken feature, just a narrower
 *   gap specific to the JVM-Stream-API idiom.
 * - Conversely, a class with its own unrelated method literally called "stream()" (e.g. a
 *   network/IO stream accessor) would collide the other way and get misread as a
 *   repeated-Java-Stream-pipeline.
 *
 * Properly extending Pattern A per language (Kotlin's `.asSequence()`, Groovy/JS
 * equivalents) needs research into each language's real idiom, not a mechanical fix, so
 * this is pinned as a finding rather than changed.
 */
internal class RepeatedCollectionIterationRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = RepeatedCollectionIterationRule()

    @Test
    fun `two real stream() calls on the same target are still flagged`() {
        findingsFor("stream") shouldHaveSize 1
    }

    @Test
    fun `an equivalent Kotlin-style asSequence pipeline is not recognized as the same pattern (documented gap)`() {
        findingsFor("asSequence").shouldBeEmpty()
    }

    private fun findingsFor(methodName: String): List<Finding> {
        val call1 = FunctionCall(methodName, "orders", emptyList(), loc, emptyList())
        val call2 = FunctionCall(methodName, "orders", emptyList(), loc, emptyList())
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(call1, call2),
            )
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
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
