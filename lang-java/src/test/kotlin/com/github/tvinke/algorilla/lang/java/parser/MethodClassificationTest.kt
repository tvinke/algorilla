package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MethodClassificationTest {
    private val loc = SourceLocation("Test.java", 1, 1)

    @Nested
    inner class FindClassification {
        @Test
        fun `find with single arg should be classified as LookupCall`() {
            val result = classifyChainedCall("find", "items", "items", listOf(DUMMY_ARG), loc)
            result.shouldBeInstanceOf<LookupCall>()
            (result as LookupCall).kind shouldBe LookupKind.FIND
        }

        @Test
        fun `find with two args should be classified as FunctionCall (DB point lookup)`() {
            val result = classifyChainedCall("find", "couch", "couch", listOf(DUMMY_ARG, DUMMY_ARG), loc)
            result.shouldBeInstanceOf<FunctionCall>()
            (result as FunctionCall).name shouldBe "find"
        }
    }

    @Nested
    inner class ContainsKeyClassification {
        @Test
        fun `containsKey should be marked as O1`() {
            val result = classifyChainedCall("containsKey", "params", "params", listOf(DUMMY_ARG), loc)
            result.shouldBeInstanceOf<LookupCall>()
            (result as LookupCall).isO1 shouldBe true
        }

        @Test
        fun `containsValue should be marked as O1`() {
            val result = classifyChainedCall("containsValue", "map", "map", listOf(DUMMY_ARG), loc)
            result.shouldBeInstanceOf<LookupCall>()
            (result as LookupCall).isO1 shouldBe true
        }
    }

    @Nested
    inner class IndexOfClassification {
        @Test
        fun `indexOf on string-like target should be FunctionCall`() {
            val result = classifyChainedCall("indexOf", "it.toString()", "it", listOf(DUMMY_ARG), loc)
            result.shouldBeInstanceOf<FunctionCall>()
        }

        @Test
        fun `indexOf on collection-like target should be LookupCall`() {
            val result = classifyChainedCall("indexOf", "items", "items", listOf(DUMMY_ARG), loc)
            result.shouldBeInstanceOf<LookupCall>()
            (result as LookupCall).kind shouldBe LookupKind.INDEX_OF
        }
    }

    @Nested
    inner class O1TypeDetection {
        @Test
        fun `HashMap target should be O1`() {
            isO1Type("new HashMap<>()") shouldBe true
        }

        @Test
        fun `List target should not be O1`() {
            isO1Type("items") shouldBe false
        }

        @Test
        fun `Set target should be O1`() {
            isO1Type("mySet") shouldBe true
        }

        // isO1Type delegates to the registry's o1-types check, which used to be a bare
        // contains() with no boundary at all - "java.sql.ResultSet" contains "Set" and
        // "android.graphics.Bitmap" contains "Map", so both got misread as O(1) lookup
        // types even though neither is really a java.util.Set/Map. Excluded explicitly via
        // the registry's non-o1-type-names YAML list rather than by boundary logic alone,
        // since "ResultSet" satisfies a real camelCase boundary the same way a genuine
        // subclass name like "UserHashMap" does.
        @Test
        fun `ResultSet is not misread as a Set just because it contains one`() {
            isO1Type("resultSet") shouldBe false
            isO1Type("new ResultSet()") shouldBe false
        }

        @Test
        fun `a genuine HashMap subclass name is still recognized as O1`() {
            isO1Type("userHashMap") shouldBe true
        }
    }

    @Nested
    inner class StringTargetDetection {
        // isStringTarget's suffix fallback used to be a bare endsWith - "id"/"key"/"path"/
        // "line"/"value"/"field" are all real string-name-suffixes entries, so any name that
        // merely ends in those letters (not just a real camelCase word) satisfied it too.
        @Test
        fun `a variable merely ending in a string-name-suffix without a real boundary is not a string target`() {
            isStringTarget("monkey") shouldBe false // ends in "key"
            isStringTarget("pipeline") shouldBe false // ends in "line"
            isStringTarget("classpath") shouldBe false // ends in "path"
            isStringTarget("minefield") shouldBe false // ends in "field"
        }

        @Test
        fun `a variable ending in a string-name-suffix at a real camelCase boundary is still a string target`() {
            isStringTarget("userId") shouldBe true
            isStringTarget("cacheKey") shouldBe true
            isStringTarget("filePath") shouldBe true
        }
    }

    companion object {
        private val DUMMY_ARG =
            FunctionCall(
                name = "arg",
                qualifiedTarget = null,
                arguments = emptyList(),
                location = SourceLocation("Test.java", 1, 1),
                children = emptyList(),
            )
    }
}
