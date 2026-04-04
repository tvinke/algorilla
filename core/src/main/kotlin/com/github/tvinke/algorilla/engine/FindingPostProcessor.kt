package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Demotes findings in vendored/generated code to LOW confidence.
 * Vendored libraries (e.g. Spring's embedded ASM, cglib) produce noise from rules
 * designed for application code. The patterns are technically correct but not actionable.
 */
internal fun demoteVendoredCode(findings: List<Finding>): List<Finding> =
    findings.map { finding ->
        if (isVendoredCode(finding.location.file)) {
            finding.copy(confidence = Confidence.LOW)
        } else {
            finding
        }
    }

/** Known vendored/generated package path fragments. */
private val VENDORED_PATH_PATTERNS =
    listOf(
        // Spring Framework vendors ASM and cglib
        "/asm/",
        "/cglib/",
        // Vendored code markers
        "/internal/shaded/",
        "/shaded/",
        "/vendor/",
        "/thirdparty/",
        "/repackaged/",
        // Generated code
        "/generated/",
        "/generated-sources/",
    )

private fun isVendoredCode(filePath: String): Boolean = VENDORED_PATH_PATTERNS.any { filePath.contains(it) }

/**
 * Demotes findings inside constructors to LOW confidence.
 * Constructors run once per object creation — findings there are technically correct
 * but almost never actionable (catalog init, entity hydration, config building).
 */
internal fun demoteConstructorFindings(
    findings: List<Finding>,
    irTrees: Map<String, FileRoot>,
): List<Finding> {
    val constructorRanges = mutableMapOf<String, MutableList<IntRange>>()
    for ((_, fileRoot) in irTrees) {
        for (fn in fileRoot.findDescendants<FunctionDecl>()) {
            if (fn.isConstructor) {
                val start = fn.location.line
                val end = maxLineOf(fn)
                constructorRanges.getOrPut(fileRoot.filePath) { mutableListOf() }.add(start..end)
            }
        }
    }
    if (constructorRanges.isEmpty()) return findings
    return findings.map { finding ->
        val ranges = constructorRanges[finding.location.file]
        if (ranges != null && ranges.any { finding.location.line in it }) {
            finding.copy(confidence = Confidence.LOW)
        } else {
            finding
        }
    }
}

/**
 * Demotes findings in lifecycle/init methods to LOW confidence.
 * Lifecycle methods (@PostConstruct, @Bean, InitializingBean.afterPropertiesSet) run at
 * startup or configuration time — findings there are technically correct but not actionable.
 */
internal fun demoteLifecycleFindings(
    findings: List<Finding>,
    irTrees: Map<String, FileRoot>,
    registry: LanguageSemanticsRegistry,
): List<Finding> {
    val lifecycleRanges = mutableMapOf<String, MutableList<IntRange>>()
    for ((_, fileRoot) in irTrees) {
        val language = fileRoot.language
        val methodAnnotations = registry.extraSection(language, "lifecycle-method-annotations")
        val lifecycleInterfaces = registry.extraSection(language, "lifecycle-interfaces")
        if (methodAnnotations.isEmpty() && lifecycleInterfaces.isEmpty()) continue

        // Build a set of known lifecycle interface callback method names
        val interfaceCallbacks = buildInterfaceCallbacks(lifecycleInterfaces)

        for (fn in fileRoot.findDescendants<FunctionDecl>()) {
            val isLifecycleAnnotated = fn.annotations.any { it in methodAnnotations }
            val isInterfaceCallback = fn.name in interfaceCallbacks && isInLifecycleClass(fn, fileRoot, lifecycleInterfaces)
            if (isLifecycleAnnotated || isInterfaceCallback) {
                val start = fn.location.line
                val end = maxLineOf(fn)
                lifecycleRanges.getOrPut(fileRoot.filePath) { mutableListOf() }.add(start..end)
            }
        }
    }
    if (lifecycleRanges.isEmpty()) return findings
    return findings.map { finding ->
        val ranges = lifecycleRanges[finding.location.file]
        if (ranges != null && ranges.any { finding.location.line in it }) {
            finding.copy(confidence = Confidence.LOW)
        } else {
            finding
        }
    }
}

private val KNOWN_CALLBACKS =
    mapOf(
        "InitializingBean" to "afterPropertiesSet",
        "DisposableBean" to "destroy",
        "SmartLifecycle" to "start",
        "SmartInitializingSingleton" to "afterSingletonsInstantiated",
    )

private fun buildInterfaceCallbacks(lifecycleInterfaces: Set<String>): Set<String> =
    KNOWN_CALLBACKS.entries
        .filter { it.key in lifecycleInterfaces }
        .map { it.value }
        .toSet()

private fun isInLifecycleClass(
    fn: FunctionDecl,
    fileRoot: FileRoot,
    lifecycleInterfaces: Set<String>,
): Boolean {
    val declaringClass = fn.declaringClass ?: return false
    return fileRoot
        .findDescendants<ClassNode>()
        .any { it.name == declaringClass && it.supertypes.any { st -> st in lifecycleInterfaces } }
}

/**
 * Copies [FunctionDecl.pathContext] to each [Finding] based on enclosing method line range.
 * Does NOT affect confidence or detection — purely metadata enrichment.
 */
internal fun enrichPathContext(
    findings: List<Finding>,
    irTrees: Map<String, FileRoot>,
): List<Finding> {
    val contextRanges = mutableMapOf<String, MutableList<Pair<IntRange, com.github.tvinke.algorilla.model.PathContext>>>()
    for ((_, fileRoot) in irTrees) {
        for (fn in fileRoot.findDescendants<FunctionDecl>()) {
            val ctx = fn.pathContext ?: continue
            val start = fn.location.line
            val end = maxLineOf(fn)
            contextRanges.getOrPut(fileRoot.filePath) { mutableListOf() }.add((start..end) to ctx)
        }
    }
    if (contextRanges.isEmpty()) return findings
    return findings.map { finding ->
        val ranges = contextRanges[finding.location.file]
        val matchedContext = ranges?.firstOrNull { finding.location.line in it.first }?.second
        if (matchedContext != null) finding.copy(pathContext = matchedContext) else finding
    }
}

/** Recursively finds the maximum source line in an IR subtree. */
private fun maxLineOf(node: IRNode): Int {
    var max = node.location.line
    for (child in node.children) {
        val childMax = maxLineOf(child)
        if (childMax > max) max = childMax
    }
    return max
}

/**
 * Adjusts confidence per finding based on the rule's declared [Rule.defaultConfidence],
 * type evidence, and language context.
 *
 * The rule's [defaultConfidence] is the baseline. The engine may further adjust:
 * - Demote to LOW when the rule requires type context but the language lacks type declarations.
 * - Cap at MEDIUM for cross-method findings (evidence spanning multiple files).
 * - Demote to LOW for non-HIGH rules in languages without type declarations.
 */
internal fun adjustConfidence(
    findings: List<Finding>,
    fileLanguages: Map<String, Language>,
    ruleIndex: Map<String, Rule>,
): List<Finding> =
    findings.map { finding ->
        val language = fileLanguages[finding.location.file]
        val rule = ruleIndex[finding.ruleId]
        val ceiling = rule?.defaultConfidence ?: Confidence.MEDIUM

        // Step 1: Set baseline from rule's defaultConfidence for MEDIUM findings.
        //         Respect explicit rule-level HIGH promotion from MEDIUM-default rules
        //         (these have reliable evidence criteria like repo-target-match, flow-confirmed).
        //         LOW-default rules are heuristic-heavy — their HIGH promotions stay capped.
        val baselined =
            when {
                finding.confidence == Confidence.MEDIUM -> ceiling
                finding.confidence == Confidence.HIGH && ceiling >= Confidence.MEDIUM -> Confidence.HIGH
                finding.confidence.ordinal > ceiling.ordinal -> ceiling
                else -> finding.confidence
            }

        // Step 2: Apply language/evidence-based demotions (never promotes).
        val adjusted = applyDemotions(baselined, finding, language, rule)
        if (adjusted != finding.confidence) finding.copy(confidence = adjusted) else finding
    }

/**
 * Applies language and evidence-based demotions. Never promotes — only lowers confidence
 * when the evidence is weaker than the baseline suggests.
 */
private fun applyDemotions(
    confidence: Confidence,
    finding: Finding,
    language: Language?,
    rule: Rule?,
): Confidence {
    // Demote when rule requires type context but language lacks type declarations
    val lacksTypes = language != null && !language.hasTypeDeclarations
    if (lacksTypes && rule?.requiresTypeContext == true) return Confidence.LOW

    // Cross-method findings: cap at MEDIUM (inference across files is less certain)
    val isCrossMethod = finding.evidence.any { it.location.file != finding.location.file }
    if (isCrossMethod && confidence.ordinal > Confidence.MEDIUM.ordinal) return Confidence.MEDIUM

    // Languages without type declarations and non-HIGH confidence: demote to LOW
    if (lacksTypes && confidence != Confidence.HIGH) return Confidence.LOW

    return confidence
}

/**
 * When two rules flag the same source location for overlapping reasons, keep the
 * more specific one. Rules declare which other rules they subsume via [Rule.subsumes].
 *
 * For each (file, line) group, if rule A fired there and rule B also fired there,
 * and A declares that it subsumes B, then B's finding is dropped.
 */
internal fun applySubsumption(
    findings: List<Finding>,
    rules: List<Rule>,
): List<Finding> {
    val subsumptionIndex: Map<String, Set<String>> =
        rules.filter { it.subsumes.isNotEmpty() }.associate { it.id to it.subsumes }
    if (subsumptionIndex.isEmpty()) return findings

    data class LocationKey(
        val file: String,
        val line: Int,
    )

    val byLocation = findings.groupBy { LocationKey(it.location.file, it.location.line) }
    val subsumed = mutableSetOf<Finding>()

    for ((_, group) in byLocation) {
        if (group.size < 2) continue
        val ruleIdsPresent = group.map { it.ruleId }.toSet()
        for (finding in group) {
            if (finding in subsumed) continue
            val dominated =
                subsumptionIndex.entries
                    .filter { (dominantId, _) -> dominantId in ruleIdsPresent && dominantId != finding.ruleId }
                    .any { (_, subs) -> finding.ruleId in subs }
            if (dominated) subsumed.add(finding)
        }
    }

    return findings.filter { it !in subsumed }
}
