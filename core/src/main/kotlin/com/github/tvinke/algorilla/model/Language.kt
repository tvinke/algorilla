package com.github.tvinke.algorilla.model

/**
 * Supported source languages for analysis.
 */
public enum class Language(
    public val displayName: String,
    public val extensions: Set<String>,
) {
    JAVA("Java", setOf("java")),
    GROOVY("Groovy", setOf("groovy")),
    KOTLIN("Kotlin", setOf("kt", "kts")),
    JAVASCRIPT("JavaScript", setOf("js", "mjs", "cjs")),
    TYPESCRIPT("TypeScript", setOf("ts", "tsx")),
    VUE("Vue", setOf("vue")),
    ;

    public companion object {
        /**
         * Detects the language from a file extension, or returns null if unsupported.
         */
        public fun fromExtension(extension: String): Language? = entries.find { extension.lowercase() in it.extensions }

        /**
         * Resolves a language by name (case-insensitive), matching against enum name or display name.
         */
        public fun fromName(name: String): Language? {
            val normalized = name.trim().lowercase()
            return entries.find { it.name.lowercase() == normalized || it.displayName.lowercase() == normalized }
        }
    }
}
