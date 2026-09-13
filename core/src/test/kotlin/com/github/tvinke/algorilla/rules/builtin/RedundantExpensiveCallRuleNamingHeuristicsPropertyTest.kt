package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Finding
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.forAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isSequentialReadPrefix decides
 * whether a repeated call is a "sequential read" (Iterator.next()-style — each call is
 * legitimately different, don't suggest caching) purely from whether the name starts with
 * "read"/"next". Its two siblings in this file, isTypeCheckPredicate and isGetterPattern,
 * both require the character right after the prefix to be uppercase — a real camelCase
 * word boundary — before treating it as a match, precisely so "total" doesn't get read as
 * "to" + "tal". isSequentialReadPrefix skipped that check, so a plain getter like
 * `readable()` (`read` + lowercase `able`) or `nextdoor()` was silently treated the same as
 * `readVarInt()`/`nextToken()` and excluded from redundant-call detection entirely — a
 * real, callable false negative, not just a theoretical one.
 *
 * (Note: a literal `reader()` is deliberately *not* used as a collision example here —
 * Jackson's overlay lists it as a genuine cheap builder method (`ObjectMapper.reader()`),
 * so it is correctly excluded for an unrelated reason regardless of this fix.)
 */
internal class RedundantExpensiveCallRuleNamingHeuristicsPropertyTest {
    private val rule = RedundantExpensiveCallRule()

    private val readNextBoundaryCollisions = listOf("readable", "nextdoor", "nextgen", "readworthy")

    @Test
    fun `a getter that merely starts with 'read' or 'next' without a real word boundary is still flagged as redundant`() {
        runBlocking {
            forAll(Arb.of(readNextBoundaryCollisions)) { name ->
                evaluateTwoIdenticalCalls(name).size == 1
            }
        }
    }

    private val genuineReadNextPrefixMatches = listOf("readVarInt", "readCString", "nextToken", "nextElement")

    @Test
    fun `a genuine readXxx or nextXxx call is still excluded from redundant-call detection`() {
        runBlocking {
            forAll(Arb.of(genuineReadNextPrefixMatches)) { name ->
                evaluateTwoIdenticalCalls(name).isEmpty()
            }
        }
    }

    @Test
    fun `exact sequential-read methods stay excluded regardless of the prefix fix`() {
        evaluateTwoIdenticalCalls("read").shouldBeEmpty()
    }

    // -- isTypeCheckPredicate / isGetterPattern: same word-boundary rule, already correct --

    private val typeCheckWordBoundaryNonMatches = listOf("total", "island", "hastily")

    @Test
    fun `type-check and getter prefixes require a real word boundary, not just a string prefix`() {
        runBlocking {
            forAll(Arb.of(typeCheckWordBoundaryNonMatches)) { name ->
                // None of these are type-check/getter patterns ("is"+"land" isn't isLand,
                // "has"+"tily" isn't hasTily) - two identical calls must still be flagged
                // at the MIN_DUPLICATES threshold, not silently skipped as accessors.
                evaluateTwoIdenticalCalls(name).size == 1
            }
        }
    }

    // -- isBytecodeInstruction: documented gap, not fixed here --

    @Test
    fun `an all-caps underscore name is treated as a bytecode instruction even when it is a real method (documented gap)`() {
        // "_DEFAULT_CONFIG" reads like a decompiled bytecode op (_ALOAD, _ISTORE) to
        // isBytecodeInstruction, but nothing rules out a real codebase using that ALL_CAPS
        // convention for a constant-style accessor. Left as-is: narrowing it needs a real
        // signal (declaring class looks like decompiled bytecode) that isn't available here,
        // and the false-negative rate for genuine bytecode dumps would go up if we removed
        // the heuristic outright.
        evaluateTwoIdenticalCalls("_DEFAULT_CONFIG").shouldBeEmpty()
    }

    private fun evaluateTwoIdenticalCalls(callName: String): List<Finding> {
        val loc = SourceLocation("Fixture.java", 1, 1)
        val arg = GenericNode(nodeType = "id", location = loc, children = emptyList())
        val call1 = FunctionCall(callName, "service", listOf(arg), loc, emptyList())
        val call2 = FunctionCall(callName, "service", listOf(arg), loc, emptyList())
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
