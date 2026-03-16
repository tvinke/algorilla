package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.Language
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SemanticsYamlParserTest {
    @Nested
    inner class SplitSections {
        @Test
        fun `splits top-level keys into sections`() {
            val yaml =
                "methods:\n" +
                    "  get: { semantics: lookup, kind: FIND }\n" +
                    "  put: { semantics: lookup, kind: CONTAINS }\n" +
                    "heavyweight-types:\n" +
                    "  - ResultSet\n" +
                    "  - Connection\n"

            val sections = splitSections(yaml)

            sections.keys shouldBe setOf("methods", "heavyweight-types")
            sections["methods"]!!.size shouldBe 2
            sections["heavyweight-types"]!!.size shouldBe 2
        }

        @Test
        fun `ignores comments and blank lines`() {
            val yaml =
                "# This is a comment\n" +
                    "methods:\n" +
                    "  get: { semantics: lookup }\n" +
                    "\n" +
                    "  # Another comment\n" +
                    "  put: { semantics: lookup }\n"

            val sections = splitSections(yaml)

            sections["methods"]!!.size shouldBe 2
        }
    }

    @Nested
    inner class CollectListItemsParsing {
        @Test
        fun `collects dash-prefixed items`() {
            val lines = listOf("  - HashMap", "  - TreeMap", "  - LinkedHashMap")
            val items = collectListItems(lines)

            items shouldBe setOf("HashMap", "TreeMap", "LinkedHashMap")
        }

        @Test
        fun `strips quotes from items`() {
            val lines = listOf("  - \"contains\"", "  - 'indexOf'")
            val items = collectListItems(lines)

            items shouldBe setOf("contains", "indexOf")
        }

        @Test
        fun `returns empty set for null input`() {
            collectListItems(null) shouldBe emptySet()
        }
    }

    @Nested
    inner class ParseYamlIntegration {
        @Test
        fun `parses methods with semantics`() {
            val yaml =
                "methods:\n" +
                    "  get: { semantics: lookup, kind: FIND }\n" +
                    "  sort: { semantics: sort }\n" +
                    "  indexOf: { semantics: lookup, kind: CONTAINS }\n"

            val parsed = parseYaml(yaml)

            parsed.methods.size shouldBe 3
            parsed.methods["get"].shouldNotBeNull()
            parsed.methods["get"]!!.category shouldBe SemanticCategory.LOOKUP
        }

        @Test
        fun `parses known sections`() {
            val yaml =
                "heavyweight-types:\n" +
                    "  - ResultSet\n" +
                    "collection-types:\n" +
                    "  - List\n" +
                    "  - ArrayList\n" +
                    "o1-types:\n" +
                    "  - HashSet\n" +
                    "stream-ops:\n" +
                    "  - filter\n" +
                    "  - map\n"

            val parsed = parseYaml(yaml)

            parsed.heavyweightTypes shouldBe setOf("ResultSet")
            parsed.collectionTypes shouldBe setOf("List", "ArrayList")
            parsed.o1Types shouldBe setOf("HashSet")
            parsed.streamOps shouldBe setOf("filter", "map")
        }

        @Test
        fun `collects unknown sections as extras`() {
            val yaml =
                "hidden-loop-skip-methods:\n" +
                    "  - isEmpty\n" +
                    "  - isBlank\n" +
                    "custom-section:\n" +
                    "  - foo\n" +
                    "  - bar\n"

            val parsed = parseYaml(yaml)

            parsed.extras shouldContainKey "hidden-loop-skip-methods"
            parsed.extras["hidden-loop-skip-methods"]!! shouldContain "isEmpty"
            parsed.extras shouldContainKey "custom-section"
            parsed.extras["custom-section"]!! shouldBe setOf("foo", "bar")
        }
    }

    @Nested
    inner class ExtractLanguage {
        @Test
        fun `extracts language from yaml header`() {
            val yaml = "language: java\nmethods:\n  get: { semantics: lookup }\n"

            extractLanguageFromYaml(yaml) shouldBe Language.JAVA
        }

        @Test
        fun `returns null when no language key`() {
            val yaml = "methods:\n  get: { semantics: lookup }\n"

            extractLanguageFromYaml(yaml).shouldBeNull()
        }

        @Test
        fun `returns null for unknown language`() {
            extractLanguageFromYaml("language: cobol").shouldBeNull()
        }
    }
}
