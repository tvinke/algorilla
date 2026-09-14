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
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). matchesIOPattern's suffix-only
 * branch (`t.endsWith(pattern)`) is deliberately chosen over `contains` specifically to
 * avoid "session" matching "sessionState" — the doc comment says so. But `endsWith` alone
 * has no boundary check either, just on the other side: "writer" also matches inside
 * "screenwriter" or "underwriter" — real domain words that have nothing to do with a
 * stream Writer. The receiver is lowercased before the check, so a real camelCase boundary
 * ("screenWriter" vs "screenwriter") is indistinguishable by the time the comparison runs.
 * Same shape as the prefix-boundary bugs elsewhere in this campaign, on the suffix side,
 * and duplicated verbatim between matchesIOPattern and isReactiveChainTarget in this file.
 */
internal class IOInLoopRuleNameVsTypeTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = IOInLoopRule()
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    private val unrelatedSuffixCollisions =
        listOf(
            "screenwriter" to "write",
            "underwriter" to "write",
            "copywriter" to "write",
        )

    @Test
    fun `a domain word that merely ends with an IO suffix is not treated as an IO target`() {
        unrelatedSuffixCollisions.forEach { (target, methodName) ->
            findingsFor(target, methodName).shouldBeEmpty()
        }
    }

    @Test
    fun `a genuine IO-suffixed receiver is still flagged`() {
        findingsFor("reportWriter", "write") shouldHaveSize 1
        findingsFor("hibernateSession", "write") shouldHaveSize 1
    }

    @Test
    fun `an exact suffix match with nothing before it is still flagged`() {
        findingsFor("writer", "write") shouldHaveSize 1
    }

    /**
     * Documented finding (cc #73), not fixed here: isStreamCopyLoop matches the bare method
     * names "read"/"write" with no receiver-type confirmation, same category as
     * CardinalityExplosionRule.scanFlatMap's hardcoded "flatMap". A loop calling read()/write()
     * on two unrelated, non-stream receivers (e.g. an access-control check named
     * `permissions.read()`/`permissions.write()`) still gets suppressed as if it were the
     * stream-copy idiom. Fixing this needs a real receiver-type signal this rule doesn't
     * have wired in for this specific check, so it's pinned rather than changed.
     */
    @Test
    fun `read and write on an unrelated non-stream receiver still suppress the loop as a stream copy (documented gap)`() {
        val readCall = FunctionCall("read", "permissions", listOf(arg()), loc, emptyList())
        val writeCall = FunctionCall("write", "permissions", listOf(arg()), loc, emptyList())
        val ioCall = FunctionCall("save", "orderRepository", listOf(arg()), loc, emptyList())
        val loop =
            LoopNode(
                kind = LoopKind.FOR_EACH,
                iteratedVariable = "items",
                location = loc,
                children = listOf(readCall, writeCall, ioCall),
            )
        rule.evaluate(fixtureContext(listOf(loop))).shouldBeEmpty()
    }

    private fun arg() = GenericNode("x", loc, emptyList())

    private fun findingsFor(
        target: String,
        methodName: String,
    ): List<Finding> {
        val call = FunctionCall(methodName, target, listOf(arg()), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        return rule.evaluate(fixtureContext(listOf(loop)))
    }

    private fun fixtureContext(children: List<com.github.tvinke.algorilla.model.IRNode>): AnalysisContext {
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = children)
        return AnalysisContext(
            irTrees = mapOf("Fixture.java" to fileRoot),
            symbolTable = SymbolTable(),
            callGraph = CallGraph(),
            config = AnalysisConfig(),
            registry = registry,
        )
    }
}
