package com.github.tvinke.algorilla.cli

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CustomRuleLoaderTest {
    @TempDir
    lateinit var tempDir: File

    private fun writeScript(
        name: String,
        content: String,
    ) {
        val rulesDir = File(tempDir, ".algorilla/rules")
        rulesDir.mkdirs()
        File(rulesDir, name).writeText(content)
    }

    @Test
    fun `returns empty list when rules directory does not exist`() {
        CustomRuleLoader.loadRules(tempDir).shouldBeEmpty()
    }

    @Test
    fun `returns empty list when rules directory has no kts files`() {
        File(tempDir, ".algorilla/rules").mkdirs()
        CustomRuleLoader.loadRules(tempDir).shouldBeEmpty()
    }

    @Test
    fun `loads a single rule returned directly from a script`() {
        writeScript(
            "single.kts",
            """
            import com.github.tvinke.algorilla.rules.custom.rule
            rule("custom-single") {}
            """.trimIndent(),
        )
        val rules = CustomRuleLoader.loadRules(tempDir)
        rules shouldHaveSize 1
        rules.first().id shouldBe "custom-single"
    }

    @Test
    fun `loads multiple rules returned as a list from a single script`() {
        writeScript(
            "multi.kts",
            """
            import com.github.tvinke.algorilla.rules.custom.rule
            listOf(rule("custom-a") {}, rule("custom-b") {})
            """.trimIndent(),
        )
        val rules = CustomRuleLoader.loadRules(tempDir)
        rules shouldHaveSize 2
        rules.map { it.id }.toSet() shouldBe setOf("custom-a", "custom-b")
    }

    @Test
    fun `loads rules from more than one script file`() {
        writeScript("first.kts", """import com.github.tvinke.algorilla.rules.custom.rule; rule("custom-first") {}""")
        writeScript("second.kts", """import com.github.tvinke.algorilla.rules.custom.rule; rule("custom-second") {}""")
        val rules = CustomRuleLoader.loadRules(tempDir)
        rules.map { it.id }.toSet() shouldBe setOf("custom-first", "custom-second")
    }

    @Test
    fun `returns empty list when a script does not compile`() {
        writeScript("broken.kts", "this is not valid kotlin {{{")
        CustomRuleLoader.loadRules(tempDir).shouldBeEmpty()
    }

    @Test
    fun `returns empty list when a script does not return a Rule`() {
        writeScript("wrong-type.kts", "\"just a string\"")
        CustomRuleLoader.loadRules(tempDir).shouldBeEmpty()
    }

    @Test
    fun `a broken script does not prevent other scripts from loading`() {
        writeScript("broken.kts", "this is not valid kotlin {{{")
        writeScript("ok.kts", """import com.github.tvinke.algorilla.rules.custom.rule; rule("custom-ok") {}""")
        val rules = CustomRuleLoader.loadRules(tempDir)
        rules shouldHaveSize 1
        rules.first().id shouldBe "custom-ok"
    }
}
