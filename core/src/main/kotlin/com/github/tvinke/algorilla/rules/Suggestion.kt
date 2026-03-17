package com.github.tvinke.algorilla.rules

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.util.VariableNameGenerator

/**
 * A suggested code fix with language and optional framework tag.
 */
public data class CodeSuggestion(
    val code: String,
    val language: String,
    val framework: String? = null,
)

/**
 * Context available to [Suggestion.renderCode] for generating language-specific code snippets.
 */
public data class SuggestionContext(
    val language: Language,
    val imports: Set<String> = emptySet(),
    val bulkAlternatives: Map<String, String> = emptyMap(),
    val detectedFrameworks: Set<String> = emptySet(),
)

public sealed class Suggestion {
    public abstract fun render(): String

    /**
     * Optionally produces a concrete code suggestion. Subclasses override to opt in.
     * Returns null by default — rules that don't support code suggestions don't need to implement.
     */
    public open fun renderCode(ctx: SuggestionContext): CodeSuggestion? = null

    public data class PreBuildStructure(
        val structure: String,
        val variable: String,
        val qualifier: String? = null,
    ) : Suggestion() {
        override fun render(): String =
            "Build a $structure from '$variable' before the loop" +
                (if (qualifier != null) " ($qualifier)" else "")

        override fun renderCode(ctx: SuggestionContext): CodeSuggestion? {
            val isMap = structure.contains("Map")
            val langName = ctx.language.displayName.lowercase()
            val code =
                if (isMap) {
                    renderMapCode(ctx.language)
                } else {
                    renderSetCode(ctx.language)
                }
            return CodeSuggestion(code = code, language = langName)
        }

        private fun renderSetCode(language: Language): String {
            val setName = VariableNameGenerator.suggestSetName(variable)
            return when (language) {
                Language.JAVA -> "Set<$GENERIC> $setName = new HashSet<>($variable);"
                Language.KOTLIN -> "val $setName = $variable.toHashSet()"
                Language.GROOVY -> "def $setName = $variable.toSet()"
                Language.JAVASCRIPT, Language.TYPESCRIPT -> "const $setName = new Set($variable);"
            }
        }

        private fun renderMapCode(language: Language): String {
            val mapName = VariableNameGenerator.suggestMapName(variable, "key")
            return when (language) {
                Language.JAVA ->
                    "Map<$GENERIC, List<$GENERIC>> $mapName = $variable.stream()\n" +
                        "    .collect(Collectors.groupingBy(e -> e.getKey()));"
                Language.KOTLIN -> "val $mapName = $variable.groupBy { it.key }"
                Language.GROOVY -> "def $mapName = $variable.groupBy { it.key }"
                Language.JAVASCRIPT, Language.TYPESCRIPT ->
                    "const $mapName = Map.groupBy($variable, e => e.key);"
            }
        }

        private companion object {
            const val GENERIC = "T" // placeholder — actual generic type requires full type resolution
        }
    }

    public data class AlreadyOptimal(
        val variable: String,
        val currentType: String,
        val reason: String,
    ) : Suggestion() {
        override fun render(): String = "'$variable' is already a $currentType — $reason"
    }

    public data class UseBulkAPI(
        val bulkMethod: String,
        val singleMethod: String,
        val framework: String? = null,
    ) : Suggestion() {
        override fun render(): String {
            val fw = if (framework != null) " ($framework)" else ""
            return "Use $bulkMethod$fw instead of calling $singleMethod per iteration"
        }

        override fun renderCode(ctx: SuggestionContext): CodeSuggestion? {
            val alt = ctx.bulkAlternatives[singleMethod] ?: return null
            val code = "repo.$alt(items)"
            return CodeSuggestion(
                code = code,
                language = ctx.language.displayName.lowercase(),
                framework = framework,
            )
        }
    }

    public data class UseAlternativeAPI(
        val alternative: String,
        val reason: String,
    ) : Suggestion() {
        override fun render(): String = "Use $alternative — $reason"

        override fun renderCode(ctx: SuggestionContext): CodeSuggestion? {
            if (!alternative.contains("removeAll")) return null
            val langName =
                ctx.language.displayName
                    .lowercase()
            val code =
                when (ctx.language) {
                    Language.JAVA -> "items.removeAll(toRemove);"
                    Language.KOTLIN -> "items.removeAll(toRemove)"
                    Language.GROOVY -> "items.removeAll(toRemove)"
                    Language.JAVASCRIPT, Language.TYPESCRIPT ->
                        "const result = items.filter(item => !toRemoveSet.has(item));"
                }
            return CodeSuggestion(code = code, language = langName)
        }
    }

    public data class CacheResult(
        val callDescription: String,
    ) : Suggestion() {
        override fun render(): String = "Cache the result of $callDescription in a local variable"
    }

    public data class HoistBeforeLoop(
        val call: String,
    ) : Suggestion() {
        override fun render(): String = "Hoist $call before the loop — the result is the same on every iteration"
    }

    public data class ReorderOperations(
        val first: String,
        val second: String,
        val reason: String,
    ) : Suggestion() {
        override fun render(): String = "Move $first before $second — $reason"
    }

    public data class Freeform(
        val text: String,
    ) : Suggestion() {
        override fun render(): String = text
    }
}
