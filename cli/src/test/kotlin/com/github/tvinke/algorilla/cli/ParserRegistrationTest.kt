package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.engine.ParserRegistry
import com.github.tvinke.algorilla.lang.groovy.parser.GroovyLanguageParser
import com.github.tvinke.algorilla.lang.java.parser.JavaLanguageParser
import com.github.tvinke.algorilla.lang.javascript.parser.JavaScriptLanguageParser
import com.github.tvinke.algorilla.lang.kotlin.parser.KotlinLanguageParser
import com.github.tvinke.algorilla.model.Language
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * Verifies that all language parsers register themselves correctly via companion object init.
 * If you add a new language module, add its parser to the assertions below.
 * A failing test here means the parser was implemented but not wired into [registerAllParsers].
 */
internal class ParserRegistrationTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun triggerRegistration() {
            // Touch companions the same way the CLI does — triggers self-registration
            JavaLanguageParser.Companion
            GroovyLanguageParser.Companion
            KotlinLanguageParser.Companion
            JavaScriptLanguageParser.Companion
        }
    }

    @Test
    fun `all language parsers should be registered`() {
        val parsers = ParserRegistry.all()
        parsers shouldHaveAtLeastSize 4
        parsers.map { it.language } shouldContainAll
            listOf(
                Language.JAVA,
                Language.GROOVY,
                Language.KOTLIN,
                Language.JAVASCRIPT,
            )
    }

    @Test
    fun `each parser should handle its file extensions`() {
        ParserRegistry.forFile("Example.java") shouldNotBe null
        ParserRegistry.forFile("Example.groovy") shouldNotBe null
        ParserRegistry.forFile("Example.kt") shouldNotBe null
        ParserRegistry.forFile("Example.js") shouldNotBe null
        ParserRegistry.forFile("Example.ts") shouldNotBe null
        ParserRegistry.forFile("Example.tsx") shouldNotBe null
        ParserRegistry.forFile("Example.vue") shouldNotBe null
    }
}
