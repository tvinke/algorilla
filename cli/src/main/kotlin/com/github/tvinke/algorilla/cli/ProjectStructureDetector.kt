package com.github.tvinke.algorilla.cli

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

private val GRADLE_SOURCE_DIRS = listOf("java", "kotlin", "groovy")

/**
 * Detects project structure (Gradle, Maven, JS/TS) and resolves the project root,
 * source roots, and test-exclusion patterns from build-system conventions.
 */
internal class ProjectStructureDetector {
    /**
     * Resolves the project root by walking up from the given [dir] until a build-system
     * marker file is found. Returns [dir] itself when no marker is found.
     */
    fun resolveProjectRoot(dir: File): File {
        val resolved = findProjectRoot(dir)
        if (resolved != null) {
            logger.info { "Resolved project root: ${resolved.path}" }
        } else {
            logger.info { "No build system detected, using path as-is: ${dir.path}" }
        }
        return resolved ?: dir.canonicalFile
    }

    /**
     * Returns the source directories to scan for the given [projectRoot].
     *
     * When [userPath] is a subdirectory within the project (not the project root itself),
     * only that subdirectory is returned — the user explicitly requested a targeted scan.
     */
    fun resolveSourceRoots(
        projectRoot: File,
        userPath: File,
    ): List<File> {
        val canonicalRoot = projectRoot.canonicalFile
        val canonicalUser = userPath.canonicalFile

        if (canonicalUser != canonicalRoot) {
            logger.info { "Targeted scan: using explicit path ${canonicalUser.path}" }
            return listOf(canonicalUser)
        }

        val detected = detectSourceRoots(canonicalRoot)
        return detected.ifEmpty { listOf(canonicalRoot) }
    }

    private fun detectSourceRoots(projectRoot: File): List<File> =
        when {
            isGradleProject(projectRoot) -> {
                logger.info { "Detected Gradle project at ${projectRoot.path}" }
                detectGradleSourceRoots(projectRoot)
            }
            isMavenProject(projectRoot) -> {
                logger.info { "Detected Maven project at ${projectRoot.path}" }
                detectMavenSourceRoots(projectRoot)
            }
            isJsProject(projectRoot) -> {
                logger.info { "Detected JS/TS project at ${projectRoot.path}" }
                listOf(projectRoot)
            }
            else -> emptyList()
        }

    private fun detectGradleSourceRoots(projectRoot: File): List<File> {
        val roots = mutableListOf<File>()

        // Root-level source dirs (single-module or root module with code)
        roots.addAll(existingSourceDirs(projectRoot))

        // Submodule source dirs
        val settingsFile = findSettingsFile(projectRoot)
        if (settingsFile != null) {
            for (module in parseGradleModules(settingsFile)) {
                val moduleDir = File(projectRoot, module.replace(":", "/"))
                roots.addAll(existingSourceDirs(moduleDir))
            }
        }

        // Fallback: when no source dirs found (e.g. custom settings DSL), walk the tree
        if (roots.isEmpty()) {
            logger.info { "No source roots from settings parsing, falling back to directory walk" }
            roots.addAll(walkForSourceDirs(projectRoot))
        }

        return roots
    }

    private fun detectMavenSourceRoots(projectRoot: File): List<File> {
        val roots = mutableListOf<File>()

        // Root-level source dirs
        roots.addAll(existingSourceDirs(projectRoot))

        // Submodule source dirs
        val pomContent = File(projectRoot, "pom.xml").readText()
        val modulePattern = Regex("""<module>\s*([^<]+)\s*</module>""")
        for (match in modulePattern.findAll(pomContent)) {
            val moduleDir = File(projectRoot, match.groupValues[1].trim())
            roots.addAll(existingSourceDirs(moduleDir))
        }

        return roots
    }

    private fun existingSourceDirs(moduleDir: File): List<File> =
        GRADLE_SOURCE_DIRS
            .map { File(moduleDir, "src/main/$it") }
            .filter { it.isDirectory }

    private fun walkForSourceDirs(projectRoot: File): List<File> {
        val excludeDirNames = setOf("build", ".git", ".gradle", "node_modules")
        val sourceTargets = GRADLE_SOURCE_DIRS.map { "src/main/$it" }.toSet()

        return projectRoot
            .walkTopDown()
            .onEnter { dir -> dir == projectRoot || dir.name !in excludeDirNames }
            .filter { it.isDirectory && sourceTargets.any { target -> it.path.endsWith(target) } }
            .toList()
    }

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
            isGradleProject(projectRoot) -> detectGradleTestDirs(projectRoot)
            isMavenProject(projectRoot) -> setOf("/src/test/")
            else -> emptySet()
        }
    }

    private fun findProjectRoot(dir: File): File? {
        var current = if (dir.isFile) dir.parentFile else dir
        while (current != null) {
            if (isGradleProject(current) || isMavenProject(current) || isJsProject(current)) {
                return current
            }
            current = current.parentFile
        }
        return null
    }

    private fun detectGradleTestDirs(projectRoot: File): Set<String> {
        val patterns = mutableSetOf("/src/test/")
        val settingsFile = findSettingsFile(projectRoot)
        if (settingsFile != null) {
            for (module in parseGradleModules(settingsFile)) {
                val modulePath = module.replace(":", "/")
                patterns.add("/$modulePath/src/test/")
            }
        }
        return patterns
    }

    private fun findSettingsFile(projectRoot: File): File? =
        listOf("settings.gradle.kts", "settings.gradle")
            .map { File(projectRoot, it) }
            .firstOrNull { it.exists() }

    private fun parseGradleModules(settingsFile: File): List<String> {
        val content = settingsFile.readText()
        val modules = mutableListOf<String>()

        // Kotlin DSL: include("core", "cli") or include("core")
        val ktsDsl = Regex("""include\s*\(([^)]+)\)""")
        for (match in ktsDsl.findAll(content)) {
            val args = match.groupValues[1]
            val quoted = Regex(""""([^"]+)"""")
            for (q in quoted.findAll(args)) {
                modules.add(q.groupValues[1].removePrefix(":"))
            }
        }

        // Groovy DSL: include 'core', 'cli' or include ':core', ':cli'
        val groovyDsl = Regex("""include\s+(['"][^)]*?)(?:\n|$)""")
        for (match in groovyDsl.findAll(content)) {
            val line = match.groupValues[1]
            val quoted = Regex("""['"]([^'"]+)['"]""")
            for (q in quoted.findAll(line)) {
                modules.add(q.groupValues[1].removePrefix(":"))
            }
        }

        return modules.distinct()
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
                "/.algorilla/",
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

        private fun isJsProject(dir: File): Boolean = File(dir, "package.json").exists()
    }
}
