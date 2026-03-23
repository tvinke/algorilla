package com.github.tvinke.algorilla.semantics

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Validates the YAML semantics files and framework overlays against a known schema.
 * Catches typos like `io-method` vs `io-methods` that silently disable rules.
 */
internal class YamlSchemaValidationTest {
    /** All section names that the parser and rules recognize. */
    private val validSectionNames =
        setOf(
            // Parsed into dedicated fields
            "methods",
            "heavyweight-types",
            "collection-types",
            "o1-types",
            "stream-ops",
            "scope-ops",
            "trivial-methods",
            "builder-methods",
            "getter-prefixes",
            "cheap-methods",
            "sequential-read-methods",
            "reflection-methods",
            "copy-on-modify-methods",
            "regex-types",
            "regex-recompilation-methods",
            "mutation-methods",
            "removal-methods",
            "bulk-load-prefixes",
            "full-scan-methods",
            "io-methods",
            "o1-factory-methods",
            "memoization-methods",
            "tree-traversal-accessors",
            "visited-set-names",
            "pure-prefixes",
            "side-effect-prefixes",
            "side-effect-targets",
            "string-indicators",
            "string-name-suffixes",
            "string-exact-names",
            "monadic-types",
            "monadic-variable-names",
            "bulk-alternatives",
            // Framework overlay metadata
            "language",
            // Extras: consumed by rules via extraSection()
            "hidden-loop-skip-methods",
            "hidden-loop-skip-prefixes",
            "hidden-loop-skip-keywords",
            "constant-bound-keywords",
            "static-utility-classes",
            "non-list-targets-exact",
            "non-list-targets-suffixes",
            "non-list-targets-contains",
            "bulk-removal-methods",
            "non-growth-mutations",
            "small-collection-hints",
            "map-value-accessors",
            "single-fetch-prefixes",
            "batch-method-suffixes",
            "batch-method-prefixes",
            "repository-patterns",
            "non-repository-targets",
            "date-type-names",
            "date-parse-methods",
            "date-parse-targets",
            "dom-target-names",
            "non-deterministic-methods",
            "future-indicators",
            "collection-getter-names",
            "scalar-suffixes",
            "non-io-targets",
            "io-method-candidates",
            "io-target-patterns",
            "getter-excluded-names",
            "non-regex-matches-targets",
            "implicit-iteration-ops",
            "hof-methods",
            "filter-methods",
            "stream-entry-methods",
            "object-methods",
            "reflection-exclusions",
            "type-check-prefixes",
            "sequential-read-prefixes",
            "string-types",
            "list-types",
            "collection-view-accessors",
        )

    @ParameterizedTest(name = "language file {0} has no unknown sections")
    @ValueSource(strings = ["java.yml", "kotlin.yml", "groovy.yml", "javascript.yml"])
    fun `language YAML files should only contain known section names`(filename: String) {
        val text = loadResource("semantics/$filename")
        val sections = splitSections(text)
        val unknown = sections.keys - validSectionNames

        assert(unknown.isEmpty()) {
            "Unknown section(s) in $filename: $unknown\n" +
                "If these are intentional, add them to validSectionNames in this test."
        }
    }

    @ParameterizedTest(name = "language file {0} has no duplicate sections")
    @ValueSource(strings = ["java.yml", "kotlin.yml", "groovy.yml", "javascript.yml"])
    fun `language YAML files should not have duplicate section names`(filename: String) {
        val text = loadResource("semantics/$filename")
        val duplicates = findDuplicateSections(text)

        assert(duplicates.isEmpty()) {
            "Duplicate section(s) in $filename: $duplicates"
        }
    }

