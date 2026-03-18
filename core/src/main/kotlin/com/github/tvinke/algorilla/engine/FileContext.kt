package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.Language
import kotlinx.serialization.Serializable

/**
 * Per-file metadata enriched incrementally by each pipeline pass.
 * Carries structural information (language, imports, class names) that
 * downstream passes and rules can consume without re-parsing.
 */
@Serializable
public data class FileContext(
    val filePath: String,
    val language: String,
    val imports: Set<String> = emptySet(),
    val classNames: Set<String> = emptySet(),
    val detectedFrameworks: Set<String> = emptySet(),
    val exportedTypes: Map<String, String> = emptyMap(),
    val dependsOn: Set<String> = emptySet(),
) {
    public fun languageEnum(): Language? = Language.fromName(language)
}
