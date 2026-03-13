package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.model.ExecutionContext
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.Severity
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.rules.AnalysisContext
import com.github.tvinke.algorilla.rules.Evidence
import com.github.tvinke.algorilla.rules.Finding
import com.github.tvinke.algorilla.rules.Rule
import com.github.tvinke.algorilla.rules.RuleCategory
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.hasO1Type

/**
 * Detects String methods that recompile a regex on every call when used inside loops.
 * Methods like String.matches(), split(), replaceAll(), and replaceFirst() call Pattern.compile()
 * internally, making each invocation O(pattern-length) for compilation alone. In JS/TS, regex
 * literals passed to replace/match/split are recompiled each iteration.
 */
public class RegexRecompilationInLoopRule : Rule {
    override val id: String = "regex-recompilation-in-loop"
    override val name: String = "Regex Recompilation In Loop"
    override val severity: Severity = Severity.WARNING
    override val languages: Set<Language> = Language.entries.toSet()
    override val category: RuleCategory = RuleCategory.LOOP_AMPLIFIER

    override fun evaluate(context: AnalysisContext): List<Finding> {
        val findings = mutableListOf<Finding>()
        for ((_, fileRoot) in context.irTrees) {
            val language = (fileRoot as? FileRoot)?.language
            val methods = language?.let { context.registry.regexRecompilationMethods(it) } ?: emptySet()
            scanNode(fileRoot, null, emptyList(), language, methods, findings)
        }
        return findings
    }

    private fun scanNode(
        node: IRNode,
        enclosingFn: FunctionDecl?,
        loopStack: List<LoopNode>,
        language: Language?,
        regexMethods: Set<String>,
        findings: MutableList<Finding>,
    ) {
        val fn = if (node is FunctionDecl) node else enclosingFn

        if (node is LoopNode) {
            for (child in node.children) {
                scanNode(child, fn, loopStack + node, language, regexMethods, findings)
            }
            return
        }

        if (loopStack.isNotEmpty() && node is FunctionCall && isRegexRecompilationCall(node, fn, language, regexMethods)) {
            findings.add(buildFinding(node, loopStack))
        }

        for (child in node.children) {
            scanNode(child, fn, loopStack, language, regexMethods, findings)
        }
    }

    private fun buildFinding(
        call: FunctionCall,
        loopStack: List<LoopNode>,
    ): Finding {
        val outerLoop = loopStack.first()
        val loopVar = outerLoop.iteratedVariable ?: "items"
        val target = call.qualifiedTarget ?: "string"
        val evidence =
            listOf(
                Evidence(outerLoop.location, outerLoop.kind.label(), ExecutionContext.INSIDE_LOOP, complexity = "O(|$loopVar|)"),
                Evidence(
                    call.location,
                    "$target.${call.name}() inside loop",
                    ExecutionContext.INSIDE_LOOP,
                    depth = 1,
                    complexity = "compile ← bottleneck",
                ),
            )
        return Finding(
            ruleId = id,
            ruleName = name,
            severity = severity,
            location = call.location,
            message = "${call.name}() compiles a regex on every call inside ${outerLoop.kind.label()}",
            suggestion = "Pre-compile with Pattern.compile() outside the loop and use Matcher directly",
            currentComplexity = "O(|$loopVar| × compile)",
            suggestedComplexity = "O(|$loopVar|)",
            evidence = evidence,
        )
    }
}

/** Languages where string methods only compile regex when the argument is a regex literal. */
private val JS_FAMILY = setOf(Language.JAVASCRIPT, Language.TYPESCRIPT, Language.VUE)

/** JVM languages where String.split() has a single-char fast path that skips regex compilation. */
private val JVM_FAMILY = setOf(Language.JAVA, Language.KOTLIN, Language.GROOVY)

/** Regex metacharacters — single-char arguments using these still compile a regex in the JDK. */
private const val REGEX_METACHARACTERS = ".\$|()[{^?*+\\"