    @Test
    fun `framework overlay files should only contain known section names`() {
        val index = loadResource("semantics/frameworks-index.txt")
        val overlayFiles = index.lines().filter { it.isNotBlank() }
        val violations = mutableMapOf<String, Set<String>>()

        for (file in overlayFiles) {
            val text = loadResource("semantics/frameworks/$file")
            val sections = splitSections(text)
            val unknown = sections.keys - validSectionNames
            if (unknown.isNotEmpty()) {
                violations[file] = unknown
            }
        }

        assert(violations.isEmpty()) {
            violations.entries.joinToString("\n") { (file, unknown) ->
                "Unknown section(s) in $file: $unknown"
            }
        }
    }

    @Test
    fun `framework overlay files should not have duplicate sections`() {
        val index = loadResource("semantics/frameworks-index.txt")
        val overlayFiles = index.lines().filter { it.isNotBlank() }
        val violations = mutableMapOf<String, List<String>>()

        for (file in overlayFiles) {
            val text = loadResource("semantics/frameworks/$file")
            val duplicates = findDuplicateSections(text)
            if (duplicates.isNotEmpty()) {
                violations[file] = duplicates
            }
        }

        assert(violations.isEmpty()) {
            violations.entries.joinToString("\n") { (file, dups) ->
                "Duplicate section(s) in $file: $dups"
            }
        }
    }

    @Nested
    inner class LanguageFileCompleteness {
        @ParameterizedTest(name = "{0} has core sections populated")
        @ValueSource(strings = ["java.yml", "kotlin.yml", "groovy.yml", "javascript.yml"])
        fun `language files should have essential sections non-empty`(filename: String) {
            val text = loadResource("semantics/$filename")
            val sections = splitSections(text)
            val essentialSections =
                listOf(
                    "methods",
                    "collection-types",
                    "o1-types",
                    "io-methods",
                    "trivial-methods",
                    "mutation-methods",
                )
            val empty = essentialSections.filter { collectListItems(sections[it]).isEmpty() && sections[it]?.isEmpty() != false }

            assert(empty.isEmpty()) {
                "Essential section(s) in $filename are empty or missing: $empty"
            }
        }
    }

    @Test
    fun `all language files should load without errors`() {
        // This implicitly validates the full parse pipeline
        val registry = LanguageSemanticsRegistry.DEFAULT
        // Verify each language has at least some data loaded
        registry.ioMethods(com.github.tvinke.algorilla.model.Language.JAVA).shouldNotBeEmpty()
        registry.ioMethods(com.github.tvinke.algorilla.model.Language.KOTLIN).shouldNotBeEmpty()
        registry.ioMethods(com.github.tvinke.algorilla.model.Language.GROOVY).shouldNotBeEmpty()
        registry.ioMethods(com.github.tvinke.algorilla.model.Language.JAVASCRIPT).shouldNotBeEmpty()
    }

    @Test
    fun `no list item should be blank or whitespace-only`() {
        val files = listOf("java.yml", "kotlin.yml", "groovy.yml", "javascript.yml")
        val violations =
            files.flatMap { filename ->
                val sections = splitSections(loadResource("semantics/$filename"))
                sections.filterKeys { it != "methods" }.flatMap { (section, lines) ->
                    collectListItems(lines).filter { it.isBlank() }.map { "$filename/$section: blank item" }
                }
            }
        violations.shouldBeEmpty()
    }

    // ── Helpers ────────────────────────────────────────────────────

    private fun loadResource(path: String): String {
        val stream =
            checkNotNull(LanguageSemanticsRegistry::class.java.classLoader.getResourceAsStream(path)) {
                "Resource not found: $path"
            }
        return stream.bufferedReader().readText()
    }

    /** Finds section header lines that appear more than once. */
    private fun findDuplicateSections(text: String): List<String> {
        val sectionPattern = Regex("""^([a-z][a-z0-9-]*):""")
        val counts = mutableMapOf<String, Int>()
        for (line in text.lines()) {
            val match = sectionPattern.find(line)
            if (match != null) {
                val name = match.groupValues[1]
                counts[name] = (counts[name] ?: 0) + 1
            }
        }
        return counts.filter { it.value > 1 }.keys.toList()
    }
}
