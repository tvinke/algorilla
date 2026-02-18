package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.CollectionAccess
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
 * Scans line by line for common patterns: function declarations, loops, method calls,
 * and object creation.
 */
internal class JsRegexScanner(
    private val filePath: String,
) {
    internal fun scan(source: String): List<IRNode> {
        val lines = source.lines()
        val nodes = mutableListOf<IRNode>()
        for ((index, line) in lines.withIndex()) {
            val lineNum = index + 1
            nodes.addAll(scanLine(line, lineNum))
        }
        return nodes
    }

    private fun scanLine(
        line: String,
        lineNum: Int,
    ): List<IRNode> {
        val nodes = mutableListOf<IRNode>()
        nodes.addAll(scanFunctionDecls(line, lineNum))
        nodes.addAll(scanLoops(line, lineNum))
        nodes.addAll(scanChainedCalls(line, lineNum))
        nodes.addAll(scanObjectCreation(line, lineNum))
        return nodes
    }

    private fun scanFunctionDecls(
        line: String,
        lineNum: Int,
    ): List<IRNode> {
        val results = mutableListOf<IRNode>()
        for (pattern in FUNCTION_PATTERNS) {
            for (match in pattern.findAll(line)) {
                val name = match.groupValues[1]
                if (name.isNotBlank()) {
                    results.add(buildFunctionDecl(name, lineNum, match))
                }
            }
        }
        return results
    }

    private fun buildFunctionDecl(
        name: String,
        lineNum: Int,
        match: MatchResult,
    ): FunctionDecl =
        FunctionDecl(
            name = name,
            qualifiedName = name,
            parameters = emptyList<Parameter>(),
            location = SourceLocation(filePath, lineNum, match.range.first + 1),
            children = emptyList(),
        )

    private fun scanLoops(
        line: String,
        lineNum: Int,
    ): List<IRNode> {
        val results = mutableListOf<IRNode>()
        for (pattern in LOOP_PATTERNS) {
            for (match in pattern.findAll(line)) {
                results.add(buildLoopNode(match, lineNum))
            }
        }
        return results
    }

    private fun buildLoopNode(
        match: MatchResult,
        lineNum: Int,
    ): LoopNode {
        val text = match.value
        val kind = classifyLoopText(text)
        return LoopNode(
            kind = kind,
            iteratedVariable = null,
            location = SourceLocation(filePath, lineNum, match.range.first + 1),
            children = emptyList(),
        )
    }

    private fun scanChainedCalls(
        line: String,
        lineNum: Int,
    ): List<IRNode> {
        val results = mutableListOf<IRNode>()
        for (match in CHAINED_CALL_PATTERN.findAll(line)) {
            val methodName = match.groupValues[1]
            val loc = SourceLocation(filePath, lineNum, match.range.first + 1)
            val node = classifyJsChainedCall(methodName, loc)
            if (node != null) results.add(node)
        }
        return results
    }

    private fun scanObjectCreation(
        line: String,
        lineNum: Int,
    ): List<IRNode> {
        val results = mutableListOf<IRNode>()
        for (match in OBJECT_CREATION_PATTERN.findAll(line)) {
            val typeName = match.groupValues[1]
            results.add(
                ObjectCreation(
                    typeName = typeName,
                    location = SourceLocation(filePath, lineNum, match.range.first + 1),
                    children = emptyList(),
                ),
            )
        }
        return results
    }
}

private val FUNCTION_PATTERNS =
    listOf(
        Regex("""function\s+(\w+)"""),
        Regex("""(?:const|let|var)\s+(\w+)\s*=\s*(?:async\s+)?function"""),
        Regex("""(?:const|let|var)\s+(\w+)\s*=\s*(?:async\s+)?\([^)]*\)\s*=>"""),
    )

private val LOOP_PATTERNS =
    listOf(
        Regex("""\bfor\s*\("""),
        Regex("""\bwhile\s*\("""),
        Regex("""\.forEach\s*\("""),
        Regex("""\.each\s*\("""),
    )

private val CHAINED_CALL_PATTERN = Regex("""\.(contains|includes|indexOf|find|findIndex|filter|some|every|sort|first|last)\s*\(""")

private val OBJECT_CREATION_PATTERN = Regex("""\bnew\s+(\w+)\s*\(""")

private fun classifyLoopText(text: String): LoopKind =
    when {
        text.contains(".forEach") || text.contains(".each") -> LoopKind.HIGHER_ORDER
        text.contains("while") -> LoopKind.WHILE
        else -> LoopKind.FOR
    }

@Suppress("CyclomaticComplexMethod")
private fun classifyJsChainedCall(
    methodName: String,
    loc: SourceLocation,
): IRNode? =
    when (methodName) {
        "contains" -> LookupCall(kind = LookupKind.CONTAINS, targetVariable = null, isO1 = false, location = loc, children = emptyList())
        "includes" -> LookupCall(kind = LookupKind.INCLUDES, targetVariable = null, isO1 = false, location = loc, children = emptyList())
        "indexOf", "findIndex" ->
            LookupCall(
                kind = LookupKind.INDEX_OF,
                targetVariable = null,
                isO1 = false,
                location = loc,
                children = emptyList(),
            )
        "find" -> LookupCall(kind = LookupKind.FIND, targetVariable = null, isO1 = false, location = loc, children = emptyList())
        "filter" -> LookupCall(kind = LookupKind.FILTER, targetVariable = null, isO1 = false, location = loc, children = emptyList())
        "some" -> LookupCall(kind = LookupKind.SOME, targetVariable = null, isO1 = false, location = loc, children = emptyList())
        "every" -> LookupCall(kind = LookupKind.ANY, targetVariable = null, isO1 = false, location = loc, children = emptyList())
        "sort" -> SortCall(kind = SortKind.SORT, hasComparator = false, comparatorBody = null, location = loc, children = emptyList())
        "first" -> CollectionAccess(kind = AccessKind.FIRST, location = loc, children = emptyList())
        "last" -> CollectionAccess(kind = AccessKind.LAST, location = loc, children = emptyList())
        else -> null
    }
