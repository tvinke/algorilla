package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.Language
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ExtractVariableNameTest {
    @Nested
    inner class SimpleVariables {
        @Test
        fun `should return simple variable name`() {
            extractVariableName("items") shouldBe "items"
        }

        @Test
        fun `should return null for null input`() {
            extractVariableName(null).shouldBeNull()
        }
    }

    @Nested
    inner class FieldAccess {
        @Test
        fun `should preserve single field access`() {
            extractVariableName("resource.currentLocationIds") shouldBe "resource.currentLocationIds"
        }

        @Test
        fun `should preserve nested field access`() {
            extractVariableName("this.config.items") shouldBe "this.config.items"
        }
    }

    @Nested
    inner class StreamStripping {
        @Test
        fun `should strip stream() entry point`() {
            extractVariableName("items.stream()") shouldBe "items"
        }

        @Test
        fun `should strip parallelStream() entry point`() {
            extractVariableName("items.parallelStream()") shouldBe "items"
        }

        @Test
        fun `should strip stream() from field access chain`() {
            extractVariableName("resource.currentLocationIds.stream()") shouldBe "resource.currentLocationIds"
        }
    }

    @Nested
    inner class StreamChainOperations {
        @Test
        fun `should strip map() from stream chain`() {
            extractVariableName("items.stream().map(x->x.id)") shouldBe "items"
        }

        @Test
        fun `should strip map() with nested parens from stream chain`() {
            extractVariableName("items.stream().map(x->foo(x))") shouldBe "items"
        }

        @Test
        fun `should strip chained map and flatMap from stream`() {
            extractVariableName("items.stream().map(x->x.name).flatMap(x->x.chars())") shouldBe "items"
        }

        @Test
        fun `should strip map from field access stream chain`() {
            extractVariableName("resource.currentAtLocations.stream().map(location->location.locationId)") shouldBe
                "resource.currentAtLocations"
        }
    }

    @Nested
    inner class SortOperationStripping {
        @Test
        fun `should strip sorted() with comparator`() {
            extractVariableName("locationResources.sorted(locationResourceComparator)") shouldBe "locationResources"
        }

        @Test
        fun `should strip sort() from chain`() {
            extractVariableName("items.stream().sort(comparator)") shouldBe "items"
        }

        @Test
        fun `should strip sortedBy from Kotlin chain`() {
            extractVariableName("items.sortedBy(it.name)", Language.KOTLIN) shouldBe "items"
        }

        @Test
        fun `should strip filter from chain`() {
            extractVariableName("items.stream().filter(x->x.active)") shouldBe "items"
        }
    }

    @Nested
    inner class LanguageAwareStripping {
        @Test
        fun `should preserve values() for Java - enum iteration`() {
            extractVariableName("Status.values()", Language.JAVA) shouldBe "Status.values()"
        }

        @Test
        fun `should preserve values() for Kotlin`() {
            extractVariableName("Status.values()", Language.KOTLIN) shouldBe "Status.values()"
        }

        @Test
        fun `should preserve values() for Groovy`() {
            extractVariableName("Status.values()", Language.GROOVY) shouldBe "Status.values()"
        }

        @Test
        fun `should strip values() for JavaScript - stream op`() {
            extractVariableName("arr.values()", Language.JAVASCRIPT) shouldBe "arr"
        }
    }

    @Nested
    inner class StaticWrappers {
        @Test
        fun `should unwrap Arrays_stream`() {
            extractVariableName("Arrays.stream(subscriptionResources)") shouldBe "subscriptionResources"
        }

        @Test
        fun `should unwrap Collections_unmodifiableList`() {
            extractVariableName("Collections.unmodifiableList(items)") shouldBe "items"
        }

        @Test
        fun `should unwrap Optional_of`() {
            extractVariableName("Optional.of(value)") shouldBe "value"
        }
    }
}
