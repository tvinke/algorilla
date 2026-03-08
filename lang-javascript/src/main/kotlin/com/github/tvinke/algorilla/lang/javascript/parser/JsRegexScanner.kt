package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SortCall
import com.github.tvinke.algorilla.model.SortKind
import com.github.tvinke.algorilla.model.SourceLocation

/**
 * Lightweight regex-based scanner that extracts IR nodes from JavaScript/TypeScript source.
 * Tracks brace depth to build a proper tree structure so that rules can detect
 * patterns like lookups inside loops, or heavy objects inside functions.
 */
internal class JsRegexScanner(
    private val filePath: String,
) {
    internal fun scan(source: String): List<IRNode> {
        val lines = source.lines()
        val scopeStack = ArrayDeque<ScopeFrame>()
        scopeStack.addLast(ScopeFrame(null, 0, mutableListOf()))

        for ((index, rawLine) in lines.withIndex()) {
            val lineNum = index + 1
            val line = stripComments(rawLine)

            // Detect structural nodes that open a new scope
            val structuralNode = detectStructuralNode(line, lineNum)
            if (structuralNode != null) {
                val openBraces = countChar(line, '{')
                val closeBraces = countChar(line, '}')
                val frame = ScopeFrame(structuralNode, openBraces - closeBraces, mutableListOf())
                scopeStack.last().children.add(frame)
                if (openBraces > closeBraces) {
                    scopeStack.addLast(frame)
                }
                // Also scan for leaf nodes on the same line
                scanLeafNodes(line, lineNum, frame.children)
            } else {
                // Scan for leaf nodes (lookups, sorts, object creation, generic calls)
                scanLeafNodes(line, lineNum, scopeStack.last().children)

                // Track brace depth changes
                val openBraces = countChar(line, '{')
                val closeBraces = countChar(line, '}')
                val delta = openBraces - closeBraces
                if (delta < 0) {
                    // Closing braces — pop scopes
                    var remaining = -delta
                    while (remaining > 0 && scopeStack.size > 1) {
                        val top = scopeStack.last()
                        if (top.unclosedBraces <= remaining) {
                            remaining -= top.unclosedBraces
                            scopeStack.removeLast()
                        } else {
                            top.unclosedBraces -= remaining
                            remaining = 0
                        }
                    }
                } else if (delta > 0 && scopeStack.size > 1) {
                    scopeStack.last().unclosedBraces += delta
                }
            }
        }

        return buildTree(scopeStack.first())
    }

    private fun detectStructuralNode(line: String, lineNum: Int): IRNode? {
        // Function declarations
        for (pattern in FUNCTION_PATTERNS) {
            val match = pattern.find(line)
            if (match != null) {
                val name = match.groupValues[1]
                if (name.isNotBlank()) {
                    return FunctionDecl(
                        name = name, qualifiedName = name,
                        parameters = emptyList(),
                        location = SourceLocation(filePath, lineNum, match.range.first + 1),
                        children = emptyList(),
                    )
                }
            }
        }
        // Object method shorthand: methodName(...) { or methodName: function
        OBJECT_METHOD_PATTERN.find(line)?.let { match ->
            val name = match.groupValues[1]
            if (name.isNotBlank() && name !in KEYWORDS) {
                return FunctionDecl(
                    name = name, qualifiedName = name,
                    parameters = emptyList(),
                    location = SourceLocation(filePath, lineNum, match.range.first + 1),
                    children = emptyList(),
                )
            }
        }
        // Loops
        for (pattern in LOOP_PATTERNS) {
            val match = pattern.find(line)
            if (match != null) {
                return LoopNode(
                    kind = classifyLoopText(match.value),
                    iteratedVariable = extractIteratedVar(line),
                    location = SourceLocation(filePath, lineNum, match.range.first + 1),
                    children = emptyList(),
                )
            }
        }
        // Higher-order iteration methods that open a callback scope
        ITERATION_CALL_PATTERN.find(line)?.let { match ->
            val methodName = match.groupValues[1]
            return LoopNode(
                kind = LoopKind.HIGHER_ORDER,
                iteratedVariable = extractChainTarget(line, match.range.first),
                location = SourceLocation(filePath, lineNum, match.range.first + 1),
                children = emptyList(),
            )
        }
        return null
    }

    private fun scanLeafNodes(line: String, lineNum: Int, target: MutableList<Any>) {
        for (match in CHAINED_CALL_PATTERN.findAll(line)) {
            val methodName = match.groupValues[1]
            val loc = SourceLocation(filePath, lineNum, match.range.first + 1)
            val targetVar = extractChainTarget(line, match.range.first)
            val node = classifyJsCall(methodName, targetVar, loc)
            if (node != null) target.add(LeafNode(node))
        }
        for (match in OBJECT_CREATION_PATTERN.findAll(line)) {
            val typeName = match.groupValues[1]
            target.add(
                LeafNode(
                    ObjectCreation(
                        typeName = typeName,
                        location = SourceLocation(filePath, lineNum, match.range.first + 1),
                        children = emptyList(),
                    ),
                ),
            )
        }
        // Generic function calls (for cross-method analysis, serialization detection, etc.)
        for (match in GENERIC_CALL_PATTERN.findAll(line)) {
            val target2 = match.groupValues[1]
            val name = match.groupValues[2]
            if (name !in STRUCTURAL_NAMES && name !in LOOKUP_NAMES) {
                val loc = SourceLocation(filePath, lineNum, match.range.first + 1)
                target.add(
                    LeafNode(
                        FunctionCall(
                            name = name,
                            qualifiedTarget = target2.ifBlank { null },
                            location = loc,
                            children = emptyList(),
                        ),
                    ),
                )
            }
        }
    }

    private fun buildTree(root: ScopeFrame): List<IRNode> =
        root.children.map { buildNode(it) }

    private fun buildNode(item: Any): IRNode =
        when (item) {
            is ScopeFrame -> {
                val children = item.children.map { buildNode(it) }
                when (val node = item.node) {
                    is FunctionDecl -> node.copy(children = children)
                    is LoopNode -> node.copy(children = children)
                    else -> node ?: error("null structural node")
                }
            }
            is LeafNode -> item.node
            else -> error("unexpected item type")
        }
}

