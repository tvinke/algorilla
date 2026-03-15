package com.github.tvinke.algorilla.lang.javascript.parser

import com.github.tvinke.algorilla.model.AccessKind
import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.CollectionAccess
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
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
import com.github.tvinke.algorilla.model.VariableDecl
import org.treesitter.TSNode

/**
 * Walks a tree-sitter JavaScript/TypeScript CST and produces IR nodes.
 * Replaces the regex-based [JsRegexScanner] with a proper parse tree walk,
 * enabling BranchNode, VariableDecl, and correct nesting of children.
 */
@Suppress("TooManyFunctions", "LargeClass")
internal class JsTreeSitterVisitor(
    private val filePath: String,
    private val source: String,
) {
    private var enclosingClass: String? = null

    @Suppress("CyclomaticComplexMethod")
    fun visit(node: TSNode): List<IRNode> {
        if (node.isNull) return emptyList()
        return when (node.type) {
            "function_declaration", "generator_function_declaration" -> visitFunctionDeclaration(node)
            "function_expression", "generator_function" -> visitFunctionExpression(node)
            "arrow_function" -> visitArrowFunction(node)
            "method_definition" -> visitMethodDefinition(node)
            "for_statement" -> visitForStatement(node)
            "for_in_statement", "for_of_statement" -> visitForInStatement(node, LoopKind.FOR_EACH)
            "while_statement", "do_statement" -> visitWhileStatement(node)
            "if_statement" -> visitIfStatement(node)
            "switch_statement" -> visitSwitchStatement(node)
            "variable_declaration", "lexical_declaration" -> visitVariableDeclaration(node)
            "call_expression" -> visitCallExpression(node)
            "new_expression" -> visitNewExpression(node)
            "class_declaration" -> visitClassDeclaration(node)
            "ternary_expression" -> visitTernaryExpression(node)
            "member_expression" -> emptyList()
            in TRANSPARENT_TYPES -> visitChildren(node)
            else -> emptyList()
        }
    }

    private fun visitChildren(node: TSNode): List<IRNode> {
        val result = mutableListOf<IRNode>()
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull) {
                result.addAll(visit(child))
            }
        }
        return result
    }

    private fun visitFunctionDeclaration(node: TSNode): List<IRNode> {
        val nameNode = node.getChildByFieldName("name")
        val name = nodeText(nameNode)
        val body = node.getChildByFieldName("body")
        val bodyChildren = if (!body.isNull) visit(body) else emptyList()
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = extractParameters(node),
                declaringClass = enclosingClass,
                location = locationOf(node),
                children = bodyChildren,
            ),
        )
    }

    private fun visitFunctionExpression(node: TSNode): List<IRNode> {
        val nameNode = node.getChildByFieldName("name")
        val name = if (!nameNode.isNull) nodeText(nameNode) else "<anonymous>"
        val body = node.getChildByFieldName("body")
        val bodyChildren = if (!body.isNull) visit(body) else emptyList()

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = name,
                parameters = extractParameters(node),
                location = locationOf(node),
                children = bodyChildren,
            ),
        )
    }

    private fun visitArrowFunction(node: TSNode): List<IRNode> {
        val body = node.getChildByFieldName("body")
        return if (!body.isNull) visit(body) else emptyList()
    }

    private fun visitMethodDefinition(node: TSNode): List<IRNode> {
        val nameNode = node.getChildByFieldName("name")
        val name = nodeText(nameNode)
        val body = node.getChildByFieldName("body")
        val bodyChildren = if (!body.isNull) visit(body) else emptyList()
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name
        val isCtor = name == "constructor"

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = extractParameters(node),
                isConstructor = isCtor,
                declaringClass = enclosingClass,
                location = locationOf(node),
                children = bodyChildren,
            ),
        )
    }

    private fun visitForStatement(node: TSNode): List<IRNode> {
        val body = node.getChildByFieldName("body")
        val bodyChildren = if (!body.isNull) visit(body) else emptyList()
        return listOf(LoopNode(LoopKind.FOR, null, locationOf(node), bodyChildren))
    }

    private fun visitForInStatement(
        node: TSNode,
        kind: LoopKind,
    ): List<IRNode> {
        val right = node.getChildByFieldName("right")
        val iterVar = if (!right.isNull) jsExtractVariableName(nodeText(right)) else null
        val body = node.getChildByFieldName("body")
        val bodyChildren = if (!body.isNull) visit(body) else emptyList()
        return listOf(LoopNode(kind, iterVar, locationOf(node), bodyChildren))
    }

    private fun visitWhileStatement(node: TSNode): List<IRNode> {
        val body = node.getChildByFieldName("body")
        val bodyChildren = if (!body.isNull) visit(body) else emptyList()
        return listOf(LoopNode(LoopKind.WHILE, null, locationOf(node), bodyChildren))
    }

    private fun visitIfStatement(node: TSNode): List<IRNode> {
        val condChildren = visitField(node, "condition")
        val thenBranch = visitField(node, "consequence")
        val altNode = node.getChildByFieldName("alternative")
        if (!altNode.isNull) {
            val elseBranch = visit(altNode)
            return condChildren + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(node)))
        }
        return condChildren + thenBranch
    }

    private fun visitSwitchStatement(node: TSNode): List<IRNode> {
        val condChildren = visitField(node, "value")
        val bodyNode = node.getChildByFieldName("body")
        if (bodyNode.isNull) return condChildren
        val branches = mutableListOf<List<IRNode>>()
        for (i in 0 until bodyNode.namedChildCount) {
            val caseNode = bodyNode.getNamedChild(i)
            if (!caseNode.isNull) {
                branches.add(visitChildren(caseNode))
            }
        }
        if (branches.size > 1) {
            return condChildren + listOf(BranchNode(branches, locationOf(node)))
        }
        return condChildren + branches.flatten()
    }

    private fun visitTernaryExpression(node: TSNode): List<IRNode> {
        val condChildren = visitField(node, "condition")
        val thenBranch = visitField(node, "consequence")
        val elseBranch = visitField(node, "alternative")
        return condChildren + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(node)))
    }

    private fun visitVariableDeclaration(node: TSNode): List<IRNode> {
        val results = mutableListOf<IRNode>()
        for (i in 0 until node.namedChildCount) {
            val declarator = node.getNamedChild(i)
            if (!declarator.isNull && declarator.type == "variable_declarator") {
                results.addAll(visitVariableDeclarator(declarator))
            }
        }
        return results
    }

    private fun visitVariableDeclarator(declarator: TSNode): List<IRNode> {
        val nameNode = declarator.getChildByFieldName("name")
        val varName = nodeText(nameNode)
        val valueNode = declarator.getChildByFieldName("value")
        // Arrow function or function expression assigned to a variable → FunctionDecl
        if (!valueNode.isNull && valueNode.type in FUNCTION_VALUE_TYPES) {
            val body = valueNode.getChildByFieldName("body")
            val bodyChildren = if (!body.isNull) visit(body) else emptyList()
            return listOf(
                FunctionDecl(
                    name = varName,
                    qualifiedName = varName,
                    parameters = extractParameters(valueNode),
                    location = locationOf(declarator),
                    children = bodyChildren,
                ),
            )
        }
        val initChildren = if (!valueNode.isNull) visit(valueNode) else emptyList()
        val typeName = inferVariableType(declarator, valueNode)
        return listOf(
            VariableDecl(
                name = varName,
                typeName = typeName,
                location = locationOf(declarator),
                children = initChildren,
            ),
        )
    }

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private fun visitCallExpression(node: TSNode): List<IRNode> {
        val fnNode = node.getChildByFieldName("function")
        if (fnNode.isNull) return visitChildren(node)

        // Chained call: target.method(args)
        if (fnNode.type == "member_expression") {
            return visitChainedCall(fnNode, node)
        }

        // Standalone call: method(args)
        val name = nodeText(fnNode)
        val loc = locationOf(node)
        val argNodes = visitArgs(node)

        // Iteration higher-order calls used standalone
        if (name in JS_ITERATION_METHODS) {
            return listOf(LoopNode(LoopKind.HIGHER_ORDER, null, loc, argNodes))
        }

        val lookupKind = jsAllLookupKindFor(name)
        if (lookupKind != null) {
            return listOf(LookupCall(lookupKind, null, false, loc, argNodes))
        }

        return listOf(
            FunctionCall(
                name = name,
                qualifiedTarget = null,
                arguments = argNodes,
                location = loc,
                children = argNodes,
            ),
        )
    }

    private fun visitChainedCall(
        memberNode: TSNode,
        callNode: TSNode,
    ): List<IRNode> {
        val objectNode = memberNode.getChildByFieldName("object")
        val propertyNode = memberNode.getChildByFieldName("property")
        val methodName = nodeText(propertyNode)
        val targetText = nodeText(objectNode)
        val targetVar = jsExtractVariableName(targetText)
        val loc = locationOf(callNode)
        val argNodes = visitArgs(callNode)
        val targetChildren = if (!objectNode.isNull) visit(objectNode) else emptyList()
        val node = classifyChainedJsCall(methodName, targetText, targetVar, argNodes, loc)
        return targetChildren + listOf(node)
    }

    private fun visitNewExpression(node: TSNode): List<IRNode> {
        val ctorNode = node.getChildByFieldName("constructor")
        val typeName = nodeText(ctorNode)
        val argNodes = visitArgs(node)
        return listOf(ObjectCreation(typeName = typeName, location = locationOf(node), children = argNodes))
    }

    private fun visitClassDeclaration(node: TSNode): List<IRNode> {
        val prev = enclosingClass
        val nameNode = node.getChildByFieldName("name")
        enclosingClass = if (!nameNode.isNull) nodeText(nameNode) else null
        val result = visitChildren(node)
        enclosingClass = prev
        return result
    }

    private fun visitField(
        node: TSNode,
        fieldName: String,
    ): List<IRNode> {
        val child = node.getChildByFieldName(fieldName)
        return if (!child.isNull) visit(child) else emptyList()
    }

    private fun visitArgs(callNode: TSNode): List<IRNode> {
        val argsNode = callNode.getChildByFieldName("arguments")
        if (argsNode.isNull) return emptyList()
        val result = mutableListOf<IRNode>()
        for (i in 0 until argsNode.namedChildCount) {
            val arg = argsNode.getNamedChild(i)
            if (!arg.isNull) {
                val visited = visit(arg)
                if (visited.isEmpty()) {
                    result.add(GenericNode(nodeText(arg), locationOf(arg), emptyList()))
                } else {
                    result.addAll(visited)
                }
            }
        }
        return result
    }

    private fun extractParameters(funcNode: TSNode): List<Parameter> {
        val paramsNode = funcNode.getChildByFieldName("parameters") ?: funcNode.getChildByFieldName("parameter")
        if (paramsNode == null || paramsNode.isNull) return emptyList()

        // Single identifier param (arrow functions like x => ...)
        if (paramsNode.type == "identifier") {
            return listOf(Parameter(nodeText(paramsNode), null))
        }

        val params = mutableListOf<Parameter>()
        for (i in 0 until paramsNode.namedChildCount) {
            val paramChild = paramsNode.getNamedChild(i)
            if (!paramChild.isNull) {
                extractSingleParameter(paramChild)?.let { params.add(it) }
            }
        }
        return params
    }

    private fun extractSingleParameter(paramChild: TSNode): Parameter? =
        when (paramChild.type) {
            "identifier" -> Parameter(nodeText(paramChild), null)
            "required_parameter", "optional_parameter" -> extractTypedParameter(paramChild)
            "assignment_pattern" -> {
                val left = paramChild.getChildByFieldName("left")
                if (left != null && !left.isNull) Parameter(nodeText(left), null) else null
            }
            "rest_pattern" -> extractRestParameter(paramChild)
            else -> nodeText(paramChild).trim().takeIf { it.isNotEmpty() }?.let { Parameter(it, null) }
        }

    private fun extractTypedParameter(paramChild: TSNode): Parameter {
        val patternNode = paramChild.getChildByFieldName("pattern")
        val name = if (patternNode != null && !patternNode.isNull) nodeText(patternNode) else nodeText(paramChild)
        val typeNode = paramChild.getChildByFieldName("type")
        return Parameter(name, extractTypeName(typeNode))
    }

    private fun extractRestParameter(paramChild: TSNode): Parameter? {
        for (j in 0 until paramChild.namedChildCount) {
            val inner = paramChild.getNamedChild(j)
            if (!inner.isNull && inner.type == "identifier") {
                return Parameter(nodeText(inner), null)
            }
        }
        return null
    }

    /**
     * Infer a variable's type from TypeScript annotations or constructor calls.
     * Examples: `const x: Map<K,V> = ...` → "Map", `const x = new Set()` → "Set"
     */
    private fun inferVariableType(
        declarator: TSNode,
        valueNode: TSNode,
    ): String? {
        // TypeScript type annotation: `const x: Map<string, number> = ...`
        val typeNode = declarator.getChildByFieldName("type")
        val annotated = extractTypeName(typeNode)
        if (annotated != null) return annotated

        // Constructor inference: `const x = new Map()` → "Map"
        if (!valueNode.isNull && valueNode.type == "new_expression") {
            val ctorNode = valueNode.getChildByFieldName("constructor")
            if (ctorNode != null && !ctorNode.isNull) return nodeText(ctorNode)
        }
        return null
    }

    private fun extractTypeName(typeNode: TSNode?): String? {
        if (typeNode == null || typeNode.isNull) return null
        val typeAnnotation = typeNode
        // type_annotation wraps the actual type; drill into it
        return when (typeAnnotation.type) {
            "type_annotation" -> {
                // The actual type is a named child
                for (i in 0 until typeAnnotation.namedChildCount) {
                    val child = typeAnnotation.getNamedChild(i)
                    if (!child.isNull) {
                        return extractTypeText(child)
                    }
                }
                null
            }
            else -> extractTypeText(typeAnnotation)
        }
    }

    private fun extractTypeText(typeNode: TSNode): String? {
        if (typeNode.isNull) return null
        return when (typeNode.type) {
            "generic_type" -> {
                val nameNode = typeNode.getChildByFieldName("name")
                if (nameNode != null && !nameNode.isNull) nodeText(nameNode) else nodeText(typeNode)
            }
            "type_identifier", "predefined_type" -> nodeText(typeNode)
            else -> nodeText(typeNode)
        }
    }

    private fun nodeText(node: TSNode): String {
        if (node.isNull) return ""
        val start = node.startByte.coerceAtMost(source.length)
        val end = node.endByte.coerceAtMost(source.length)
        return source.substring(start, end)
    }

    private fun locationOf(node: TSNode): SourceLocation = SourceLocation(filePath, node.startPoint.row + 1, node.startPoint.column + 1)
}

