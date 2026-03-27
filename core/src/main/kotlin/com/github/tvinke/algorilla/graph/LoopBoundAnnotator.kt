package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.ControlFlowExit
import com.github.tvinke.algorilla.model.ExitKind
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Pass 2.5: marks [LoopNode]s whose iteration bound is a compile-time constant
 * (enum values, small literal collections, numeric literal bounds, config/registry keywords)
 * and loops whose body always exits after a single iteration (throw/break/return on every path).
 *
 * Downstream rules can inspect [LoopNode.isConstantBound] and [LoopNode.isSingleIteration]
 * to decide whether the loop contributes meaningful algorithmic growth.
 */
public class LoopBoundAnnotator(
    private val registry: LanguageSemanticsRegistry = LanguageSemanticsRegistry.DEFAULT,
) {
    public fun annotate(irTrees: Map<String, FileRoot>) {
        var constantBound = 0
        var singleIteration = 0
        for ((_, fileRoot) in irTrees) {
            val language = fileRoot.language
            for (loop in fileRoot.findDescendants<LoopNode>()) {
                if (isConstantBound(loop, language)) {
                    loop.isConstantBound = true
                    constantBound++
                }
                if (isSingleIteration(loop)) {
                    loop.isSingleIteration = true
                    singleIteration++
                }
            }
        }
        logger.info { "Pass 2.5 complete: $constantBound constant-bound, $singleIteration single-iteration" }
    }

    internal fun isConstantBound(
        loop: LoopNode,
        language: Language,
    ): Boolean =
        isEnumIteration(loop, language) ||
            isConstantBoundKeyword(loop, language) ||
            isLiteralNumericBound(loop)

    /**
     * Enum `.values()` / Kotlin `.entries` / `EnumSet.allOf(...)` / `EnumSet.of(...)`.
     *
     * Since `extractVariableName` is now language-aware, `.values()` is preserved for
     * Java/Kotlin/Groovy (it's only a stream-op in JavaScript). No bare-name heuristic needed.
     */
    @Suppress("ReturnCount") // Guard clauses for each pattern variant
    private fun isEnumIteration(
        loop: LoopNode,
        language: Language,
    ): Boolean {
        val iterVar = loop.iteratedVariable ?: return false

        // Type.values() — Java/Groovy/Kotlin
        if (iterVar.endsWith(".values()")) {
            val prefix = iterVar.substringBefore(".values()")
            if (prefix.isNotEmpty() && prefix[0].isUpperCase()) return true
        }

        // Type.entries — Kotlin
        if (language == Language.KOTLIN && iterVar.endsWith(".entries")) {
            val prefix = iterVar.substringBefore(".entries")
            if (prefix.isNotEmpty() && prefix[0].isUpperCase()) return true
        }

        // EnumSet.allOf(...) / EnumSet.of(...)
        if (iterVar.startsWith("EnumSet.allOf(") || iterVar.startsWith("EnumSet.of(")) {
            return true
        }

        return false
    }

    /**
     * Checks YAML `constant-bound-keywords` — iterating mappers, validators, handlers, etc.
     */
    private fun isConstantBoundKeyword(
        loop: LoopNode,
        language: Language,
    ): Boolean {
        val keywords = registry.extraSection(language, "constant-bound-keywords")
        if (keywords.isEmpty()) return false
        val loopVar = loop.iteratedVariable?.lowercase() ?: return false
        return keywords.any { kw -> loopVar.contains(kw) }
    }

    /**
     * Traditional for-loop with a literal numeric bound, e.g. `for (int i = 0; i < 5; i++)`.
     * The parser represents these as [LoopKind.FOR] with a numeric literal in the iteratedVariable.
     */
    private fun isLiteralNumericBound(loop: LoopNode): Boolean {
        if (loop.kind != LoopKind.FOR) return false
        val iterVar = loop.iteratedVariable ?: return false
        // The parser may encode the bound as just a number
        return iterVar.toIntOrNull() != null
    }

    /**
     * Detects loops where every top-level code path through the body exits via
     * throw, break, or return — meaning the loop runs at most one iteration.
     *
     * This covers patterns like:
     * - `for (x : items) { doSomething(); break; }` (unconditional exit)
     * - `for (x : items) { if (a) throw; else return; }` (all branches exit)
     */
    internal fun isSingleIteration(loop: LoopNode): Boolean {
        if (loop.children.isEmpty()) return false
        return endsWithExit(loop.children)
    }

    /**
     * Returns true if the given IR node sequence always terminates in a [ControlFlowExit]
     * (throw/break/return). For [BranchNode]s (if/else), both branches must exit.
     */
    private fun endsWithExit(nodes: List<IRNode>): Boolean {
        if (nodes.isEmpty()) return false
        val last = nodes.last()
        if (last is ControlFlowExit && last.kind in LOOP_TERMINATING_EXITS) return true
        if (last is BranchNode) {
            return last.branches.size >= 2 &&
                last.branches.all { branch ->
                    branch.isNotEmpty() && endsWithExit(branch)
                }
        }
        return false
    }

    private companion object {
        val LOOP_TERMINATING_EXITS = setOf(ExitKind.THROW, ExitKind.BREAK, ExitKind.RETURN)
    }
}
