package com.github.tvinke.algorilla.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VariableNameGeneratorTest {
    @Nested
    inner class ExtractSimpleName {
        @Test
        fun `simple variable passes through`() {
            VariableNameGenerator.extractSimpleName("isoCodes") shouldBe "isoCodes"
        }

        @Test
        fun `getter method strips get prefix and lowercases`() {
            VariableNameGenerator.extractSimpleName("getImages()") shouldBe "images"
        }

        @Test
        fun `dotted chain uses last segment`() {
            VariableNameGenerator.extractSimpleName("product.getImages()") shouldBe "images"
        }

        @Test
        fun `deep chain uses last segment`() {
            VariableNameGenerator.extractSimpleName(
                "attribute.getProductOption().getDescriptions()",
            ) shouldBe "descriptions"
        }

        @Test
        fun `non-getter method keeps name`() {
            VariableNameGenerator.extractSimpleName("items.stream()") shouldBe "stream"
        }

        @Test
        fun `short get prefix not stripped`() {
            VariableNameGenerator.extractSimpleName("get()") shouldBe "get"
        }
    }

    @Nested
    inner class SuggestSetName {
        @Test
        fun `simple variable`() {
            VariableNameGenerator.suggestSetName("isoCodes") shouldBe "isoCodesSet"
        }

        @Test
        fun `strips List suffix`() {
            VariableNameGenerator.suggestSetName("itemList") shouldBe "itemSet"
        }

        @Test
        fun `getter expression`() {
            VariableNameGenerator.suggestSetName("product.getImages()") shouldBe "imagesSet"
        }

        @Test
        fun `deep getter chain`() {
            VariableNameGenerator.suggestSetName(
                "attribute.getProductOption().getDescriptions()",
            ) shouldBe "descriptionsSet"
        }
    }

    @Nested
    inner class SuggestMapName {
        @Test
        fun `simple variable and key`() {
            VariableNameGenerator.suggestMapName("orders", "id") shouldBe "ordersById"
        }

        @Test
        fun `getter expression`() {
            VariableNameGenerator.suggestMapName("product.getImages()", "name") shouldBe "imagesByName"
        }
    }
}
