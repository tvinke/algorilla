package com.github.tvinke.algorilla.lang.template.parser

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Tests for TemplateParser.
 *
 * TODO: rename to match your parser (e.g. PythonParserTest).
 * TODO: add fixture files under src/test/resources/fixtures/parser/ and reference them below.
 */
internal class TemplateParserTest {
    private val parser = TemplateParser()

    @Test
    fun `should only accept the right file extensions`() {
        // TODO: replace with your language's extensions
        // parser.canParse("example.py") shouldBe true
        // parser.canParse("example.kt") shouldBe false
    }

    @Test
    fun `should report the correct language`() {
        // TODO: uncomment once Language enum entry exists
        // parser.language shouldBe Language.YOUR_LANG
    }

    @Nested
    inner class FunctionParsing {
        @Test
        fun `should parse function declarations`() {
            // TODO: parse a fixture, then:
            // val tree = parseFixture("your-fixture.ext")
            // val funcs = tree.findDescendants<FunctionDecl>()
            // funcs.any { it.name == "someFunction" } shouldBe true
        }
    }

    @Nested
    inner class LoopParsing {
        @Test
        fun `should parse for loops`() {
            // TODO: parse a fixture with a for loop, verify LoopNode with correct LoopKind
        }

        @Test
        fun `should parse while loops`() {
            // TODO: parse a fixture with a while loop
        }

        @Test
        fun `should parse higher-order iteration`() {
            // TODO: if your language has forEach/map/etc., verify LoopKind.HIGHER_ORDER
        }
    }

    @Nested
    inner class CallClassification {
        @Test
        fun `should parse lookup calls`() {
            // TODO: parse a fixture with list.contains() or similar, verify LookupCall
        }

        @Test
        fun `should parse sort calls`() {
            // TODO: parse a fixture with list.sort() or similar, verify SortCall
        }

        @Test
        fun `should parse object creation`() {
            // TODO: parse a fixture with constructor calls, verify ObjectCreation
        }
    }

    @Nested
    inner class VariableParsing {
        @Test
        fun `should parse variable declarations`() {
            // TODO: parse a fixture, verify VariableDecl with name and type
        }
    }

    /**
     * Loads a parser fixture from src/test/resources/fixtures/parser/.
     * Put small, focused source snippets there — one per language construct you're testing.
     */
    private fun parseFixture(name: String) =
        parser.parse(
            File(
                javaClass.classLoader.getResource("fixtures/parser/$name")?.toURI()
                    ?: error("Fixture not found: $name"),
            ).absolutePath,
        )
}