/** Node types that are transparent — we visit their children without producing IR nodes. */
private val TRANSPARENT_TYPES =
    setOf(
        "program",
        "export_statement",
        "expression_statement",
        "return_statement",
        "throw_statement",
        "try_statement",
        "catch_clause",
        "finally_clause",
        "statement_block",
        "parenthesized_expression",
        "assignment_expression",
        "augmented_assignment_expression",
        "binary_expression",
        "unary_expression",
        "update_expression",
        "sequence_expression",
        "template_string",
        "template_substitution",
        "array",
        "object",
        "pair",
        "spread_element",
        "await_expression",
        "yield_expression",
        "subscript_expression",
        "arguments",
        "class_body",
    )

private val FUNCTION_VALUE_TYPES = setOf("arrow_function", "function_expression", "generator_function")

private val JS_ITERATION_METHODS = setOf("forEach", "map", "flatMap", "reduce", "reduceRight", "removeIf")

private fun jsAllLookupKindFor(methodName: String): LookupKind? =
    when (methodName) {
        "contains", "containsKey", "containsValue" -> LookupKind.CONTAINS
        "indexOf", "lastIndexOf", "findIndex" -> LookupKind.INDEX_OF
        "find", "findLast" -> LookupKind.FIND
        "filter" -> LookupKind.FILTER
        "anyMatch" -> LookupKind.ANY_MATCH
        "allMatch" -> LookupKind.ALL_MATCH
        "noneMatch" -> LookupKind.NONE_MATCH
        "some" -> LookupKind.SOME
        "includes" -> LookupKind.INCLUDES
        "every" -> LookupKind.ANY
        else -> null
    }

