package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.ControlFlowExit
import com.github.tvinke.algorilla.model.ExitKind
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LoopBoundAnnotatorTest {
    private val loc = SourceLocation("Test.java", 1, 1)
    private val annotator = LoopBoundAnnotator()

    private fun loop(
        iteratedVariable: String?,
        kind: LoopKind = LoopKind.FOR_EACH,
    ) = LoopNode(kind = kind, iteratedVariable = iteratedVariable, location = loc, children = emptyList())

    private fun fileRoot(
        language: Language = Language.JAVA,
        vararg loops: LoopNode,
    ) = FileRoot(
        filePath = "Test.java",
        language = language,
        location = loc,
        children = loops.toList(),
    )

    @Nested
    inner class EnumIteration {
        @Test
        fun `should mark Type_values() as constant-bound`() {
            val l = loop("Status.values()")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe true
        }

        @Test
        fun `should mark Type_values() as constant-bound for Groovy`() {
            val l = loop("Priority.values()")
            val root = fileRoot(Language.GROOVY, l)
            annotator.annotate(mapOf("Test.groovy" to root))

            l.isConstantBound shouldBe true
        }

        @Test
        fun `should mark Kotlin entries as constant-bound`() {
            val l = loop("Status.entries")
            val root = fileRoot(Language.KOTLIN, l)
            annotator.annotate(mapOf("Test.kt" to root))

            l.isConstantBound shouldBe true
        }

        @Test
        fun `should not mark lowercase variable as enum`() {
            val l = loop("items")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe false
        }

        @Test
        fun `should not mark bare uppercase name as constant-bound`() {
            // With language-aware extractVariableName, .values() is preserved —
            // bare names like "Status" without .values() are not enum iteration
            val l = loop("Status")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe false
        }

        @Test
        fun `should mark EnumSet_allOf as constant-bound`() {
            val l = loop("EnumSet.allOf(Status.class)")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe true
        }
    }

    @Nested
    inner class ConstantBoundKeywords {
        @Test
        fun `should mark loop over mappers as constant-bound`() {
            val l = loop("mappers")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe true
        }

        @Test
        fun `should mark loop over validators as constant-bound`() {
            val l = loop("this.validators")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe true
        }

        @Test
        fun `should not mark loop over orders as constant-bound`() {
            val l = loop("orders")
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe false
        }
    }

    @Nested
    inner class LiteralNumericBound {
        @Test
        fun `should mark for-loop with numeric bound as constant-bound`() {
            val l = loop("5", kind = LoopKind.FOR)
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe true
        }

        @Test
        fun `should not mark for-each loop with numeric iterated variable`() {
            val l = loop("5", kind = LoopKind.FOR_EACH)
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe false
        }

        @Test
        fun `should not mark for-loop with variable bound`() {
            val l = loop("items.size()", kind = LoopKind.FOR)
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe false
        }
    }

    @Nested
    inner class NullAndEmpty {
        @Test
        fun `should not mark loop with null iteratedVariable`() {
            val l = loop(null)
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isConstantBound shouldBe false
        }
    }

    @Nested
    inner class SingleIteration {
        private fun call(name: String): FunctionCall = FunctionCall(name, null, emptyList(), loc, emptyList())

        private fun throwExit(): ControlFlowExit = ControlFlowExit(ExitKind.THROW, loc)

        private fun breakExit(): ControlFlowExit = ControlFlowExit(ExitKind.BREAK, loc)

        private fun returnExit(): ControlFlowExit = ControlFlowExit(ExitKind.RETURN, loc)

        private fun loopWith(vararg children: IRNode) = LoopNode(LoopKind.FOR_EACH, "items", loc, children.toList())

        @Test
        fun `should mark loop that ends with throw`() {
            val l = loopWith(call("save"), throwExit())
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe true
        }

        @Test
        fun `should mark loop that ends with break`() {
            val l = loopWith(call("remove"), breakExit())
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe true
        }

        @Test
        fun `should mark loop that ends with return`() {
            val l = loopWith(call("process"), returnExit())
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe true
        }

        @Test
        fun `should mark loop with if-else where both branches exit`() {
            val branch =
                BranchNode(
                    branches =
                        listOf(
                            listOf(call("save"), throwExit()),
                            listOf(call("log"), returnExit()),
                        ),
                    location = loc,
                )
            val l = loopWith(call("validate"), branch)
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe true
        }

        @Test
        fun `should not mark loop where only one branch exits`() {
            val branch =
                BranchNode(
                    branches =
                        listOf(
                            listOf(call("save"), throwExit()),
                            listOf(call("log")), // no exit — loop continues
                        ),
                    location = loc,
                )
            val l = loopWith(branch)
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe false
        }

        @Test
        fun `should not mark loop without control flow exit`() {
            val l = loopWith(call("process"), call("save"))
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe false
        }

        @Test
        fun `should not mark empty loop`() {
            val l = loopWith()
            val root = fileRoot(Language.JAVA, l)
            annotator.annotate(mapOf("Test.java" to root))

            l.isSingleIteration shouldBe false
        }
    }
}
