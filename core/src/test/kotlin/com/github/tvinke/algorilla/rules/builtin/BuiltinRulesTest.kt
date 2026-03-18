package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class BuiltinRulesTest {
    @Nested
    inner class All {
        @Test
        fun `returns at least 20 rules`() {
            BuiltinRules.all() shouldHaveAtLeastSize 20
        }

        @Test
        fun `all rule IDs are unique`() {
            val rules = BuiltinRules.all()
            val ids = rules.map { it.id }.toSet()
            ids shouldHaveSize rules.size
        }

        @Test
        fun `each rule has a non-blank id and name`() {
            BuiltinRules.all().forEach { rule ->
                (rule.id.isNotBlank()) shouldBe true
                (rule.name.isNotBlank()) shouldBe true
            }
        }

        @Test
        fun `returns new instances each call`() {
            val first = BuiltinRules.all()
            val second = BuiltinRules.all()
            first shouldHaveSize second.size
            (first[0] !== second[0]) shouldBe true
        }
    }

    @Nested
    inner class DefaultConfidence {
        @Test
        fun `redundant-expensive-call defaults to LOW`() {
            RedundantExpensiveCallRule().defaultConfidence shouldBe Confidence.LOW
        }

        @Test
        fun `expensive-construction defaults to LOW`() {
            HeavyweightObjectPerInvocationRule().defaultConfidence shouldBe Confidence.LOW
        }

        @Test
        fun `repeated-collection-iteration defaults to LOW`() {
            RepeatedCollectionIterationRule().defaultConfidence shouldBe Confidence.LOW
        }

        @Test
        fun `repeated-linear-scan defaults to LOW`() {
            RepeatedLinearScanRule().defaultConfidence shouldBe Confidence.LOW
        }
    }
}
