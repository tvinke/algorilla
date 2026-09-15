package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Direct unit test for [NestedLookupRule]'s cross-method confidence handling, built entirely
 * from IR nodes (no parser involved) - see [ChainedGettersRuleTest] for the established
 * pattern this follows.
 *
 * Regression coverage for a gap an architecture review found in the cross-method resolution
 * confidence work: `checkCrossMethod` used to re-resolve only the *outermost* call to read its
 * confidence, while [CrossMethodResolver.resolveAndFind] (now [CrossMethodResolver.resolveAndFindWithConfidence])
 * may have followed a chain of calls - up to `maxCallDepth` hops - to actually find the hidden
 * lookup. An ambiguous overload guess two hops deep was invisible to the outermost-only check,
 * so a finding could report MEDIUM/HIGH confidence even though the hidden lookup it named was
 * found through a guess, not a genuine resolution.
 */
internal class NestedLookupRuleTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = NestedLookupRule()

    @Test
    fun `demotes to LOW when the hidden lookup is found through an ambiguous inner hop, not the outermost call`() {
        val (outerFn, symbolTable) = ambiguousInnerHopFixture()

        val findings = rule.evaluate(context(outerFn, symbolTable))

        findings shouldHaveSize 1
        // Signal-based confidence alone would be MEDIUM here (the hidden lookup's target,
        // "items", matches the outer loop's iterated variable) - LOW only happens because the
        // inner-hop ambiguity was folded in. Before the fix, this asserted MEDIUM and passed
        // for the wrong reason: the ambiguity two hops down was invisible to the check.
        findings.single().confidence shouldBe Confidence.LOW
    }

    /**
     * for (item in items) { helper() } - "helper" is the ONE registered candidate (exact), but
     * helper()'s own body calls "inner()", which has TWO same-name, same-arity candidates
     * registered (ambiguous). Only the first-registered one (innerWithLookup) actually
     * contains a matching lookup - bestMatch's arbitrary pick has to land there for the rule
     * to find anything at all, which is itself the point: the pick is a guess, not a real
     * disambiguation, even though it happens to be "right" here.
     */
    private fun ambiguousInnerHopFixture(): Pair<FunctionDecl, SymbolTable> {
        val loop = LoopNode(kind = LoopKind.FOR, iteratedVariable = "items", location = loc, children = listOf(call("helper")))
        val outerFn = functionDecl("outer", loop)
        val helperFn = functionDecl("helper", call("inner"))

        val symbolTable =
            SymbolTable().also {
                it.register(helperFn)
                registerAmbiguousInnerCandidates(it)
            }
        return outerFn to symbolTable
    }

    /**
     * Registers two same-name, same-arity "inner" candidates - registration order matters:
     * bestMatch's arbitrary guess among them is [List.first] over however they were
     * registered, so `innerWithLookup` must go first for the fixture to find anything.
     */
    private fun registerAmbiguousInnerCandidates(symbolTable: SymbolTable) {
        val innerWithLookup =
            FunctionDecl(
                name = "inner",
                qualifiedName = "Fixture.inner",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children =
                    listOf(
                        LookupCall(kind = LookupKind.FIND, targetVariable = "items", isO1 = false, location = loc, children = emptyList()),
                    ),
            )
        val innerWithoutLookup =
            FunctionDecl(
                name = "inner",
                qualifiedName = "Fixture.inner2",
                parameters = emptyList(),
                declaringClass = "Fixture",
                location = loc,
                children = emptyList(),
            )
        symbolTable.register(innerWithLookup)
        symbolTable.register(innerWithoutLookup)
    }

    private fun context(
        fn: FunctionDecl,
        symbolTable: SymbolTable,
    ): AnalysisContext {
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(fn))
        return AnalysisContext(
            irTrees = mapOf("Fixture.java" to fileRoot),
            symbolTable = symbolTable,
            callGraph = CallGraph(),
            config = AnalysisConfig(),
        )
    }

    private fun functionDecl(
        name: String,
        vararg body: IRNode,
    ) = FunctionDecl(
        name = name,
        qualifiedName = "Fixture.$name",
        parameters = emptyList(),
        declaringClass = "Fixture",
        location = loc,
        children = body.toList(),
    )

    private fun call(name: String) =
        FunctionCall(name = name, qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())
}
