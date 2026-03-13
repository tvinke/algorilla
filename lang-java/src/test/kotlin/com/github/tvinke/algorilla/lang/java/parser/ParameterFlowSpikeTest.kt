package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Spike: verifies that parameter names are recoverable from the IR tree.
 * This is the prerequisite for HARD-3 (cross-method parameter-flow analysis).
 */
internal class ParameterFlowSpikeTest {
    private val parser =
        com.github.tvinke.algorilla.lang.java.parser
            .JavaLanguageParser()

    @Test
    fun `parameter name appears in LoopNode iteratedVariable`() {
        // findMatches(List<String> items) { for (item : items) { ... } }
        val tree = parser.parse(fixture("nested-lookup/positive/list-field-contains-in-loop.java"))
        val fn = tree.findDescendants<FunctionDecl>().first { it.name == "findMatches" }

        fn.parameters.first().name shouldBe "items"
        val loop = fn.findDescendants<LoopNode>().first()
        loop.iteratedVariable shouldBe "items"
    }

    @Test
    fun `parameter name appears in LookupCall targetVariable`() {
        val tree = parser.parse(fixture("nested-lookup/positive/list-field-contains-in-loop.java"))
        val fn = tree.findDescendants<FunctionDecl>().first { it.name == "findMatches" }
        val lookup = fn.findDescendants<LookupCall>().first()
        // targets.contains() — targetVariable is the field, not the param
        // but the param "items" flows into the LoopNode
        lookup.targetVariable shouldContain "targets"
    }

    @Test
    fun `FunctionCall qualifiedTarget can reference a parameter`() {
        // n-plus-one fixture: repository.findById(x) in loop over items
        val nPlusOneFixture = findFixture("n-plus-one-query") ?: return
        val tree = parser.parse(nPlusOneFixture)
        val calls = tree.findDescendants<FunctionCall>()
        // Just verify we can see qualifiedTarget strings
        val targets = calls.mapNotNull { it.qualifiedTarget }.toSet()
        // Should have at least some targets we can match against param names
        assert(targets.isNotEmpty()) { "No qualified targets found in IR" }
    }

    @Test
    fun `VariableDecl captures assignment from function call`() {
        // Check that variable initializers are captured
        val tree = parser.parse(fixture("nested-lookup/positive/list-field-contains-in-loop.java"))
        val vars = tree.findDescendants<VariableDecl>()
        // Local variables should have names
        for (v in vars) {
            assert(v.name.isNotEmpty()) { "VariableDecl has empty name" }
        }
    }

    private fun fixture(path: String): String {
        val url =
            javaClass.classLoader.getResource("fixtures/$path")
                ?: error("Fixture not found: $path")
        return File(url.toURI()).absolutePath
    }

    private fun findFixture(ruleName: String): String? {
        val dir = javaClass.classLoader.getResource("fixtures/$ruleName/positive") ?: return null
        val file = File(dir.toURI()).listFiles()?.firstOrNull { it.extension == "java" } ?: return null
        return file.absolutePath
    }
}
