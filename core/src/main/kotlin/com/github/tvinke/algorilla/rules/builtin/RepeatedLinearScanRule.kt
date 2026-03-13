package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.ComplexityModel
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.util.findDescendantsWithBranchContext
import com.github.tvinke.algorilla.util.isCollectionLookup
import com.github.tvinke.algorilla.util.maxCoExecutableSubset

/**
 * Detects multiple linear scans on the same collection within a single function.
 * Each additional scan multiplies the cost; caching or combining into a single pass is preferred.
 *
 * Covers both [LookupCall] nodes (contains, find, filter, etc.) and full-scan [FunctionCall]
 * nodes (groupBy, distinct, toMap, etc.) from the YAML `full-scan-methods` section.
 */
public class RepeatedLinearScanRule : Rule {
    override val id: String = "repeated-linear-scan"
    override val name: String = "Repeated Linear Scan"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.REDUNDANCY

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val fullScanMethods = context.registry.fullScanMethods(fileRoot.language)
            scanNode(fileRoot, fullScanMethods, fileRoot.language, context, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        fullScanMethods: Set<String>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        if (node is FunctionDecl) {
            checkFunction(node, fullScanMethods, language, context, findings)
        }
        for (child in node.children) {
            scanNode(child, fullScanMethods, language, context, findings)
        }
    }

    private fun checkFunction(
        fn: FunctionDecl,
        fullScanMethods: Set<String>,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ) {
        val reportedTargets = checkLookupCalls(fn, language, context, findings)
        checkFullScanCalls(fn, fullScanMethods, reportedTargets, findings)
    }

    private fun checkLookupCalls(
        fn: FunctionDecl,
        language: Language,
        context: AnalysisContext,
        findings: MutableList<Finding>,
    ): Set<String?> {
        val typeEnv = context.typeEnvironmentFor(fn)
        val lookupsWithContext = fn.findDescendantsWithBranchContext<LookupCall>()
        val filteredLookups =
            lookupsWithContext.filter {
                it.first.targetVariable != null &&
                    it.first.isCollectionLookup(fn, typeEnv, language, context.registry)
            }
        val lookupsByTarget = filteredLookups.groupBy { it.first.targetVariable }
        for ((targetVar, callsWithContext) in lookupsByTarget) {
            val coExecutable = maxCoExecutableSubset(callsWithContext)
            if (coExecutable.size >= MIN_SCANS_TO_REPORT) {
                findings.add(buildLookupFinding(fn, targetVar!!, coExecutable))
            }
        }
        return lookupsByTarget.keys
    }

    private fun checkFullScanCalls(
        fn: FunctionDecl,
        fullScanMethods: Set<String>,
        reportedTargets: Set<String?>,
        findings: MutableList<Finding>,
    ) {
        if (fullScanMethods.isEmpty()) return
        val fullScansWithContext = fn.findDescendantsWithBranchContext<FunctionCall>()
        val filteredFullScans =
            fullScansWithContext.filter {
                val call = it.first
                call.name in fullScanMethods &&
                    call.qualifiedTarget != null &&
                    isCollectionVariable(call.qualifiedTarget!!)
            }
        val fullScansByTarget = filteredFullScans.groupBy { it.first.qualifiedTarget }
        for ((targetVar, callsWithContext) in fullScansByTarget) {
            if (targetVar in reportedTargets) continue
            val coExecutable = maxCoExecutableSubset(callsWithContext)
            if (coExecutable.size >= MIN_SCANS_TO_REPORT) {
                findings.add(buildFullScanFinding(fn, targetVar!!, coExecutable))
            }
        }
    }

    private fun buildLookupFinding(
        fn: FunctionDecl,
        targetVar: String,
        lookups: List<LookupCall>,
    ): Finding {
        val cx = ComplexityModel.repeatedScans(lookups.size)
        val opsDesc = lookups.joinToString(" and ") { ".${it.kind.label}()" }
        val structure = dominantStructure(lookups)
        val paramBacked = fn.parameterFlows.any { it.paramName == targetVar }
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (paramBacked) Confidence.HIGH else Confidence.MEDIUM,
            location = lookups.first().location,
            message =
                "'$targetVar' is scanned ${lookups.size} times in ${fn.name}(): " +
                    "each $opsDesc iterates the full collection",
            suggestion =
                "Combine into a single pass, or build a $structure from '$targetVar' for O(1) lookups",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildLookupEvidence(lookups, targetVar),
        )
    }

    private fun buildLookupEvidence(
        lookups: List<LookupCall>,
        targetVar: String,
    ): List<Evidence> =
        lookups.mapIndexed { idx, lookup ->
            val tag = if (idx == 0) "1st" else "#${idx + 1}"
            Evidence(
                location = lookup.location,
                label = ".${lookup.kind.label}() on '$targetVar' ($tag)",
                executionContext = ExecutionContext.SINGLE,
                complexity = ComplexityModel.loopEvidence(targetVar),
            )
        }

    private fun buildFullScanFinding(
        fn: FunctionDecl,
        targetVar: String,
        calls: List<FunctionCall>,
    ): Finding {
        val cx = ComplexityModel.repeatedScans(calls.size)
        val opsDesc = calls.joinToString(" and ") { ".${it.name}()" }
        val paramBacked = fn.parameterFlows.any { it.paramName == targetVar }
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            confidence = if (paramBacked) Confidence.HIGH else Confidence.MEDIUM,
            location = calls.first().location,
            message =
                "'$targetVar' is scanned ${calls.size} times in ${fn.name}(): " +
                    "each $opsDesc iterates the full collection",
            suggestion = "Combine into a single pass over '$targetVar'",
            currentComplexity = cx.current,
            suggestedComplexity = cx.suggested,
            evidence = buildFullScanEvidence(calls, targetVar),
        )
    }

    private fun buildFullScanEvidence(
        calls: List<FunctionCall>,
        targetVar: String,
    ): List<Evidence> =
        calls.mapIndexed { idx, call ->
            val tag = if (idx == 0) "1st" else "#${idx + 1}"
            Evidence(
                location = call.location,
                label = ".${call.name}() on '$targetVar' ($tag)",
                executionContext = ExecutionContext.SINGLE,
                complexity = ComplexityModel.loopEvidence(targetVar),
            )
        }

    internal companion object {
        const val MIN_SCANS_TO_REPORT = 2
    }
}

/**
 * Returns true if the target looks like a collection variable (starts with lowercase).
 * Rejects class references like "Collectors", "Observable", "Stream" which are utility targets,
 * not collection variables being scanned.
 */
private fun isCollectionVariable(target: String): Boolean {
    val firstChar = target.firstOrNull() ?: return false
    return firstChar.isLowerCase() || firstChar == '_'
}

/**
 * Picks the most appropriate structure suggestion based on the lookup kinds.
 * If any lookup needs a Map (find, filter, indexOf, count), suggest Map; otherwise HashSet.
 */
private fun dominantStructure(lookups: List<LookupCall>): String {
    val kinds = lookups.map { it.kind }.toSet()
    val needsMap =
        kinds.any {
            it == LookupKind.FIND ||
                it == LookupKind.FILTER ||
                it == LookupKind.INDEX_OF ||
                it == LookupKind.COUNT
        }
    return if (needsMap) "Map (via groupBy)" else "HashSet"
}
