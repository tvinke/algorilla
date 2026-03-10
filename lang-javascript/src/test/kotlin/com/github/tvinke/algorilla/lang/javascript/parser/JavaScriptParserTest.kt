package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

internal class JavaScriptParserTest {
    private val parser = JavaScriptParser()

    @Test
    fun `should parse js files`() {
        parser.canParse("app.js") shouldBe true
        parser.canParse("app.ts") shouldBe true
        parser.canParse("app.tsx") shouldBe true
        parser.canParse("app.vue") shouldBe true
        parser.canParse("app.java") shouldBe false
    }

    @Test
    fun `should report JavaScript language`() {
        parser.language shouldBe Language.JAVASCRIPT
    }

    @Test
    fun `should parse function declarations`() {
        val tree = parseFixture("loops-and-lookups.js")
        val funcs = tree.findDescendants<FunctionDecl>()

        funcs.any { it.name == "processItems" } shouldBe true
        funcs.any { it.name == "filterActive" } shouldBe true
        funcs.any { it.name == "searchItem" } shouldBe true
        funcs.any { it.name == "sortAndPick" } shouldBe true
    }

    @Test
    fun `should parse loop kinds`() {
        val tree = parseFixture("loops-and-lookups.js")
        val loops = tree.findDescendants<LoopNode>()

        loops.any { it.kind == LoopKind.HIGHER_ORDER } shouldBe true
        loops.any { it.kind == LoopKind.FOR } shouldBe true
        loops.any { it.kind == LoopKind.WHILE } shouldBe true
    }

    @Test
    fun `should parse lookup calls`() {
        val tree = parseFixture("loops-and-lookups.js")
        val lookups = tree.findDescendants<LookupCall>()

        lookups.any { it.kind == LookupKind.FILTER } shouldBe true
        lookups.any { it.kind == LookupKind.INCLUDES } shouldBe true
        lookups.any { it.kind == LookupKind.FIND } shouldBe true
        lookups.any { it.kind == LookupKind.INDEX_OF } shouldBe true
    }

    @Test
    fun `should parse sort calls`() {
        val tree = parseFixture("loops-and-lookups.js")
        val sorts = tree.findDescendants<SortCall>()

        sorts shouldHaveSize 1
    }

    @Test
    fun `should parse object creation`() {
        val tree = parseFixture("object-creation.ts")
        val creations = tree.findDescendants<ObjectCreation>()

        creations.any { it.typeName == "ObjectMapper" } shouldBe true
        creations.any { it.typeName == "Item" } shouldBe true
        creations.any { it.typeName == "Date" } shouldBe true
    }

    @Test
    fun `should extract script from Vue SFC`() {
        val tree = parseFixture("vue-component.vue")
        val funcs = tree.findDescendants<FunctionDecl>()
        val lookups = tree.findDescendants<LookupCall>()

        funcs.any { it.name == "loadUsers" } shouldBe true
        lookups.any { it.kind == LookupKind.INCLUDES } shouldBe true
        lookups.any { it.kind == LookupKind.FILTER } shouldBe true
    }

    @Test
    fun `should extract typed parameters from TS function declarations`() {
        val tree = parseFixture("parameters.ts")
        val funcs = tree.findDescendants<FunctionDecl>()

        val greet = funcs.first { it.name == "greet" }
        greet.parameters shouldContainExactly
            listOf(
                Parameter("name", "string"),
                Parameter("age", "number"),
            )
    }

    @Test
    fun `should extract Set and Map type names from TS parameters`() {
        val tree = parseFixture("parameters.ts")
        val funcs = tree.findDescendants<FunctionDecl>()

        val lookup = funcs.first { it.name == "lookupUser" }
        lookup.parameters shouldHaveSize 2
        lookup.parameters[0].name shouldBe "users"
        lookup.parameters[0].typeName shouldBe "Set"
        lookup.parameters[1].name shouldBe "id"
        lookup.parameters[1].typeName shouldBe "string"
    }

    @Test
    fun `should extract parameters from const arrow function with types`() {
        val tree = parseFixture("parameters.ts")
        val funcs = tree.findDescendants<FunctionDecl>()

        val process = funcs.first { it.name == "processItems" }
        process.parameters shouldHaveSize 2
        process.parameters[0].name shouldBe "items"
        process.parameters[0].typeName shouldBe "Map"
        process.parameters[1].name shouldBe "filter"
        process.parameters[1].typeName shouldBe "boolean"
    }

    @Test
    fun `should extract parameters from class methods`() {
        val tree = parseFixture("parameters.ts")
        val funcs = tree.findDescendants<FunctionDecl>()

        val findById = funcs.first { it.name == "findById" }
        findById.parameters shouldHaveSize 2
        findById.parameters[0].name shouldBe "id"
        findById.parameters[1].name shouldBe "cache"
        findById.parameters[1].typeName shouldBe "Map"
    }

    @Test
    fun `should extract untyped JS parameters`() {
        val tree = parseFixture("parameters.ts")
        val funcs = tree.findDescendants<FunctionDecl>()

        val noTypes = funcs.first { it.name == "noTypes" }
        noTypes.parameters shouldHaveSize 3
        noTypes.parameters.map { it.name } shouldContainExactly listOf("a", "b", "c")
        noTypes.parameters.all { it.typeName == null } shouldBe true
    }

    @Test
    fun `should extract parameters from const arrow function without types`() {
        val tree = parseFixture("parameters.ts")
        val funcs = tree.findDescendants<FunctionDecl>()

        val arrow = funcs.first { it.name == "arrowNoTypes" }
        arrow.parameters shouldHaveSize 2
        arrow.parameters.map { it.name } shouldContainExactly listOf("x", "y")
    }

    @Test
    fun `should extract parameters from existing JS fixture`() {
        val tree = parseFixture("loops-and-lookups.js")
        val funcs = tree.findDescendants<FunctionDecl>()

        val process = funcs.first { it.name == "processItems" }
        process.parameters shouldHaveSize 1
        process.parameters[0].name shouldBe "items"

        val search = funcs.first { it.name == "searchItem" }
        search.parameters shouldHaveSize 2
        search.parameters.map { it.name } shouldContainExactly listOf("items", "target")

        val filterActive = funcs.first { it.name == "filterActive" }
        filterActive.parameters shouldHaveSize 1
        filterActive.parameters[0].name shouldBe "users"
    }

    private fun parseFixture(name: String) =
        parser.parse(
            File(
                javaClass.classLoader.getResource("fixtures/parser/$name")?.toURI()
                    ?: error("Fixture not found: $name"),
            ).absolutePath,
        )
}
