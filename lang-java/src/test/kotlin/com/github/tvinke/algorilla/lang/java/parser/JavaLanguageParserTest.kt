package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

internal class JavaLanguageParserTest {
    private val parser = JavaLanguageParser()

    @Test
    fun `should report Java as supported language`() {
        parser.language shouldBe Language.JAVA
    }

    @Test
    fun `should accept java files`() {
        parser.canParse("Example.java") shouldBe true
        parser.canParse("Example.kt") shouldBe false
        parser.canParse("Example.groovy") shouldBe false
    }

    @Test
    fun `should parse basic for loop`() {
        val root = parseFixture("loops.java")
        val loops = root.findDescendants<LoopNode>()
        val forLoops = loops.filter { it.kind == LoopKind.FOR }

        forLoops shouldHaveSize 1
        forLoops.first().iteratedVariable shouldBe null
    }

    @Test
    fun `should parse enhanced for loop with iterated variable`() {
        val root = parseFixture("loops.java")
        val loops = root.findDescendants<LoopNode>()
        val forEachLoops = loops.filter { it.kind == LoopKind.FOR_EACH }

        forEachLoops shouldHaveSize 1
        forEachLoops.first().iteratedVariable shouldBe "items"
    }

    @Test
    fun `should parse while loop`() {
        val root = parseFixture("loops.java")
        val loops = root.findDescendants<LoopNode>()
        val whileLoops = loops.filter { it.kind == LoopKind.WHILE }

        whileLoops shouldHaveSize 2
    }

    @Test
    fun `should parse stream forEach as stream loop`() {
        val root = parseFixture("loops.java")
        val loops = root.findDescendants<LoopNode>()
        val streamLoops = loops.filter { it.kind == LoopKind.STREAM_FOR_EACH }

        streamLoops shouldHaveSize 1
        streamLoops.first().iteratedVariable shouldBe "items"
    }

    @Test
    fun `should parse higher-order forEach`() {
        val root = parseFixture("loops.java")
        val loops = root.findDescendants<LoopNode>()
        val higherOrderLoops = loops.filter { it.kind == LoopKind.HIGHER_ORDER }

        higherOrderLoops shouldHaveSize 1
        higherOrderLoops.first().iteratedVariable shouldBe "items"
    }

    @Test
    fun `should detect all loop kinds`() {
        val root = parseFixture("loops.java")
        val loops = root.findDescendants<LoopNode>()
        val kinds = loops.map { it.kind }.toSet()

        kinds shouldBe
            setOf(
                LoopKind.FOR,
                LoopKind.FOR_EACH,
                LoopKind.WHILE,
                LoopKind.STREAM_FOR_EACH,
                LoopKind.HIGHER_ORDER,
            )
    }

    @Test
    fun `should parse contains as lookup call`() {
        val root = parseFixture("method-calls.java")
        val lookups = root.findDescendants<LookupCall>()
        val containsCalls = lookups.filter { it.kind == LookupKind.CONTAINS }

        containsCalls shouldHaveSize 4
    }

    @Test
    fun `should parse indexOf as lookup call`() {
        val root = parseFixture("method-calls.java")
        val lookups = root.findDescendants<LookupCall>()
        val indexOfCalls = lookups.filter { it.kind == LookupKind.INDEX_OF }

        indexOfCalls shouldHaveSize 2
    }

    @Test
    fun `should parse sort calls`() {
        val root = parseFixture("method-calls.java")
        val sorts = root.findDescendants<SortCall>()

        sorts shouldHaveSize 2
    }

    @Test
    fun `should parse collection access calls`() {
        val root = parseFixture("method-calls.java")
        val accesses = root.findDescendants<CollectionAccess>()

        accesses shouldHaveSize 3
    }

    @Test
    fun `should parse filter as lookup call`() {
        val root = parseFixture("method-calls.java")
        val lookups = root.findDescendants<LookupCall>()
        val filterCalls = lookups.filter { it.kind == LookupKind.FILTER }

        filterCalls shouldHaveSize 1
    }

    @Test
    fun `should parse object creation`() {
        val root = parseFixture("method-calls.java")
        val creations = root.findDescendants<ObjectCreation>()
        val typeNames = creations.map { it.typeName }

        typeNames shouldContainExactly listOf("HashMap<>", "Object")
    }

    @Test
    fun `should parse method declarations with parameters`() {
        val root = parseFixture("declarations.java")
        val methods = root.findDescendants<FunctionDecl>()
        val withParams = methods.find { it.name == "methodWithParams" }!!

        withParams.parameters shouldHaveSize 2
        withParams.parameters[0].name shouldBe "name"
        withParams.parameters[0].typeName shouldBe "String"
        withParams.parameters[1].name shouldBe "count"
        withParams.parameters[1].typeName shouldBe "int"
    }

    @Test
    fun `should parse local variable declarations`() {
        val root = parseFixture("declarations.java")
        val vars = root.findDescendants<VariableDecl>()

        vars.map { it.name } shouldContainExactly listOf("count", "name", "items", "x", "y")
    }

    @Test
    fun `should set correct source locations`() {
        val root = parseFixture("declarations.java")
        val methods = root.findDescendants<FunctionDecl>()
        val simpleMethod = methods.find { it.name == "simpleMethod" }!!

        simpleMethod.location.line shouldBe 5
    }

    @Test
    fun `should mark constant-size factory lookups as isO1`() {
        val root = parseFixture("method-calls.java")
        val func = root.findDescendants<FunctionDecl>().single { it.name == "constantSizeFactories" }
        val lookups = func.findDescendants<LookupCall>()

        lookups.single { it.kind == LookupKind.CONTAINS }.isO1 shouldBe true
        lookups.single { it.kind == LookupKind.INDEX_OF }.isO1 shouldBe true
    }

    @Test
    fun `should suppress loop on constant-size factory forEach`() {
        val root = parseFixture("method-calls.java")
        val func = root.findDescendants<FunctionDecl>().single { it.name == "constantSizeFactories" }
        val loops = func.findDescendants<LoopNode>()

        loops shouldHaveSize 0
    }

    private fun parseFixture(name: String): IRNode {
        val url =
            javaClass.classLoader.getResource("fixtures/parser/$name")
                ?: error("Fixture not found: $name")
        val path = File(url.toURI()).absolutePath
        return parser.parse(path)
    }
}
