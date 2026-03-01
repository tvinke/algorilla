package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CallGraphBuilderTest {
    private val loc = SourceLocation("test.java", 1, 1)

    @Test
    fun `should create edges from function calls to known declarations`() {
        val callee = decl("validate")
        val caller = decl("process", call("validate"))
        val graph = buildGraph(mapOf("test.java" to listOf(caller, callee)))

        graph.callees("process") shouldBe setOf("validate")
        graph.edgeCount() shouldBe 1
    }

    @Test
    fun `should not create edges for unresolved calls`() {
        val caller = decl("process", call("unknownMethod"))
        val graph = buildGraph(mapOf("test.java" to listOf(caller)))

        graph.edgeCount() shouldBe 0
    }

    @Test
    fun `should build cross-file edges`() {
        val callee = decl("helper")
        val caller = decl("main", call("helper"))
        val graph = buildGraph(mapOf("a.java" to listOf(caller), "b.java" to listOf(callee)))

        graph.callees("main") shouldBe setOf("helper")
        graph.edgeCount() shouldBe 1
    }

    private fun decl(
        name: String,
        vararg children: IRNode,
    ) = FunctionDecl(name = name, qualifiedName = name, parameters = emptyList(), location = loc, children = children.toList())

    private fun call(name: String) =
        FunctionCall(name = name, qualifiedTarget = null, arguments = emptyList(), location = loc, children = emptyList())

    private fun buildGraph(fileDecls: Map<String, List<FunctionDecl>>): CallGraph {
        val symbolTable = SymbolTable()
        val irTrees = mutableMapOf<String, FileRoot>()
        for ((file, decls) in fileDecls) {
            decls.forEach { symbolTable.register(it) }
            irTrees[file] = FileRoot(file, Language.JAVA, SourceLocation(file, 1, 1), decls)
        }
        return CallGraphBuilder(symbolTable).build(irTrees)
    }
}
