package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.rules.Suggestion
import com.github.tvinke.algorilla.semantics.MethodPurity
import com.github.tvinke.algorilla.util.findDescendantsWithBranchContext
import com.github.tvinke.algorilla.util.maxCoExecutableSubset

/**
 * Detects the same parameterized call invoked multiple times with the same arguments
 * within a single function. The result should be cached in a local variable.
 */
public class RedundantExpensiveCallRule : Rule {
    override val id: String = "redundant-expensive-call"
    override val name: String = "Redundant Expensive Call"
    override val severity: Severity = Severity.INFO
    override val defaultConfidence: Confidence = Confidence.LOW
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.REDUNDANCY
    override val subsumes: Set<String> = setOf("uncached-getter")

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language ?: Language.JAVA
            val nonDeterministic = context.registry.nonDeterministicMethods(language)
            val trivial = context.registry.trivialMethods(language)
            val builder = context.registry.builderMethods(language)
            val cheap = context.registry.cheapMethods(language)
            val seqRead = context.registry.sequentialReadMethods(language)
            val typeCheckPrefixes = context.registry.typeCheckPrefixes(language)
            val sequentialReadPrefixes = context.registry.sequentialReadPrefixes(language)
            val skipSets = SkipSets(trivial, builder, cheap, seqRead, typeCheckPrefixes, sequentialReadPrefixes)
            scanNode(fileRoot, nonDeterministic, skipSets, language, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        nonDeterministic: Set<String>,
        skipSets: SkipSets,
        language: Language,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionDecl) {
            checkFunction(node, nonDeterministic, skipSets, language, findings)
        }
        for (child in node.children) {
            scanNode(child, nonDeterministic, skipSets, language, findings)
        }
    }

    private fun checkFunction(
        fn: FunctionDecl,
        nonDeterministic: Set<String>,
        skipSets: SkipSets,
        language: Language,
        findings: MutableList<Finding>,
    ) {
        val callsWithContext = fn.findDescendantsWithBranchContext<FunctionCall>()
        val filtered = callsWithContext.filter { it.first.arguments.isNotEmpty() && !isSideEffectCall(it.first, skipSets, language) }
        val grouped = filtered.groupBy { callSignature(it.first, nonDeterministic, skipSets.seqRead, skipSets.sequentialReadPrefixes) }

        for ((sig, duplicatesWithContext) in grouped) {
            if (sig.isBlank()) continue
            val coExecutable = maxCoExecutableSubset(duplicatesWithContext)
            // Getter-pattern methods (getX, isX, hasX, toX) are typically cheap —
            // require more duplicates before flagging to reduce noise on accessor calls
            val threshold = if (isGetterPattern(coExecutable.first().name)) GETTER_THRESHOLD else MIN_DUPLICATES
            if (coExecutable.size >= threshold) {
                findings.add(buildFinding(fn, coExecutable))
            }
        }
    }

    private fun buildFinding(
        fn: FunctionDecl,
        calls: List<FunctionCall>,
    ): Finding {
        val first = calls.first()
        val callDesc = "${first.qualifiedTarget ?: ""}${if (first.qualifiedTarget != null) "." else ""}${first.name}()"
        val evidence =
            calls.mapIndexed { idx, call ->
                val tag = if (idx == 0) "1st call" else "duplicate"
                Evidence(call.location, "$callDesc ($tag)", ExecutionContext.SINGLE)
            }
        val cx = ComplexityModel.redundantCalls(calls.size)
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = first.location,
            message = "$callDesc called ${calls.size} times with same arguments in ${fn.name}()",
            suggestions = listOf(Suggestion.CacheResult(callDescription = callDesc)),
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = evidence,
        )
    }

    internal companion object {
        const val MIN_DUPLICATES = 2
        const val GETTER_THRESHOLD = 3
    }
}

private data class SkipSets(
    val trivial: Set<String>,
    val builder: Set<String>,
    val cheap: Set<String>,
    val seqRead: Set<String>,
    val typeCheckPrefixes: Set<String>,
    val sequentialReadPrefixes: Set<String>,
)

