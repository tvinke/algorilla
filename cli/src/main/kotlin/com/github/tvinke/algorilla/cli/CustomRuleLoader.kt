package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.rules.Rule
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import javax.script.ScriptEngineManager

private val logger = KotlinLogging.logger {}

private const val CUSTOM_RULES_DIR = ".algorilla/rules"

/**
 * Loads custom analysis rules from `.kts` Kotlin Script files in the `.algorilla/rules/` directory.
 * Each script file should use the `rule()` DSL function to define one or more rules.
 */
internal object CustomRuleLoader {
    fun loadRules(scanRoot: File): List<Rule> {
        val rulesDir = File(scanRoot, CUSTOM_RULES_DIR)
        if (!rulesDir.isDirectory) return emptyList()

        val scriptFiles = rulesDir.listFiles { f -> f.extension == "kts" } ?: return emptyList()
        if (scriptFiles.isEmpty()) return emptyList()

        logger.info { "Loading ${scriptFiles.size} custom rule script(s) from ${rulesDir.path}" }
        return scriptFiles.flatMap { loadScript(it) }
    }

    private fun loadScript(file: File): List<Rule> {
        return try {
            val engine = ScriptEngineManager().getEngineByExtension("kts")
            if (engine == null) {
                logger.warn { "Kotlin script engine not available, cannot load ${file.name}" }
                return emptyList()
            }
            val result = engine.eval(file.readText())
            when (result) {
                is Rule -> listOf(result).also { logger.info { "Loaded custom rule: ${result.id}" } }
                is List<*> -> {
                    result
                        .filterIsInstance<Rule>()
                        .also { rules -> rules.forEach { logger.info { "Loaded custom rule: ${it.id}" } } }
                }
                else -> {
                    logger.warn { "Script ${file.name} did not return a Rule" }
                    emptyList()
                }
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.warn { "Failed to load custom rule from ${file.name}: ${e.message}" }
            emptyList()
        }
    }
}
