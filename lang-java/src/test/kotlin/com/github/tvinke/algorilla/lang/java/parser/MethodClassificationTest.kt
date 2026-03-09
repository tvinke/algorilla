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
