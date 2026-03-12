package com.github.tvinke.algorilla.lang.groovy.parser

import com.github.tvinke.algorilla.config.AnalysisConfig
import com.github.tvinke.algorilla.graph.CallGraph
import com.github.tvinke.algorilla.graph.SymbolTable
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.builtin.NestedLookupRule
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.io.File

internal class GroovyLanguageParserTest {
    private val parser = GroovyLanguageParser()

    @Test
    fun `should parse groovy files`() {
        parser.canParse("service.groovy") shouldBe true
        parser.canParse("service.java") shouldBe false
    }

    @Test
    fun `should report groovy language`() {
        parser.language shouldBe Language.GROOVY
    }

    @Test
    fun `should parse each as higher order loop`() {
        val tree = parseFixture("parser/groovy-each-loop.groovy")

        val loops = tree.findDescendants<LoopNode>()
        loops.any { it.kind == LoopKind.HIGHER_ORDER } shouldBe true
    }

    @Test
    fun `should parse method declarations`() {
        val tree = parseFixture("parser/groovy-each-loop.groovy")

        val funcs = tree.findDescendants<FunctionDecl>()
        funcs.any { it.name == "processOrders" } shouldBe true
        funcs.any { it.name == "collectNames" } shouldBe true
    }

    @Test
    fun `should detect nested lookup in groovy each loop`() {
        val tree = parseFixture("parser/nested-lookup-groovy.groovy")
        val path = fixturePath("parser/nested-lookup-groovy.groovy")
        val rule = NestedLookupRule()
        val context =
            AnalysisContext(
                irTrees = mapOf(path to tree),
                symbolTable = SymbolTable(),
                callGraph = CallGraph(),
                config = AnalysisConfig(),
            )

        val findings = rule.evaluate(context)
        findings shouldHaveSize 1
        findings.first().ruleId shouldBe "nested-lookup"
        findings.first().message shouldContain "contains"
    }

    private fun parseFixture(fixturePath: String) = parser.parse(fixturePath(fixturePath))

    private fun fixturePath(fixturePath: String): String {
        val url =
            javaClass.classLoader.getResource("fixtures/$fixturePath")
                ?: error("Fixture not found: $fixturePath")
        return File(url.toURI()).absolutePath
    }
}
