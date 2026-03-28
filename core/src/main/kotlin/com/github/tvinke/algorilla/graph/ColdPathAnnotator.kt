package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Pass 2.6: marks [FunctionDecl]s that are startup, initialization, configuration,
 * or migration code as cold paths that run once at application boot or deploy time.
 *
 * Findings inside cold-path methods are technically correct but rarely actionable.
 * Downstream rules can inspect [FunctionDecl.isColdPath] to demote confidence or severity.
 */
public class ColdPathAnnotator(
    private val registry: LanguageSemanticsRegistry = LanguageSemanticsRegistry.DEFAULT,
) {
    public fun annotate(irTrees: Map<String, FileRoot>) {
        var annotated = 0
        for ((_, fileRoot) in irTrees) {
            val language = fileRoot.language
            val methodKeywords = registry.extraSection(language, "cold-path-method-keywords")
            val classPatterns = registry.extraSection(language, "cold-path-class-patterns")
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                if (isColdPath(fn, methodKeywords, classPatterns)) {
                    fn.isColdPath = true
                    annotated++
                }
            }
        }
        logger.info { "Pass 2.6 complete: $annotated methods marked as cold-path" }
    }

    internal fun isColdPath(
        fn: FunctionDecl,
        methodKeywords: Set<String>,
        classPatterns: Set<String>,
    ): Boolean =
        matchesMethodKeyword(fn, methodKeywords) ||
            matchesClassPattern(fn, classPatterns) ||
            isMainOrConstructor(fn)

    private fun matchesMethodKeyword(
        fn: FunctionDecl,
        keywords: Set<String>,
    ): Boolean {
        val name = fn.name.lowercase()
        return keywords.any { name.contains(it) }
    }

    private fun matchesClassPattern(
        fn: FunctionDecl,
        patterns: Set<String>,
    ): Boolean {
        val cls = fn.declaringClass?.lowercase() ?: return false
        return patterns.any { cls.contains(it) }
    }

    private fun isMainOrConstructor(fn: FunctionDecl): Boolean = fn.name == "main" || fn.isConstructor
}
