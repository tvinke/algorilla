package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.AnalysisContext
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

/**
 * Regression coverage for #138: [IOInLoopRule.checkCrossMethodIO] has `language` in scope (it's
 * a parameter of the very function) but, before the fix, never forwarded it to
 * [com.github.tvinke.algorilla.util.ParameterFlowQuery.parameterFlowsThrough] - which then
 * defaulted to [Language.JAVA] internally for every hop of
 * [com.github.tvinke.algorilla.util.CrossMethodResolver.resolve] it drives, regardless of the
 * file's actual language.
 *
 * `sortedBy` is a Kotlin stdlib collection op - part of Kotlin's `unresolvableNames` skiplist so
 * it's never mistaken for a user-defined function - but it isn't in Java's YAML at all, so under
 * the (bugged) hardcoded-JAVA resolution it looked like an ordinary, resolvable user method. A
 * coincidentally-named local `sortedBy()` registered in the symbol table would then get treated
 * as the resolution target, and its (fabricated) internal `executeQuery()` call would surface as
 * a hidden-IO-in-loop finding for a plain Kotlin stdlib call - a false positive that only exists
 * because the real language was never threaded through.
 */
internal class IOInLoopRuleLanguageTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = IOInLoopRule()

    @Test
    fun `does not cross-method-resolve Kotlin's sortedBy() as a user function when checking for hidden IO`() {
        val outerLoop = LoopNode(kind = LoopKind.FOR, iteratedVariable = "items", location = loc, children = listOf(call("sortedBy")))
        val caller =
            functionDecl("caller", outerLoop).also {
                it.parameterFlows = listOf(ParameterFlow(0, "items", setOf(FlowTarget.FunctionArgument("sortedBy", loc))))
            }
        // A coincidentally-named local "sortedBy" that (if ever resolved into) would report a
        // hidden executeQuery() - this must never be reached for a Kotlin source, since
        // "sortedBy" is Kotlin's own stdlib call, not a call to this declaration.
        val localSortedByFn =
            functionDecl("sortedBy").also {
                it.parameterFlows = listOf(ParameterFlow(0, "data", setOf(FlowTarget.MethodCallReceiver("executeQuery", loc))))
            }
        val symbolTable =
            SymbolTable().also {
                it.register(caller)
                it.register(localSortedByFn)
            }

        val findings = rule.evaluate(context(caller, symbolTable, Language.KOTLIN))

        // Before the fix: checkCrossMethodIO ignored its own `language` parameter when calling
        // parameterFlowsThrough, which then resolved "sortedBy" against Java's (non-matching)
        // unresolvable-names skiplist instead of Kotlin's - wrongly following it into
        // "localSortedByFn" and reporting a hidden IO-in-loop finding for ordinary Kotlin code.
        findings.shouldBeEmpty()
    }

    private fun context(
        fn: FunctionDecl,
        symbolTable: SymbolTable,
        language: Language,
    ): AnalysisContext {
        val fileRoot = FileRoot(filePath = "Fixture.kt", language = language, location = loc, children = listOf(fn))
        return AnalysisContext(
            irTrees = mapOf("Fixture.kt" to fileRoot),
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
