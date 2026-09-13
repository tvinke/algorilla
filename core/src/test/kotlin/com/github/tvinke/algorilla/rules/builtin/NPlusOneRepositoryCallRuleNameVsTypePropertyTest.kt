package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.Confidence
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
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). isSingleRecordFetch and
 * matchesRepoPattern both classify a method/target name by prefix or suffix list, and both
 * had the same missing-word-boundary shape found elsewhere in this campaign:
 *
 * - the batch-method-suffixes check (`endsWith("In")`) matched "findByOrigin" too — it
 *   genuinely ends in "in", nothing to do with a Spring Data `...In` batch query — so a
 *   real single-record fetch was silently excluded as if it were a batch method.
 * - the batch-method-prefixes check (`startsWith("findAll")`) matched "findAllocationById"
 *   too, even though "By...Id" makes this obviously a single-record fetch, not a bulk
 *   findAll.
 * - matchesRepoPattern (confidence only, not detection) reused the exact copy-pasted
 *   suffix-matching block from IOInLoopRule and had the same "screenwriter"/"writer"
 *   collision.
 *
 * All three are fixed with the same startsWithAtWordBoundary/endsWithAtWordBoundary
 * helpers introduced for IOInLoopRule and RegexRecompilationInLoopRule.
 */
internal class NPlusOneRepositoryCallRuleNameVsTypePropertyTest {
    private val loc = SourceLocation("Fixture.java", 1, 1)
    private val rule = NPlusOneRepositoryCallRule()
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    @Test
    fun `findByOrigin is not misread as a findByXIn batch method just because it ends in 'in'`() {
        findingsFor("findByOrigin", "userRepository") shouldHaveSize 1
    }

    @Test
    fun `a genuine findByStatusIn batch method is still excluded`() {
        findingsFor("findByStatusIn", "userRepository").shouldBeEmpty()
    }

    @Test
    fun `findAllocationById is not misread as a bulk findAll method just because of the prefix`() {
        findingsFor("findAllocationById", "userRepository") shouldHaveSize 1
    }

    @Test
    fun `a genuine findAllUsers bulk method is still excluded`() {
        findingsFor("findAllUsers", "userRepository").shouldBeEmpty()
    }

    @Test
    fun `matchesRepoPattern confidence is not inflated by a suffix buried in an unrelated word`() {
        // "repoScreenwriter" satisfies the repository-patterns gate ("repo") so the finding
        // fires, but its confidence must not be promoted to HIGH just because it also ends
        // in "writer" with no real word boundary before the match.
        val findings = findingsFor("findById", "repoScreenwriter")
        findings shouldHaveSize 1
        findings.first().confidence shouldBe Confidence.MEDIUM
    }

    @Test
    fun `matchesRepoPattern confidence is still promoted for a genuine repository target`() {
        val findings = findingsFor("findById", "userRepository")
        findings shouldHaveSize 1
        findings.first().confidence shouldBe Confidence.HIGH
    }

    private fun findingsFor(
        methodName: String,
        target: String,
    ): List<Finding> {
        val call = FunctionCall(methodName, target, listOf(GenericNode("x", loc, emptyList())), loc, emptyList())
        val loop = LoopNode(kind = LoopKind.FOR_EACH, iteratedVariable = "items", location = loc, children = listOf(call))
        val fileRoot = FileRoot(filePath = "Fixture.java", language = Language.JAVA, location = loc, children = listOf(loop))
        val context =
            AnalysisContext(
                irTrees = mapOf("Fixture.java" to fileRoot),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
                registry = registry,
            )
        return rule.evaluate(context)
    }
}
