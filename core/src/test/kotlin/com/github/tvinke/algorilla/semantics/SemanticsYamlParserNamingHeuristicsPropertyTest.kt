package com.github.tvinke.algorilla.semantics

import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

/**
 * Canary properties for the testharnas campaign (cc #73). SemanticsYamlParser has no
 * resolved-type concept to get wrong — it's a raw line-based YAML tokenizer, so the
 * shape here is different from TypeEnvironment/RegexRecompilationInLoopRule: the risk is
 * "does the parser actually extract what a line means" rather than "name vs resolved
 * type". One real bug of that kind turned up: `parseListItem` never strips a trailing
 * inline comment (`- set       # Map.set(key, value)`), a style genuinely used in
 * java.yml (`Stream  # Single-value stream from Optional.stream()`), kotlin.yml (`Flow`,
 * `Sequence`) and javascript.yml (`set`). Those four real entries were silently being
 * registered as `"Stream  # Single-value stream from Optional.stream()"` etc. instead of
 * the clean name — meaning the actual `monadic-types`/`keyed-aggregation-methods` YAML
 * declarations for Java's Stream, Kotlin's Flow/Sequence, and JS's Map.set() never took
 * effect. Same failure family as the naming-heuristic bugs elsewhere in this campaign:
 * text that *looks* like the right shape but doesn't survive being turned into the value
 * a caller actually reads back.
 */
internal class SemanticsYamlParserNamingHeuristicsPropertyTest {
    // -- parseListItem / collectListItems: inline trailing comments must not leak into the value --

    private val realWorldInlineCommentLines =
        listOf(
            "  - set       # Map.set(key, value)" to "set",
            "  - Stream  # Single-value stream from Optional.stream()" to "Stream",
            "  - Flow  # Can be single-value" to "Flow",
            "  - Sequence  # Lazy, not a collection" to "Sequence",
        )

    @Test
    fun `a trailing inline comment does not leak into the parsed list value`() {
        realWorldInlineCommentLines.forEach { (line, expected) ->
            collectListItems(listOf(line)) shouldBe setOf(expected)
        }
    }

    @Test
    fun `an inline comment does not survive quote-stripping either`() {
        collectListItems(listOf("  - \"contains\"   # note")) shouldBe setOf("contains")
    }

    @Test
    fun `a hash with no preceding whitespace is treated as part of the value, not a comment`() {
        // Nothing in the actual YAML files does this, but a bare "#" glued onto a real
        // token (rather than a real "word #comment" split) should not be silently eaten.
        collectListItems(listOf("  - weird#value")) shouldBe setOf("weird#value")
    }

    // -- quote-stripping round trip (unaffected by the comment fix) --

    private val plainWords = listOf("contains", "indexOf", "getSupportedTypes", "a")

    @Test
    fun `double and single quoted items round-trip to their unquoted content`() {
        plainWords.forEach { word ->
            collectListItems(listOf("  - \"$word\"")) shouldBe setOf(word)
            collectListItems(listOf("  - '$word'")) shouldBe setOf(word)
        }
    }

    // -- splitSections: indentation is what decides a section header, not just a trailing colon --

    private val indentedColonLines = listOf("  nested:", "\tnested:", "    deeper:")

    @Test
    fun `an indented line ending in a colon is content, never a new section header`() {
        indentedColonLines.forEach { line ->
            val yaml = "methods:\n$line\n  get: { semantics: lookup }\n"
            val sections = splitSections(yaml)
            // Still exactly one section ("methods") - the indented "...:" line joined
            // its contents instead of starting a section of its own.
            sections.keys shouldBe setOf("methods")
            sections["methods"]!!.contains(line.trimEnd()) shouldBe true
        }
    }

    // -- collectMapItems: list markers and comments inside a map section must not become entries --

    @Test
    fun `list markers and comment lines are skipped, only key colon value lines are collected`() {
        val lines = listOf("  # a comment", "  - notAMapEntry", "  findById: findAllById", "  ", "  save: saveAll")
        val map = collectMapItems(lines)

        map shouldBe mapOf("findById" to "findAllById", "save" to "saveAll")
    }

    // -- parseMethodLine (via parseYaml): comment/list-marker noise inside methods: must not corrupt it --

    @Test
    fun `noise lines inside a methods section do not produce spurious entries`() {
        val yaml =
            "methods:\n" +
                "  # a comment about get\n" +
                "  get: { semantics: lookup, kind: FIND }\n" +
                "  - not a method line\n" +
                "  put: { semantics: lookup, kind: CONTAINS }\n"

        val parsed = parseYaml(yaml)

        parsed.methods.keys shouldBe setOf("get", "put")
        parsed.methods shouldContainKey "get"
    }
}