private val JS_STRING_SUFFIXES =
    setOf("Name", "name", "Text", "text", "Str", "String", "string", "Line", "line", "Path", "path", "Url", "url")

private fun jsIsStringTarget(targetText: String): Boolean =
    JS_STRING_SUFFIXES.any { targetText.endsWith(it) } ||
        targetText.contains("toString()") ||
        targetText.contains("substring(") ||
        targetText.contains("toLowerCase()") ||
        targetText.contains("toUpperCase()")

private fun jsSortKindFor(methodName: String): SortKind? =
    when (methodName) {
        "sort", "toSorted" -> SortKind.SORT
        "sorted" -> SortKind.SORTED
        "sortBy", "sortedBy" -> SortKind.SORT_BY
        "orderBy" -> SortKind.ORDER_BY
        else -> null
    }

private fun jsAccessKindFor(methodName: String): AccessKind? =
    when (methodName) {
        "first" -> AccessKind.FIRST
        "last" -> AccessKind.LAST
        "findFirst" -> AccessKind.FIND_FIRST
        "findAny" -> AccessKind.FIND_ANY
        else -> null
    }

private fun jsExtractVariableName(expr: String?): String? {
    if (expr == null) return null
    val lastDot = expr.lastIndexOf('.')
    return if (lastDot >= 0) expr.substring(0, lastDot) else expr
}

