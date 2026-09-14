package com.github.tvinke.algorilla.semantics

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign, from the cross-file grep this
 * campaign did after fixing RedundantExpensiveCallRule's isSequentialReadPrefix (missing
 * word boundary on a prefix list): MethodPurity.classify's two `lower.startsWith(it)`
 * checks have the identical shape. "settings()" starts with side-effect-prefix "set" with
 * no real word boundary, so a pure getter for a Settings object reads as SIDE_EFFECT.
 *
 * Documented finding, NOT fixed here: adding the usual word-boundary check
 * (startsWithAtWordBoundary) regresses a real, common case — "println"/"printf" start
 * with side-effect-prefix "print" followed by a lowercase continuation ("ln"/"f"), with
 * no camelCase transition, yet both are genuinely side-effecting methods from the JDK
 * itself (System.out.println/printf), not a different word that happens to share a
 * prefix. Unlike "settings"/"vegetable"/"screenwriter" elsewhere in this campaign, this
 * prefix list mixes "real compound words" (set+tings, a different concept) with
 * "informal suffix on the same word" (print+ln, still printing) - a mechanical boundary
 * rule can't tell those apart, and blindly applying it broke the existing
 * `println` SIDE_EFFECT test. Fixing "settings" properly would need a per-prefix
 * allow/deny list for which prefixes tolerate a lowercase continuation - a new
 * calibration decision, not a restoration of an existing invariant.
 */
internal class MethodPurityNameVsTypeTest {
    @Test
    fun `a method name that merely starts with a side-effect prefix without a boundary is misread as side-effectful (documented gap)`() {
        MethodPurity.classify("settings") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("initials") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `a method name that merely starts with a pure prefix without a boundary is misread as pure (documented gap)`() {
        // "island" starts with pure-prefix "is" with no real boundary.
        MethodPurity.classify("island") shouldBe Purity.PURE
    }

    @Test
    fun `println and printf must keep matching the side-effect prefix despite no camelCase boundary`() {
        // The reason the boundary fix isn't applied here: these would break if it were.
        MethodPurity.classify("println") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("printf") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `real camelCase setters and predicates classify correctly regardless`() {
        MethodPurity.classify("setName") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("isValid") shouldBe Purity.PURE
    }

    // -- classify(methodName, qualifiedTarget): a separate code path from the prefix checks
    // above. "log" is a real side-effect-targets entry short enough that "catalog"/"dialog"/
    // "analog" all satisfy a bare contains with no boundary check - unlike the println/printf
    // case, there's no legitimate word where "log" is an informal continuation of itself, so
    // the standard boundary fix applies cleanly here.

    @Test
    fun `a call on a genuine logger target is still side-effectful`() {
        MethodPurity.classify("info", "logger") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("debug", "log") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `a call on a target merely containing log without a boundary is not misread as a logger call`() {
        // "load" is neither a side-effect- nor pure-prefix, so this isolates the
        // qualifiedTarget check from classify(methodName)'s own prefix checks.
        MethodPurity.classify("load", "catalog") shouldBe Purity.UNKNOWN
        MethodPurity.classify("load", "dialog") shouldBe Purity.UNKNOWN
    }
}
