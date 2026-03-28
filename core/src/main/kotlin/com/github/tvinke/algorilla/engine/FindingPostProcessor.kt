package com.github.tvinke.algorilla.engine

import com.github.tvinke.algorilla.model.Confidence
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
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
 * Demotes findings inside cold-path methods (startup, initialization, configuration)
 * to LOW confidence. These findings are technically correct but rarely actionable.
 */
internal fun demoteColdPathFindings(
    findings: List<Finding>,
    irTrees: Map<String, FileRoot>,
): List<Finding> {
    // Build an index of cold-path line ranges per file for efficient lookup
    val coldRanges = mutableMapOf<String, MutableList<IntRange>>()
    for ((_, fileRoot) in irTrees) {
        for (fn in fileRoot.findDescendants<FunctionDecl>()) {
            if (fn.isColdPath) {
                val start = fn.location.line
                val end = fn.children.maxOfOrNull { it.location.line } ?: start
                coldRanges.getOrPut(fileRoot.filePath) { mutableListOf() }.add(start..end)
            }
        }
    }
    if (coldRanges.isEmpty()) return findings
    return findings.map { finding ->
        val ranges = coldRanges[finding.location.file]
        if (ranges != null && ranges.any { finding.location.line in it }) {
            finding.copy(confidence = Confidence.LOW)
        } else {
            finding
        }
    }
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

        // Step 1: Set baseline from rule's defaultConfidence for MEDIUM findings (promotion/demotion).
        //         Cap non-MEDIUM findings at the ceiling (prevents LOW rules from emitting HIGH).
        val baselined =
            when {
                finding.confidence == Confidence.MEDIUM -> ceiling
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
