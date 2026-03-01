package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.shouldBe
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

    @Test
    fun `should parse method declarations`() {
        val tree = parseFixture("kotlin-loops.java")
        val funcs = tree.findDescendants<FunctionDecl>()

        funcs.any { it.name == "processAll" } shouldBe true
        funcs.any { it.name == "filterItems" } shouldBe true
        funcs.any { it.name == "sortAndGet" } shouldBe true
    }

    @Test
    fun `should parse loop constructs`() {
        val tree = parseFixture("kotlin-loops.java")
        val loops = tree.findDescendants<LoopNode>()

        loops.any { it.kind == LoopKind.HIGHER_ORDER } shouldBe true
    }

    @Test
    fun `should parse lookup calls`() {
        val tree = parseFixture("kotlin-loops.java")
        val lookups = tree.findDescendants<LookupCall>()

        lookups.any { it.kind == LookupKind.CONTAINS } shouldBe true
    }

    @Test
    fun `should parse sort calls`() {
        val tree = parseFixture("kotlin-loops.java")
        val sorts = tree.findDescendants<SortCall>()

        sorts.any { it.kind == com.github.tvinke.algorilla.model.SortKind.SORT } shouldBe true
    }

    private fun parseFixture(name: String) =
        parser.parse(
            File(
                javaClass.classLoader.getResource("fixtures/parser/$name")?.toURI()
                    ?: error("Fixture not found: $name"),
            ).absolutePath,
        )
}
