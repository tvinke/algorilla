package com.github.tvinke.algorilla.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Guards the KDoc contract for eight `util` functions that cross-method rules lean on for
 * correctness (the [com.github.tvinke.algorilla.util.CrossMethodResolver],
 * [com.github.tvinke.algorilla.util.RecursionDetector] and
 * [com.github.tvinke.algorilla.util.ParameterFlowQuery] resolution/flow helpers, plus
 * `IRNodeExtensions.kt`'s `hasO1Type`/`isCollectionLookup`/`isFollowedByExit`): each must
 * document at least one line starting with `Guarantee:`, `Precondition:` or `Edge case:` -
 * a concrete behavior contract a caller can rely on, not just a description of what the
 * function computes.
 *
 * Scoped to exactly these eight functions by name, not to the whole `util` package - other
 * functions there (`findDescendants`, `declaredTypeOf`, `withChildren`, the word-boundary
 * helpers, ...) are a separate, later batch and intentionally untouched here.
 *
 * Why a fixed name list instead of a structural "any function called from 2+ files" check:
 * Konsist has no call/reference-counting API (no equivalent of "find callers of X" - its
 * [com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration] exposes the declaration
 * itself, not who invokes it). A structural variant is buildable by deriving a text-search
 * marker per function name and counting matches across
 * [com.lemonappdev.konsist.api.declaration.KoFileDeclaration.hasTextMatching], but applying
 * it project-wide flips the staleness risk rather than removing it: at a ">= 2 external
 * files" shared-ness threshold, `hasO1Type` (exactly one caller today, `QuadraticRemovalRule`)
 * silently drops out; loosened to ">= 1", it sweeps in most of the rest of `util`, which this
 * batch explicitly excludes. So this list was curated by the architecture review that decided
 * this batch, not derived from a live call count.
 */
internal class UtilKDocContractTest {
    // KoTextProvider.hasTextMatching does a full-string match (Kotlin's String.matches),
    // not a search - so the pattern has to account for everything around the label too,
    // not just the label itself.
    private val contractLabel = Regex(""".*(Guarantee|Precondition|Edge case):.*""", RegexOption.DOT_MATCHES_ALL)

    private val targetFunctions
        get() = TargetFunctions.value

    @Test
    fun `all seven target names are still found, resolving to eight declarations`() {
        // isCollectionLookup has two overloads, so seven names resolve to eight
        // declarations - a guard against the name list silently going stale after a rename.
        targetFunctions.assertNotEmpty()
        val missing = SharedCrossMethodFunctions.names - targetFunctions.map { it.name }.toSet()
        missing.shouldBeEmpty()
        targetFunctions.size shouldBe EXPECTED_DECLARATION_COUNT
    }

    @Test
    fun `each of the eight shared cross-method util functions documents a Guarantee, Precondition or Edge case`() {
        targetFunctions.assertTrue { fn ->
            fn.kDoc?.hasTextMatching(contractLabel) == true
        }
    }

    // JUnit5 creates a fresh UtilKDocContractTest instance per @Test method, so an instance-level
    // `by lazy` would still re-scan the project once per method. This companion-level lazy scans
    // once for the whole class - Konsist's scope is a read-only view of already-compiled classes,
    // safe to share across test methods.
    private object TargetFunctions {
        val value by lazy {
            Konsist
                // Production only - the test source set mirrors this same package
                // (CrossMethodResolverTest.kt et al. are also `package ...util`), and scoping
                // to production keeps a same-named test helper from ever being picked up here.
                .scopeFromProduction("core")
                .functions()
                .withPackage("com.github.tvinke.algorilla.util")
                .withName(SharedCrossMethodFunctions.names)
        }
    }

    private companion object {
        const val EXPECTED_DECLARATION_COUNT = 8
    }
}
