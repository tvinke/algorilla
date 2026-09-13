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

    // -- matchesCamelCasePrefix (used by isMonadicTarget's variable-name path) --
    // Already correctly boundary-guarded, unlike the bugs found elsewhere this campaign -
    // confirmed here rather than assumed, per the "every flagged function gets a property
    // test" rule. Also surfaces a real, but data-level (not code-level) risk: "result" as a
    // monadic-name prefix genuinely collides with "resultSet"/"resultList" — both start
    // with "result" at a real camelCase boundary, so a ResultSet-style collection variable
    // reads as monadic too. That's a YAML calibration question for the fitness/veto loop,
    // not a code defect - the boundary check itself is doing exactly what it should.

    @Test
    fun `a variable name at a real camelCase boundary after a monadic prefix is still monadic`() {
        registry.isMonadicTarget("resultValue") shouldBe true
        registry.isMonadicTarget("futureResponse") shouldBe true
    }

    @Test
    fun `a variable name that merely starts with a monadic prefix without a boundary is not monadic`() {
        // "resultado" (Spanish for result) starts with nothing meaningful in English, but
        // "resultative"/"resultant" would collide via bare startsWith - the boundary check
        // correctly rejects those; "results" itself is plural, not a boundary-following word.
        registry.isMonadicTarget("resultant") shouldBe false
    }

    @Test
    fun `resultSet still reads as monadic under the current YAML - a calibration note, not a code bug`() {
        // Documents the collision rather than hiding it: "result" + capital "S" is a real
        // camelCase boundary by the code's own (correct) rule, so this returns true even
        // though a ResultSet-style variable is a genuine collection, not a monadic value.
        registry.isMonadicTarget("resultSet") shouldBe true
    }
}
