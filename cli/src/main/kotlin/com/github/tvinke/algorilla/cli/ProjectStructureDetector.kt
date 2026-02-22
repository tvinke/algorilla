package com.github.tvinke.algorilla.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Detects Gradle and Maven project structures and determines which directories
 * contain test code that should be excluded from analysis by default.
 */
internal class ProjectStructureDetector {
    /**
     * Returns a predicate that filters out test source files based on the detected
     * project structure. When [includeTests] is true, no files are excluded.
     */
    fun buildTestExcludeFilter(
        scanRoots: List<File>,
        includeTests: Boolean,
    ): (File) -> Boolean {
        if (includeTests) return { true }

        val testPatterns = mutableSetOf<String>()
        for (root in scanRoots) {
            testPatterns.addAll(detectTestPatterns(root))
        }

        if (testPatterns.isEmpty()) {
            testPatterns.addAll(FALLBACK_TEST_PATTERNS)
        }

        logger.debug { "Test exclusion patterns: $testPatterns" }
        return { file -> testPatterns.none { pattern -> file.path.contains(pattern) } }
    }

    private fun detectTestPatterns(root: File): Set<String> {
        val projectRoot = findProjectRoot(root) ?: return emptySet()
        return when {
            isGradleProject(projectRoot) -> {
                logger.info { "Detected Gradle project at ${projectRoot.path}" }
                detectGradleTestDirs(projectRoot)
            }
            isMavenProject(projectRoot) -> {
                logger.info { "Detected Maven project at ${projectRoot.path}" }
                setOf("/src/test/")
            }
            else -> emptySet()
        }
    }

    private fun findProjectRoot(dir: File): File? {
        var current = if (dir.isFile) dir.parentFile else dir
        while (current != null) {
            if (isGradleProject(current) || isMavenProject(current)) return current
            current = current.parentFile
        }
        return null
    }

    private fun detectGradleTestDirs(projectRoot: File): Set<String> {
        val patterns = mutableSetOf("/src/test/")
        val settingsFile =
            listOf("settings.gradle.kts", "settings.gradle")
                .map { File(projectRoot, it) }
                .firstOrNull { it.exists() }

        if (settingsFile != null) {
            val modules = parseGradleModules(settingsFile)
            for (module in modules) {
                val modulePath = module.replace(":", "/")
                patterns.add("/$modulePath/src/test/")
            }
        }
        return patterns
    }

    private fun parseGradleModules(settingsFile: File): List<String> {
        val content = settingsFile.readText()
        val modules = mutableListOf<String>()
        val includePattern = Regex("""include\s*\(\s*"([^"]+)"\s*\)""")
        for (match in includePattern.findAll(content)) {
            modules.add(match.groupValues[1])
        }
        return modules
    }

    /**
     * Returns directories that should always be excluded from scanning.
     */
    fun defaultExcludePatterns(): Set<String> = DEFAULT_EXCLUDES

    companion object {
        private val DEFAULT_EXCLUDES =
            setOf(
                "/node_modules/",
                "/build/",
                "/target/",
                "/dist/",
                "/.gradle/",
                "/.git/",
                "/.idea/",
                "/generated/",
            )

        private val FALLBACK_TEST_PATTERNS =
            setOf(
                "/src/test/",
                "/test/",
                "/__tests__/",
                ".test.",
                ".spec.",
            )

        private fun isGradleProject(dir: File): Boolean =
            File(dir, "build.gradle.kts").exists() ||
                File(dir, "build.gradle").exists() ||
                File(dir, "settings.gradle.kts").exists() ||
                File(dir, "settings.gradle").exists()

        private fun isMavenProject(dir: File): Boolean = File(dir, "pom.xml").exists()
    }
}
