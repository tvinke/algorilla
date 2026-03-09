package com.github.tvinke.algorilla.cli

import com.github.tvinke.algorilla.model.Language
import java.io.File

internal class SourceFileCollector(
    private val detector: ProjectStructureDetector,
) {
    fun collect(
        paths: List<File>,
        excludePatterns: List<String>,
        includeTests: Boolean,
        languageFilter: Set<Language>? = null,
    ): List<String> {
        val testFilter = detector.buildTestExcludeFilter(paths, includeTests)
        val defaultExcludes = detector.defaultExcludePatterns()
        val langFilter: (File) -> Boolean =
            if (languageFilter != null) {
                { file -> Language.fromExtension(file.extension.lowercase())?.let { it in languageFilter } == true }
            } else {
                { true }
            }
        val files = mutableListOf<String>()
        for (path in paths) {
            if (path.isFile) {
                if (isSupportedFile(path) && langFilter(path)) files.add(path.absolutePath)
            } else if (path.isDirectory) {
                path
                    .walkTopDown()
                    .filter { it.isFile && isSupportedFile(it) }
                    .filter(langFilter)
                    .filter { file -> defaultExcludes.none { excl -> file.path.contains(excl) } }
                    .filter(testFilter)
                    .filter { file -> excludePatterns.none { pattern -> matchGlob(pattern, file.path) } }
                    .forEach { files.add(it.absolutePath) }
            }
        }
        return files
    }

    private fun isSupportedFile(file: File): Boolean {
        val ext = file.extension.lowercase()
        return Language.fromExtension(ext) != null
    }

    private fun matchGlob(
        pattern: String,
        path: String,
    ): Boolean {
        val regex =
            pattern
                .replace(".", "\\.")
                .replace("**", "\u00A7\u00A7")
                .replace("*", "[^/]*")
                .replace("\u00A7\u00A7", ".*")
        return Regex(regex).containsMatchIn(path)
    }
}