private data class ScopeFrame(
    val node: IRNode?,
    var unclosedBraces: Int,
    val children: MutableList<Any>,
)

private data class LeafNode(val node: IRNode)

private val FUNCTION_PATTERNS = listOf(
    Regex("""function\s+(\w+)"""),
    Regex("""(?:const|let|var)\s+(\w+)\s*=\s*(?:async\s+)?function"""),
    Regex("""(?:const|let|var)\s+(\w+)\s*=\s*(?:async\s+)?\([^)]*\)\s*=>"""),
    Regex("""(?:export\s+(?:default\s+)?)?(?:const|let|var)\s+(\w+)\s*=\s*(?:async\s+)?\w+\s*=>\s*\{"""),
)

private val OBJECT_METHOD_PATTERN = Regex("""^\s*(\w+)\s*(?:\([^)]*\))?\s*(?::\s*function)?\s*\(""")

private val LOOP_PATTERNS = listOf(
    Regex("""\bfor\s*\("""),
    Regex("""\bwhile\s*\("""),
)

private val ITERATION_CALL_PATTERN = Regex("""\.(forEach|map|flatMap|reduce|reduceRight)\s*\(""")

private val CHAINED_CALL_PATTERN = Regex("""\.(contains|includes|indexOf|findIndex|find|findLast|filter|some|every|sort|toSorted|first|last|concat)\s*\(""")

private val OBJECT_CREATION_PATTERN = Regex("""\bnew\s+(\w+)\s*\(""")

private val GENERIC_CALL_PATTERN = Regex("""(?:(\w+(?:\.\w+)*)\.)?(\w+)\s*\(""")

private val LOOKUP_NAMES = setOf("contains", "includes", "indexOf", "findIndex", "find", "findLast", "filter", "some", "every", "sort", "toSorted", "first", "last", "concat")
private val STRUCTURAL_NAMES = setOf("function", "if", "else", "for", "while", "switch", "catch", "forEach", "map", "flatMap", "reduce", "reduceRight", "require", "import")
private val KEYWORDS = setOf("if", "else", "for", "while", "switch", "case", "catch", "try", "return", "new", "do", "class", "export", "import", "default", "const", "let", "var", "async", "await")

private fun classifyLoopText(text: String): LoopKind =
    when {
        text.contains("while") -> LoopKind.WHILE
        text.contains("for") && text.contains(" of ") -> LoopKind.FOR_EACH
        text.contains("for") && text.contains(" in ") -> LoopKind.FOR_EACH
        else -> LoopKind.FOR
    }

private fun extractIteratedVar(line: String): String? {
    val ofMatch = Regex("""\bfor\s*\([^)]*\bof\s+(\w+)""").find(line)
    if (ofMatch != null) return ofMatch.groupValues[1]
    val inMatch = Regex("""\bfor\s*\([^)]*\bin\s+(\w+)""").find(line)
    return inMatch?.groupValues?.get(1)
}

private fun extractChainTarget(line: String, callPos: Int): String? {
    val prefix = line.substring(0, callPos.coerceAtMost(line.length))
    val match = Regex("""(\w+)\s*$""").find(prefix)
    return match?.groupValues?.get(1)
}

private fun classifyJsCall(methodName: String, targetVar: String?, loc: SourceLocation): IRNode? =
    when (methodName) {
        "contains" -> LookupCall(LookupKind.CONTAINS, targetVar, false, loc, emptyList())
        "includes" -> LookupCall(LookupKind.INCLUDES, targetVar, false, loc, emptyList())
        "indexOf", "findIndex" -> LookupCall(LookupKind.INDEX_OF, targetVar, false, loc, emptyList())
        "find", "findLast" -> LookupCall(LookupKind.FIND, targetVar, false, loc, emptyList())
        "filter" -> LookupCall(LookupKind.FILTER, targetVar, false, loc, emptyList())
        "some" -> LookupCall(LookupKind.SOME, targetVar, false, loc, emptyList())
        "every" -> LookupCall(LookupKind.ANY, targetVar, false, loc, emptyList())
        "sort", "toSorted" -> SortCall(SortKind.SORT, false, null, loc, emptyList())
        "first" -> CollectionAccess(AccessKind.FIRST, loc, emptyList())
        "last" -> CollectionAccess(AccessKind.LAST, loc, emptyList())
        "concat" -> null // handled by InLoopCollectionBuildingRule via FunctionCall
        else -> null
    }

private fun stripComments(line: String): String {
    val singleLine = line.indexOf("//")
    return if (singleLine >= 0) line.substring(0, singleLine) else line
}

private fun countChar(line: String, ch: Char): Int = line.count { it == ch }
