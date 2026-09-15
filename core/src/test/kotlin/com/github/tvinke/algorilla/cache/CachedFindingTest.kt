package com.github.tvinke.algorilla.cache

import com.github.tvinke.algorilla.model.CardinalityBucket
import com.github.tvinke.algorilla.model.PathContext
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Suggestion
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CachedFindingTest {
    private fun finding(
        ruleId: String = "io-in-loop",
        line: Int = 42,
    ) = Finding(
        ruleId = ruleId,
        ruleName = "IO In Loop",
        severity = Severity.WARNING,
        location = SourceLocation("/src/OrderService.java", line, 1),
        message = "IO call inside loop",
        suggestions = listOf(Suggestion.Freeform("Batch it")),
    )

    @Test
    fun `enclosingMethod is preserved through cache round-trip`() {
        val original = finding()
        val cached = CachedFinding.fromFinding(original, enclosingMethod = "OrderService.process")

        cached.enclosingMethod shouldBe "OrderService.process"
    }

    @Test
    fun `enclosingMethod defaults to null for legacy cache entries`() {
        val cached =
            CachedFinding(
                ruleId = "io-in-loop",
                ruleName = "IO In Loop",
                severity = "WARNING",
                file = "/src/OrderService.java",
                line = 42,
                column = 1,
                message = "IO call inside loop",
                suggestion = "Batch it",
            )

        cached.enclosingMethod shouldBe null
    }

    @Test
    fun `pathContext and cardinalityBucket survive a cache round-trip`() {
        val original =
            finding().copy(
                pathContext = PathContext.REQUEST,
                cardinalityBucket = CardinalityBucket.LIKELY_LARGE,
            )

        val roundTripped = CachedFinding.fromFinding(original).toFinding()

        roundTripped.pathContext shouldBe PathContext.REQUEST
        roundTripped.cardinalityBucket shouldBe CardinalityBucket.LIKELY_LARGE
    }

    @Test
    fun `pathContext and cardinalityBucket default to null for legacy cache entries`() {
        val cached =
            CachedFinding(
                ruleId = "io-in-loop",
                ruleName = "IO In Loop",
                severity = "WARNING",
                file = "/src/OrderService.java",
                line = 42,
                column = 1,
                message = "IO call inside loop",
                suggestion = "Batch it",
            )

        val restored = cached.toFinding()

        restored.pathContext shouldBe null
        restored.cardinalityBucket shouldBe null
    }
}
