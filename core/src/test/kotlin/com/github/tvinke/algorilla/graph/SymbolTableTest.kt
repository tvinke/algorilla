package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.SourceLocation
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class SymbolTableTest {
    @Test
    fun `should register and lookup by qualified name`() {
        val table = SymbolTable()
        val decl = makeDecl("com.example.Service.process", "process")
        table.register(decl)

        table.lookup("com.example.Service.process") shouldHaveSize 1
        table.lookup("com.example.Service.process").first() shouldBe decl
    }

    @Test
    fun `should lookup by simple name`() {
        val table = SymbolTable()
        table.register(makeDecl("com.a.Service.validate", "validate"))
        table.register(makeDecl("com.b.Helper.validate", "validate"))

        table.lookupBySimpleName("validate") shouldHaveSize 2
    }

    @Test
    fun `should return empty for unknown names`() {
        val table = SymbolTable()

        table.lookup("unknown") shouldHaveSize 0
        table.lookupBySimpleName("unknown") shouldHaveSize 0
    }

    private fun makeDecl(
        qualified: String,
        simple: String,
    ) = FunctionDecl(
        name = simple,
        qualifiedName = qualified,
        parameters = emptyList(),
        location = SourceLocation("test.java", 1, 1),
        children = emptyList(),
    )
}
