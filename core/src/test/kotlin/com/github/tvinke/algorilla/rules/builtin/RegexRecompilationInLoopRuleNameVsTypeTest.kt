package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isMapTarget and
 * isNonRegexMatchesTarget both follow the same shape: check a declared type first, then
 * fall back to a name heuristic "for cases without type info". But neither actually
 * distinguishes "no type info" from "type info says no" — when the declared type is
 * known and definitely NOT a Map/Predicate/Pattern/Matcher, the code still falls through
 * to the heuristic below, where a coincidental name match (a `String queryCache`, a
 * `String matcherName`) overrides the type that was already resolved. Same mechanism,
 * same root cause, two call sites in one file — the #64/#66/#70/#71 pattern this
 * campaign is hunting for.
 */
internal class RegexRecompilationInLoopRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = RegexRecompilationInLoopRule()

    private data class MapNameCase(
        val varName: String,
        val declaredType: String,
    )

    // Names chosen from non-list-targets-suffixes (java.yml) — cache/table/set/map — paired
    // with a declared type that is definitely not O1, so the only reason isMapTarget could
    // say "yes" is the name.
    private val mapNameCollisionCases =
        listOf(
            MapNameCase("queryCache", "String"),
            MapNameCase("lookupTable", "String"),
            MapNameCase("allowedSet", "String"),
            MapNameCase("resultMap", "List"),
        )

    @Test
    fun `a String or List whose name coincidentally ends in a Map suffix still gets flagged`() {
        mapNameCollisionCases.forEach { case ->
            val findings = evaluateReplaceAllInLoop(case.varName, case.declaredType)
            findings.size shouldBe 1
        }
    }

    @Test
    fun `an untyped variable ending in a Map suffix still falls back to the name heuristic`() {
        // No declared type at all (real-world case: dynamically typed / unresolved param) —
        // the heuristic fallback must still apply here, otherwise every Map.replaceAll() in a
        // loop with an untyped receiver would false-positive.
        val findings = evaluateReplaceAllInLoop("configCache", declaredType = null)
        findings.shouldBeEmpty()
    }

    @Test
    fun `a real Map still gets excluded regardless of its variable name`() {
        val findings = evaluateReplaceAllInLoop("orders", "HashMap")
        findings.shouldBeEmpty()
    }

    @Test
    fun `an untyped variable that merely ends in a Map suffix without a word boundary still gets flagged`() {
        // "vegetable" ends in "table" (a non-list-target suffix) but is not "a table" in
        // any collection sense - the endsWith check inside the name heuristic itself
        // needs the same word-boundary guard as the declared-type-vs-heuristic fix above.
        val findings = evaluateReplaceAllInLoop("vegetable", declaredType = null)
        findings.size shouldBe 1
    }

    private data class MatchesNameCase(
        val varName: String,
        val declaredType: String,
    )

    private val matchesNameCollisionCases =
        listOf(
            MatchesNameCase("matcherName", "String"),
            MatchesNameCase("predicateLabel", "String"),
        )

    @Test
    fun `a String whose name coincidentally contains 'matcher' or 'predicate' still gets flagged`() {
        matchesNameCollisionCases.forEach { case ->
            val findings = evaluateMatchesInLoop(case.varName, case.declaredType)
            findings.size shouldBe 1
        }
    }

    @Test
    fun `an untyped variable named like a matcher still falls back to the name heuristic`() {
        val findings = evaluateMatchesInLoop("matcher", declaredType = null)
        findings.shouldBeEmpty()
    }

    @Test
    fun `a real Pattern still gets excluded regardless of its variable name`() {
        val findings = evaluateMatchesInLoop("regex", "Pattern")
        findings.shouldBeEmpty()
    }

    // -- hasSingleCharNonRegexArg / stringLiteralContent: quote-style should not matter --

    private val quoteStyles = listOf("\"", "'", "`")

    @Test
    fun `split fast-path applies for any single non-metachar regardless of quote style`() {
        quoteStyles.forEach { quote ->
            evaluateSplitInLoop(quotedLiteral("_", quote)).shouldBeEmpty()
        }
    }

    @Test
    fun `split still flags a single metacharacter regardless of quote style`() {
        quoteStyles.forEach { quote ->
            evaluateSplitInLoop(quotedLiteral(".", quote)).size shouldBe 1
        }
    }

    private fun quotedLiteral(
        content: String,
        quote: String,
    ) = "$quote$content$quote"

    private fun evaluateReplaceAllInLoop(
        varName: String,
        declaredType: String?,
    ): List<com.github.tvinke.algorilla.rules.Finding> {
        val call = FunctionCall("replaceAll", varName, listOf(literal("\"[0-9]+\"")), loc, emptyList())
        return evaluate(varName, declaredType, call)
    }

    private fun evaluateMatchesInLoop(
        varName: String,
        declaredType: String?,
    ): List<com.github.tvinke.algorilla.rules.Finding> {
        val call = FunctionCall("matches", varName, listOf(literal("\"[0-9]+\"")), loc, emptyList())
        return evaluate(varName, declaredType, call)
    }

    private fun evaluateSplitInLoop(quotedArg: String): List<com.github.tvinke.algorilla.rules.Finding> {
        val call = FunctionCall("split", "text", listOf(literal(quotedArg)), loc, emptyList())
        return evaluate("text", "String", call)
    }

    private fun literal(text: String) = GenericNode(nodeType = text, location = loc, children = emptyList())

    private fun evaluate(
        varName: String,
        declaredType: String?,
        call: FunctionCall,
    ): List<com.github.tvinke.algorilla.rules.Finding> {
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fn =
            FunctionDecl(
                name = "process",
                qualifiedName = "Fixture.process",
                parameters = if (declaredType != null) listOf(Parameter(varName, declaredType)) else emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = listOf(loop),
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