/**
 * Creates a signature string from a call's name + argument text for grouping.
 * Uses the children's toString as a rough equality check.
 */
private fun callSignature(
    call: FunctionCall,
    nonDeterministic: Set<String>,
    seqRead: Set<String>,
    sequentialReadPrefixes: Set<String>,
): String {
    val target = call.qualifiedTarget ?: ""
    val argsKey = call.arguments.joinToString(",") { argFingerprint(it, nonDeterministic, seqRead, sequentialReadPrefixes) }
    return "$target.${call.name}($argsKey)"
}

private fun argFingerprint(
    node: IRNode,
    nonDeterministic: Set<String>,
    seqRead: Set<String>,
    sequentialReadPrefixes: Set<String>,
): String =
    when (node) {
        is FunctionCall -> {
            val base = "${node.qualifiedTarget}.${node.name}(${node.arguments.joinToString(
                ",",
            ) { argFingerprint(it, nonDeterministic, seqRead, sequentialReadPrefixes) }})"
            if (containsNonDeterministic(node, nonDeterministic, seqRead, sequentialReadPrefixes)) "$base@${node.location.line}" else base
        }
        is GenericNode ->
            if (node.children.isEmpty()) {
                node.nodeType
            } else {
                val inner =
                    node.children.joinToString(",") {
                        argFingerprint(it, nonDeterministic, seqRead, sequentialReadPrefixes)
                    }
                "${node.nodeType}[$inner]"
            }
        else -> "${node::class.simpleName}@${node.location.line}:${node.location.column}"
    }

private fun containsNonDeterministic(
    call: FunctionCall,
    nonDeterministic: Set<String>,
    seqRead: Set<String>,
    sequentialReadPrefixes: Set<String>,
): Boolean {
    if (call.name in nonDeterministic) return true
    if (call.name in seqRead || isSequentialReadPrefix(call.name, sequentialReadPrefixes)) return true
    return call.children.any { it is FunctionCall && containsNonDeterministic(it, nonDeterministic, seqRead, sequentialReadPrefixes) }
}

private fun isSideEffectCall(
    call: FunctionCall,
    skipSets: SkipSets,
    language: Language,
): Boolean =
    call.name in skipSets.trivial ||
        call.name in skipSets.builder ||
        call.name in skipSets.cheap ||
        call.name in skipSets.seqRead ||
        isSequentialReadPrefix(call.name, skipSets.sequentialReadPrefixes) ||
        isTypeCheckPredicate(call.name, skipSets.typeCheckPrefixes) ||
        isBytecodeInstruction(call.name) ||
        MethodPurity.isSideEffect(call.name, call.qualifiedTarget, language)

private fun isTypeCheckPredicate(
    name: String,
    typeCheckPrefixes: Set<String>,
): Boolean =
    typeCheckPrefixes.any { prefix ->
        name.length > prefix.length &&
            name.startsWith(prefix) &&
            name[prefix.length].isUpperCase()
    }

private fun isSequentialReadPrefix(
    name: String,
    sequentialReadPrefixes: Set<String>,
): Boolean = sequentialReadPrefixes.any { name.length > it.length && name.startsWith(it) }

/** Underscore-prefixed ALL_CAPS methods are typically bytecode instructions (_ALOAD, _ISTORE). */
private fun isBytecodeInstruction(name: String): Boolean = name.startsWith("_") && name.all { it == '_' || it.isUpperCase() }

/**
 * Returns true if the method name matches a getter/accessor pattern (getX, isX, hasX, toX).
 * These are typically cheap O(1) field reads that don't warrant caching when called only twice.
 */
private fun isGetterPattern(name: String): Boolean =
    GETTER_PATTERN_PREFIXES.any { prefix ->
        name.length > prefix.length &&
            name.startsWith(prefix) &&
            name[prefix.length].isUpperCase()
    }

private val GETTER_PATTERN_PREFIXES = listOf("get", "is", "has", "to")