@Suppress("ReturnCount")
private fun classifyChainedJsCall(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
): IRNode {
    if (methodName in JS_ITERATION_METHODS) {
        return LoopNode(LoopKind.HIGHER_ORDER, targetVar, loc, argNodes)
    }
    jsClassifyAsLookup(methodName, targetText, targetVar, argNodes, loc)?.let { return it }
    jsSortKindFor(methodName)?.let { kind ->
        return SortCall(
            kind = kind,
            hasComparator = argNodes.isNotEmpty(),
            comparatorBody =
                argNodes.ifEmpty {
                    null
                },
            location = loc,
            children = argNodes,
            qualifiedTarget = targetVar,
        )
    }
    jsAccessKindFor(methodName)?.let { kind ->
        return CollectionAccess(kind = kind, location = loc, children = argNodes, qualifiedTarget = targetVar)
    }
    if (methodName == "get" && jsIsLiteralZeroArg(argNodes)) {
        return CollectionAccess(kind = AccessKind.INDEX_ZERO, location = loc, children = argNodes, qualifiedTarget = targetVar)
    }
    return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
}

private fun jsClassifyAsLookup(
    methodName: String,
    targetText: String,
    targetVar: String?,
    argNodes: List<IRNode>,
    loc: SourceLocation,
): IRNode? {
    val kind = jsAllLookupKindFor(methodName) ?: return null
    if (kind.isStringApplicable() && jsIsStringTarget(targetText)) {
        return FunctionCall(name = methodName, qualifiedTarget = targetVar, arguments = argNodes, location = loc, children = argNodes)
    }
    val isO1 = jsIsSmallInlineArray(targetText)
    return LookupCall(kind, targetVar, isO1, loc, argNodes)
}

/**
 * Detects inline array literals like `[a, b, c]` and returns true when the array
 * has at most [SMALL_ARRAY_THRESHOLD] elements. Lookups on such arrays are effectively O(1)
 * and should not be flagged as linear.
 */
private fun jsIsSmallInlineArray(targetText: String): Boolean {
    val trimmed = targetText.trim()
    if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return false
    val inner = trimmed.substring(1, trimmed.length - 1).trim()
    if (inner.isEmpty()) return true // empty array is O(1)
    // Count elements by commas, accounting for nested brackets/parens
    var depth = 0
    var elements = 1
    for (ch in inner) {
        when (ch) {
            '[', '(' -> depth++
            ']', ')' -> depth--
            ',' -> if (depth == 0) elements++
        }
    }
    return elements <= SMALL_ARRAY_THRESHOLD
}

private const val SMALL_ARRAY_THRESHOLD = 8

private fun jsIsLiteralZeroArg(argNodes: List<IRNode>): Boolean =
    argNodes.size == 1 && argNodes[0] is GenericNode && (argNodes[0] as GenericNode).nodeType.trim() == "0"
