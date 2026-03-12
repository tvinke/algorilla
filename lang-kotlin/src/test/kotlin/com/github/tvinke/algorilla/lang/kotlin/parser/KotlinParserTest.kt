package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

internal class KotlinParserTest {
    private val parser = KotlinParser()

    @Test
    fun `should parse kt and kts files`() {
        parser.canParse("Main.kt") shouldBe true
        parser.canParse("build.gradle.kts") shouldBe true
        parser.canParse("Main.java") shouldBe false
    }

    @Test
    fun `should report Kotlin language`() {
        parser.language shouldBe Language.KOTLIN
    }

    @Nested
    inner class FunctionParsing {
        @Test
        fun `should parse method declarations`() {
            val tree = parseFixture("kotlin-loops.kt")
            val funcs = tree.findDescendants<FunctionDecl>()

            funcs.any { it.name == "processAll" } shouldBe true
            funcs.any { it.name == "filterItems" } shouldBe true
            funcs.any { it.name == "sortAndGet" } shouldBe true
        }

        @Test
        fun `should parse function parameters with types`() {
            val tree = parseFixture("kotlin-generic-params.kt")
            val func = tree.findDescendants<FunctionDecl>().single { it.name == "filterByIds" }

            func.parameters.size shouldBe 2
            func.parameters[0] shouldBe Parameter(name = "ids", typeName = "List<String>")
            func.parameters[1] shouldBe Parameter(name = "active", typeName = "HashSet<String>")
        }

        @Test
        fun `should set declaring class for methods`() {
            val tree = parseFixture("kotlin-loops.kt")
            val func = tree.findDescendants<FunctionDecl>().first { it.name == "processAll" }

            func.declaringClass shouldBe "ItemProcessor"
            func.qualifiedName shouldBe "ItemProcessor.processAll"
        }
    }

    @Nested
    inner class LoopParsing {
        @Test
        fun `should parse higher-order loops`() {
            val tree = parseFixture("kotlin-loops.kt")
            val loops = tree.findDescendants<LoopNode>()

            loops.any { it.kind == LoopKind.HIGHER_ORDER } shouldBe true
        }

        @Test
        fun `should parse for-in loop`() {
            val tree = parseFixture("kotlin-control-flow.kt")
            val loops = tree.findDescendants<LoopNode>()

            loops.any { it.kind == LoopKind.FOR_EACH && it.iteratedVariable == "items" } shouldBe true
        }

        @Test
        fun `should parse while loop`() {
            val tree = parseFixture("kotlin-control-flow.kt")
            val loops = tree.findDescendants<LoopNode>()

            loops.any { it.kind == LoopKind.WHILE } shouldBe true
        }
    }

    @Nested
    inner class CallClassification {
        @Test
        fun `should parse lookup calls`() {
            val tree = parseFixture("kotlin-loops.kt")
            val lookups = tree.findDescendants<LookupCall>()

            lookups.any { it.kind == LookupKind.CONTAINS } shouldBe true
        }

        @Test
        fun `should parse sort calls`() {
            val tree = parseFixture("kotlin-loops.kt")
            val sorts = tree.findDescendants<SortCall>()

            sorts.any { it.kind == SortKind.SORT } shouldBe true
        }

        @Test
        fun `should parse object creation`() {
            val tree = parseFixture("kotlin-chains.kt")
            val creations = tree.findDescendants<ObjectCreation>()

            creations.any { it.typeName == "HashMap" } shouldBe true
            creations.any { it.typeName == "HashSet" } shouldBe true
        }
    }

    @Nested
    inner class ControlFlow {
        @Test
        fun `should parse if-else as branch node`() {
            val tree = parseFixture("kotlin-control-flow.kt")
            val branches = tree.findDescendants<BranchNode>()

            branches shouldHaveAtLeastSize 1
        }

        @Test
        fun `should parse when expression as branch node`() {
            val tree = parseFixture("kotlin-control-flow.kt")
            val func = tree.findDescendants<FunctionDecl>().single { it.name == "withWhen" }
            val branches = func.findDescendants<BranchNode>()

            branches shouldHaveAtLeastSize 1
            branches.first().branches.size shouldBe 3
        }

        @Test
        fun `should parse try-catch as branch node`() {
            val tree = parseFixture("kotlin-control-flow.kt")
            val func = tree.findDescendants<FunctionDecl>().single { it.name == "withTryCatch" }
            val branches = func.findDescendants<BranchNode>()

            branches shouldHaveAtLeastSize 1
        }
    }

    @Nested
    inner class ChainedCalls {
        @Test
        fun `should parse chained higher-order calls`() {
            val tree = parseFixture("kotlin-chains.kt")
            val loops = tree.findDescendants<LoopNode>()
            val lookups = tree.findDescendants<LookupCall>()

            // map and forEach are HIGHER_ORDER, filter is a LookupCall
            loops.filter { it.kind == LoopKind.HIGHER_ORDER }.size shouldBe 2
            lookups.any { it.kind == LookupKind.FILTER } shouldBe true
        }

        @Test
        fun `should parse property declarations`() {
            val tree = parseFixture("kotlin-chains.kt")
            val vars = tree.findDescendants<VariableDecl>()

            vars.any { it.name == "map" } shouldBe true
            vars.any { it.name == "set" } shouldBe true
            vars.any { it.name == "name" && it.typeName == "String" } shouldBe true
        }
    }

    private fun parseFixture(name: String) =
        parser.parse(
            File(
                javaClass.classLoader.getResource("fixtures/parser/$name")?.toURI()
                    ?: error("Fixture not found: $name"),
            ).absolutePath,
        )
}
