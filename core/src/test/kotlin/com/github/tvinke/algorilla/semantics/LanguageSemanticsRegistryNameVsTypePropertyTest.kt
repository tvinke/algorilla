package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.Language
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.of
import io.kotest.property.forAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

/**
 * Canary property for the testharnas campaign (cc #73), discovered as a side effect of
 * the SemanticsYamlParser inline-comment fix: cleaning up java.yml's `Stream  # ...`
 * monadic-types entry to the plain "Stream" unmasked a second, pre-existing bug in
 * [LanguageSemanticsRegistry.isMonadicTarget] — it matched a monadic type name as a raw
 * substring (`targetText.contains(it)`), so "Stream" matched inside "parallelStream()" and
 * "IntStream" too, not just a real standalone `Stream` reference. Every `.parallelStream()`
 * or `.stream()` forEach call was silently classified as monadic and skipped from loop
 * classification entirely the moment the YAML entry stopped being (accidentally) inert —
 * exactly the text-match-instead-of-a-real-boundary failure this campaign keeps finding,
 * just checked on the preceding side of the match instead of the following one.
 */
internal class LanguageSemanticsRegistryNameVsTypePropertyTest {
    private val registry = LanguageSemanticsRegistry.loadDefaults()

    private val streamSuffixCollisions =
        listOf(
            "items.parallelStream()",
            "items.stream()",
            "values.parallelStream().filter(x -> x > 0)",
        )

    @Test
    fun `a call ending in Stream as part of a longer identifier is not a monadic target`() {
        runBlocking {
            forAll(Arb.of(streamSuffixCollisions)) { targetText ->
                !registry.isMonadicTarget(Language.JAVA, targetText) && !registry.isMonadicTarget(targetText)
            }
        }
    }

    @Test
    fun `a real Stream type reference at a word boundary is still a monadic target`() {
        registry.isMonadicTarget(Language.JAVA, "Stream.of(1, 2, 3)") shouldBe true
        registry.isMonadicTarget("Stream.of(1, 2, 3)") shouldBe true
    }

    @Test
    fun `existing monadic-target behavior is unaffected by the boundary fix`() {
        registry.isMonadicTarget("Optional.of(x)") shouldBe true
        registry.isMonadicTarget("result.map") shouldBe true
        registry.isMonadicTarget("orders.stream()") shouldBe false
    }
}