@Suppress("ReturnCount")
private fun isRegexRecompilationCall(
    call: FunctionCall,
    enclosingFn: FunctionDecl?,
    language: Language?,
    regexMethods: Set<String>,
): Boolean {
    if (call.name !in regexMethods || call.qualifiedTarget == null) return false
    // In JS/TS, split/replace/match with a plain string argument do NOT compile regex.
    if (language in JS_FAMILY && hasStringLiteralFirstArg(call)) return false
    // JDK fast path: String.split() with a single non-metachar argument skips regex compilation.
    if (language in JVM_FAMILY && call.name == "split" && hasSingleCharNonRegexArg(call)) return false
    // On JVM, Map.replaceAll(BiFunction) is not a regex method — skip when target is a Map/Set type
    if (language in JVM_FAMILY && call.name == "replaceAll" && isMapTarget(call, enclosingFn)) return false
    // Predicate.matches(), Pattern.matches(), Matcher.matches() — not String regex operations
    if (language in JVM_FAMILY && call.name == "matches" && isNonRegexMatchesTarget(call, enclosingFn)) return false
    return true
}

/** Returns true if the call target is a Map type (by declared type or name heuristic). */
private fun isMapTarget(
    call: FunctionCall,
    enclosingFn: FunctionDecl?,
): Boolean {
    val target = call.qualifiedTarget ?: return false
    // Type-aware: check if the target variable is declared as a Map/Set type
    if (enclosingFn != null && enclosingFn.hasO1Type(target)) return true
    // Name heuristic fallback for cases without type info
    val lower = target.lowercase()
    return MAP_TARGET_NAMES.any { lower.endsWith(it) || lower == it }
}

private val MAP_TARGET_NAMES: Set<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT.allExtraSection("non-list-targets-suffixes")
}

/** Returns true if the call target is a Predicate/Pattern/Matcher type whose matches() is not regex. */
private fun isNonRegexMatchesTarget(
    call: FunctionCall,
    enclosingFn: FunctionDecl?,
): Boolean {
    val target = call.qualifiedTarget ?: return false
    // Type-aware: check parameter/variable type declarations
    if (enclosingFn != null) {
        val paramType = enclosingFn.parameters.find { it.name == target }?.typeName
        if (paramType != null && NON_REGEX_MATCHES_TYPES.any { paramType.contains(it) }) return true
        val varType = enclosingFn.findDescendants<VariableDecl>().find { it.name == target }?.typeName
        if (varType != null && NON_REGEX_MATCHES_TYPES.any { varType.contains(it) }) return true
    }
    // Name heuristic: variable names like "predicate", "matcher", "pattern"
    val lower = target.lowercase()
    return NON_REGEX_MATCHES_NAME_HINTS.any { lower.contains(it) }
}

private val NON_REGEX_MATCHES_TYPES: Set<String> by lazy {
    LanguageSemanticsRegistry.DEFAULT.allExtraSection("non-regex-matches-targets")
}

private val NON_REGEX_MATCHES_NAME_HINTS = setOf("predicate", "matcher")

/**
 * Returns true if the first argument is a string literal (quoted with ' or ").
 * In the IR, arguments are [GenericNode] with the source text as nodeType.
 */
private fun hasStringLiteralFirstArg(call: FunctionCall): Boolean {
    val text = stringLiteralContent(call) ?: return false
    return text.isNotEmpty()
}

/**
 * Returns true if the first argument is a single-character string literal that is NOT
 * a regex metacharacter. The JDK's String.split() fast path avoids Pattern.compile()
 * for these cases (e.g. split(","), split("_"), split(" ")).
 */
private fun hasSingleCharNonRegexArg(call: FunctionCall): Boolean {
    val content = stringLiteralContent(call) ?: return false
    return content.length == 1 && content[0] !in REGEX_METACHARACTERS
}

/**
 * Extracts the unquoted content of a string literal first argument, or null if
 * the first argument is not a recognizable string literal.
 */
private fun stringLiteralContent(call: FunctionCall): String? {
    val firstArg = call.arguments.firstOrNull() ?: return null
    if (firstArg !is GenericNode) return null
    val text = firstArg.nodeType.trim()
    return when {
        text.length >= 2 && text.startsWith("\"") && text.endsWith("\"") -> text.substring(1, text.length - 1)
        text.length >= 2 && text.startsWith("'") && text.endsWith("'") -> text.substring(1, text.length - 1)
        text.length >= 2 && text.startsWith("`") && text.endsWith("`") -> text.substring(1, text.length - 1)
        else -> null
    }
}
