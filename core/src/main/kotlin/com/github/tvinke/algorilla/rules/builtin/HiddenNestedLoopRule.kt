package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.CrossMethodResolver
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Detects loops hidden behind method calls: when a loop calls a method that internally
 * contains another loop, the combined complexity is O(outer × inner) but looks like O(n).
 *
 * Only reports when the called method can be resolved via the symbol table and contains
 * a LoopNode in its body. Does not flag trivial/pure methods or methods already covered
 * by more specific rules (e.g. nested-lookup for contains() inside loops).
 */
public class HiddenNestedLoopRule : Rule {
    override val id: String = "hidden-nested-loop"
    override val name: String = "Hidden Nested Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            scanNode(fileRoot, emptyList(), context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        loopStack: List<LoopNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, loopStack + node, context, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall) {
            checkForHiddenLoop(node, loopStack, context, findings)
        }

        for (child in node.children) {
            scanNode(child, loopStack, context, findings)
        }
    }

    private fun checkForHiddenLoop(
        call: FunctionCall,
        loopStack: List<LoopNode>,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (isStringOrCopyMethod(call.name)) return

        val resolved = CrossMethodResolver.resolve(call, context.symbolTable) ?: return
        val hiddenLoop = resolved.findDescendants<LoopNode>().firstOrNull() ?: return

        // Skip trivial methods (single-statement wrappers with no real loop body)
        if (isTrivialLoop(hiddenLoop)) return

        findings.add(buildFinding(call, resolved, hiddenLoop, loopStack))
    }

    private fun buildFinding(
        call: FunctionCall,
        resolved: FunctionDecl,
        hiddenLoop: LoopNode,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerVar = (loopStack.first().iteratedVariable ?: "items")
        val innerVar = hiddenLoop.iteratedVariable ?: "elements"
        val cx = ComplexityModel.loopTimesLookup(outerVar, innerVar)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message =
                "${call.name}() contains a ${hiddenLoop.kind.label()} \u2014 " +
                    "hidden O($outerVar \u00d7 $innerVar) complexity",
            suggestion =
                "Consider inlining the loop, batching the work, " +
                    "or pre-building a lookup structure in ${resolved.name}()",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildEvidence(call, resolved, hiddenLoop, loopStack, innerVar),
        )
    }

    private fun buildEvidence(
        call: FunctionCall,
        resolved: FunctionDecl,
        hiddenLoop: LoopNode,
        loopStack: List<LoopNode>,
        innerVar: String,
    ): List<Evidence> =
        loopStack.mapIndexed { idx, loop ->
            val varName = loop.iteratedVariable ?: "items"
            Evidence(
                location = loop.location,
                label = "${loop.kind.label()} over $varName",
                executionContext = ExecutionContext.INSIDE_LOOP,
                depth = idx,
                complexity = ComplexityModel.loopEvidence(varName),
            )
        } +
            listOf(
                Evidence(
                    call.location,
                    "${call.name}() called per iteration",
                    ExecutionContext.INSIDE_LOOP,
                    depth = loopStack.size,
                ),
                Evidence(
                    hiddenLoop.location,
                    "${hiddenLoop.kind.label()} over $innerVar inside ${resolved.name}()",
                    ExecutionContext.INSIDE_LOOP,
                    depth = loopStack.size + 1,
                    complexity = ComplexityModel.bottleneckO(innerVar),
                ),
            )
}

/** Filter out trivial loops with no meaningful body (e.g. empty forEach). */
private fun isTrivialLoop(loop: LoopNode): Boolean = loop.children.isEmpty()

/**
 * Methods whose internal loop iterates over characters/bytes rather than business
 * collections, or that are simple collection-copy operations already covered by
 * the `in-loop-collection-building` rule. Flagging these as "hidden nested loops"
 * produces false positives because their cost is O(string_length) or O(copy_size),
 * not O(collection_size).
 */
private val SKIP_METHODS =
    setOf(
        // String utility — char iteration
        "hasText",
        "hasLength",
        "trimWhitespace",
        "trimAllWhitespace",
        "replace",
        "replaceAll",
        "replaceFirst",
        "contains",
        "indexOf",
        "lastIndexOf",
        "startsWith",
        "endsWith",
        "matches",
        "strip",
        "stripLeading",
        "stripTrailing",
        "trim",
        "chars",
        "codePoints",
        "deleteAny",
        "substringMatch",
        "split",
        "join",
        "concat",
        // Fundamental object operations — loop inside equals/hashCode/compareTo is unavoidable
        "equals",
        "hashCode",
        "toString",
        "compareTo",
        "deepEquals",
        // Synchronization / waiting — while-loop is polling, not iteration
        "await",
        "awaitTermination",
        "awaitNanos",
        // DOM / XML traversal — inherently iterates child nodes
        "getChildElementsByTagName",
        "getElementsByTagName",
        "getElementsByTagNameNS",
        // XML / encoding helpers
        "writeXmlEncoded",
        "writeCDATA",
        "writeCharacterReference",
        // Canonical / parsing
        "canonicalPropertyName",
        // Bytecode reader methods
        "readElementValues",
        "readTypeAnnotationTarget",
        // URI / template processing — iterates chars, not business collections
        "expandUriComponent",
        "verifyUriComponent",
        // Collection copy — cost is already O(n) and captured by collection-building rule
        "addAll",
        "addAllFirst",
        "putAll",
        "removeAll",
        "retainAll",
        "containsAll",
    )

/** Prefixes that indicate string/byte processing rather than collection iteration. */
private val STRING_PROCESSING_PREFIXES =
    listOf(
        "encode",
        "decode",
        "parse",
        "format",
        "read",
        "write",
        "trim",
        "strip",
        "canonical",
        "delete",
        "substring",
    )

/**
 * Returns true if the method name is a known string/byte iterator or collection-copy
 * operation that should not be flagged as a hidden nested loop.
 */
private fun isStringOrCopyMethod(name: String): Boolean {
    if (name in SKIP_METHODS) return true
    val lower = name.lowercase()
    if (STRING_PROCESSING_PREFIXES.any { lower.startsWith(it) }) return true
    // Methods with "String", "Char", "Uri" in name are typically string processors
    return STRING_CONTENT_KEYWORDS.any { lower.contains(it) }
}

/** Keywords in method names that indicate string/char/URI processing. */
private val STRING_CONTENT_KEYWORDS =
    listOf("string", "char", "uri", "url", "regex", "pattern", "token")
