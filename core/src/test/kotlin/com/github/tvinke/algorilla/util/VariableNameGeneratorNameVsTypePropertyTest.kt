package com.github.tvinke.algorilla.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73).
 *
 * extractSimpleName's own "get" prefix check already has the word-boundary guard its
 * counterparts elsewhere in this campaign were missing (`withoutParens[3].isUpperCase()`)
 * — confirmed here rather than just asserted, since campaign hygiene is "every flagged
 * function gets a property test", bug or not.
 *
 * stripCollectionSuffix's suffix list (List/Collection/Array/Set/Items/Elements) is
 * case-sensitive `endsWith`, so a name that happens to end in one of those exact
 * capitalized words as part of a single compound concept (`dataSet`, `headSet`) gets
 * split as if the trailing word were an added collection-suffix, not part of the name.
 * Unlike the other missing-word-boundary bugs this campaign fixed, this one has no
 * detection-correctness impact — VariableNameGenerator only feeds suggestion text (see
 * Suggestion.kt), never a finding's fire/no-fire decision — so it's pinned as a cosmetic
 * quirk, not fixed.
 */
internal class VariableNameGeneratorNameVsTypePropertyTest {
    private val genuineGetters =
        listOf(
            "getImages()" to "images",
            "getDescriptions()" to "descriptions",
            "getProductOption()" to "productOption",
        )

    @Test
    fun `a real camelCase getter still strips the get prefix`() {
        genuineGetters.forEach { (expression, expected) ->
            VariableNameGenerator.extractSimpleName(expression) shouldBe expected
        }
    }

    private val nonGetterWordsStartingWithGet =
        listOf(
            "getaway()" to "getaway",
            "gettext()" to "gettext",
        )

    @Test
    fun `a domain method that merely starts with 'get' keeps its full name`() {
        nonGetterWordsStartingWithGet.forEach { (expression, expected) ->
            VariableNameGenerator.extractSimpleName(expression) shouldBe expected
        }
    }

    @Test
    fun `a compound name ending in a collection-suffix word is split as if the suffix were separate (documented cosmetic quirk)`() {
        // "dataSet" is one concept, not "data" + an added Set suffix, but
        // stripCollectionSuffix can't tell the difference from "orderSet".
        VariableNameGenerator.suggestMapName("dataSet", "type") shouldBe "dataByType"
    }

    @Test
    fun `a genuine collection-plus-suffix name still strips correctly`() {
        VariableNameGenerator.suggestMapName("orderList", "id") shouldBe "orderById"
    }
}
