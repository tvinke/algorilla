package com.github.tvinke.algorilla.lang.kotlin.parser

import com.github.tvinke.algorilla.lang.java.parser.classifyChainedCall
import com.github.tvinke.algorilla.lang.java.parser.classifyStandaloneCall
import com.github.tvinke.algorilla.lang.java.parser.extractVariableName
import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.GenericNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopKind
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ObjectCreation
import com.github.tvinke.algorilla.model.Parameter
import com.github.tvinke.algorilla.model.SourceLocation
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.semantics.SemanticCategory
import org.treesitter.TSNode

/**
 * Walks a tree-sitter Kotlin CST and produces IR nodes.
 * Replaces the preprocessor + Java ANTLR grammar approach with a proper Kotlin parser.
 */
@Suppress("TooManyFunctions", "LargeClass") // Visitor pattern: one visit method per AST node type
internal class KotlinTreeSitterVisitor(
    private val filePath: String,
    private val source: String,
) {
    private var enclosingClass: String? = null
    private val lambdaParams = mutableSetOf<String>()

    @Suppress("CyclomaticComplexMethod")
    fun visit(node: TSNode): List<IRNode> {
        if (node.isNull) return emptyList()
        return when (node.type) {
            "function_declaration" -> visitFunctionDeclaration(node)
            "for_statement" -> visitForStatement(node)
            "while_statement" -> visitWhileStatement(node)
            "do_while_statement" -> visitDoWhileStatement(node)
            "if_expression" -> visitIfExpression(node)
            "when_expression" -> visitWhenExpression(node)
            "try_expression" -> visitTryExpression(node)
            "call_expression" -> visitCallExpression(node)
            "property_declaration" -> visitPropertyDeclaration(node)
            "class_declaration" -> visitClassDeclaration(node)
            "object_declaration" -> visitObjectDeclaration(node)
            "lambda_literal" -> visitLambdaLiteral(node)
            "navigation_expression" -> emptyList()
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
        val name = findSimpleIdentifier(node)
        val params = extractParameters(node)
        val bodyNode = findChildByType(node, "function_body")
        val bodyChildren = if (bodyNode != null) visitChildren(bodyNode) else emptyList()
        val qName = if (enclosingClass != null) "$enclosingClass.$name" else name

        return listOf(
            FunctionDecl(
                name = name,
                qualifiedName = qName,
                parameters = params,
                declaringClass = enclosingClass,
                location = locationOf(node),
                children = bodyChildren,
            ),
        )
    }

    private fun visitClassDeclaration(node: TSNode): List<IRNode> {
        val prev = enclosingClass
        enclosingClass = findChildByType(node, "type_identifier")?.let { nodeText(it) }
        val body = findChildByType(node, "class_body")
        val result = if (body != null) visitChildren(body) else emptyList()
        enclosingClass = prev
        return result
    }

    private fun visitObjectDeclaration(node: TSNode): List<IRNode> {
        val prev = enclosingClass
        enclosingClass = findChildByType(node, "type_identifier")?.let { nodeText(it) }
        val body = findChildByType(node, "class_body")
        val result = if (body != null) visitChildren(body) else emptyList()
        enclosingClass = prev
        return result
    }

    private fun visitForStatement(node: TSNode): List<IRNode> {
        // for (item in items) { ... }
        // Children: variable_declaration, simple_identifier (collection), control_structure_body
        val collectionName = findCollectionInFor(node)
        val iterVar = extractVariableName(collectionName)
        val body = findChildByType(node, "control_structure_body")
        val bodyChildren = if (body != null) visitChildren(body) else emptyList()
        return listOf(LoopNode(LoopKind.FOR_EACH, iterVar, locationOf(node), bodyChildren))
    }

    private fun visitWhileStatement(node: TSNode): List<IRNode> {
        val body = findChildByType(node, "control_structure_body")
        val bodyChildren = if (body != null) visitChildren(body) else emptyList()
        return listOf(LoopNode(LoopKind.WHILE, null, locationOf(node), bodyChildren))
    }

    private fun visitDoWhileStatement(node: TSNode): List<IRNode> {
        val body = findChildByType(node, "control_structure_body")
        val bodyChildren = if (body != null) visitChildren(body) else emptyList()
        return listOf(LoopNode(LoopKind.WHILE, null, locationOf(node), bodyChildren))
    }

    private fun visitIfExpression(node: TSNode): List<IRNode> {
        // if_expression has: condition, control_structure_body (then), optional control_structure_body (else)
        val bodies = findAllChildrenByType(node, "control_structure_body")
        val condChildren = visitConditionChildren(node)

        if (bodies.size >= 2) {
            val thenBranch = visitChildren(bodies[0])
            val elseBranch = visitChildren(bodies[1])
            return condChildren + listOf(BranchNode(listOf(thenBranch, elseBranch), locationOf(node)))
        }
        val thenBranch = if (bodies.isNotEmpty()) visitChildren(bodies[0]) else emptyList()
        return condChildren + thenBranch
    }

    private fun visitWhenExpression(node: TSNode): List<IRNode> {
        val condChildren = findChildByType(node, "when_subject")?.let { visitChildren(it) } ?: emptyList()
        val entries = findAllChildrenByType(node, "when_entry")
        if (entries.size <= 1) {
            return condChildren + entries.flatMap { visitChildren(it) }
        }
        val branches = entries.map { visitChildren(it) }
        return condChildren + listOf(BranchNode(branches, locationOf(node)))
    }

    private fun visitTryExpression(node: TSNode): List<IRNode> {
        // try_expression: statements (try body), catch_block(s), finally_block
        val tryBody = visitTryBody(node)
        val catches = findAllChildrenByType(node, "catch_block")
        if (catches.isEmpty()) return tryBody

        val branches = mutableListOf(tryBody)
        for (catchBlock in catches) {
            branches.add(visitChildren(catchBlock))
        }
        val finallyBody = findChildByType(node, "finally_block")?.let { visitChildren(it) } ?: emptyList()
        return listOf(BranchNode(branches, locationOf(node))) + finallyBody
    }

    @Suppress("ReturnCount", "CyclomaticComplexMethod")
    private fun visitCallExpression(node: TSNode): List<IRNode> {
        val firstChild = node.getNamedChild(0)
        if (firstChild.isNull) return visitChildren(node)

        // Chained call: navigation_expression + call_suffix
        if (firstChild.type == "navigation_expression") {
            return visitChainedCall(firstChild, node)
        }

        // Standalone call: simple_identifier + call_suffix
        val name = nodeText(firstChild)
        val loc = locationOf(node)
        val callSuffix = findChildByType(node, "call_suffix")
        val argNodes = if (callSuffix != null) visitCallSuffixArgs(callSuffix) else emptyList()

        // Constructor-like call (starts with uppercase)
        if (name.isNotEmpty() && name[0].isUpperCase()) {
            return listOf(ObjectCreation(typeName = name, location = loc, children = argNodes))
        }

        val kotlinLoop = kotlinLoopKindFor(name)
        if (kotlinLoop != null) {
            return listOf(LoopNode(kotlinLoop, null, loc, argNodes))
        }

        return classifyStandaloneCall(name, loc, argNodes, Language.KOTLIN)
    }

    private fun visitChainedCall(
        navExpr: TSNode,
        callNode: TSNode,
    ): List<IRNode> {
        val methodName = extractMethodName(navExpr)
        val objectNode = findObjectInNavigation(navExpr)
        val targetText = if (objectNode != null) nodeText(objectNode) else ""
        val targetVar = extractVariableName(targetText)
        val loc = locationOf(callNode)

        val callSuffix = findChildByType(callNode, "call_suffix")
        val argNodes = if (callSuffix != null) visitCallSuffixArgs(callSuffix) else emptyList()
        val targetChildren = if (objectNode != null) visit(objectNode) else emptyList()

        val kotlinLoop = kotlinLoopKindFor(methodName)
        if (kotlinLoop != null) {
            if (isConstantSizeFactory(targetChildren)) {
                return targetChildren + argNodes
            }
            val loopKind = refineLoopKind(methodName, targetText, kotlinLoop)
            return targetChildren + listOf(LoopNode(loopKind, targetVar, loc, argNodes))
        }

        if (targetVar != null && targetVar in lambdaParams) {
            return targetChildren + listOf(lambdaParamCall(methodName, targetVar, argNodes, loc))
        }

        return targetChildren + classifyAndDeduplicate(methodName, targetText, targetVar, argNodes, loc, targetChildren)
    }

    private fun lambdaParamCall(
        name: String,
        target: String,
        args: List<IRNode>,
        loc: SourceLocation,
    ): IRNode = FunctionCall(name = name, qualifiedTarget = target, arguments = args, location = loc, children = args)

    private fun classifyAndDeduplicate(
        methodName: String,
        targetText: String,
        targetVar: String?,
        argNodes: List<IRNode>,
        loc: SourceLocation,
        targetChildren: List<IRNode>,
    ): List<IRNode> {
        val node = classifyChainedCall(methodName, targetText, targetVar, argNodes, loc, Language.KOTLIN)
        if (node is LookupCall && targetChildren.any { it is LookupCall && it.targetVariable == targetVar }) {
            return argNodes
        }
        if (node is LookupCall && !node.isO1 && isConstantSizeFactory(targetChildren)) {
            return listOf(node.copy(isO1 = true))
        }
        return listOf(node)
    }

    private fun visitPropertyDeclaration(node: TSNode): List<IRNode> {
        val varDecl = findChildByType(node, "variable_declaration")
        val varName = if (varDecl != null) findSimpleIdentifier(varDecl) else return emptyList()
        val typeName = extractPropertyType(varDecl)

        // Find initializer: everything after the variable_declaration that's not binding_pattern_kind
        val initChildren = visitPropertyInitializer(node)

        return listOf(
            VariableDecl(
                name = varName,
                typeName = typeName,
                location = locationOf(node),
                children = initChildren,
            ),
        )
    }

    private fun visitLambdaLiteral(node: TSNode): List<IRNode> {
        val paramsNode = findChildByType(node, "lambda_parameters")
        val paramNames = extractLambdaParamNames(paramsNode)
        lambdaParams.addAll(paramNames)

        val statementsNode = findChildByType(node, "statements")
        val result = if (statementsNode != null) visitChildren(statementsNode) else emptyList()

        lambdaParams.removeAll(paramNames.toSet())
        return result
    }

    // --- Helper methods ---

    private fun visitCallSuffixArgs(callSuffix: TSNode): List<IRNode> {
        val result = mutableListOf<IRNode>()

        // value_arguments: (arg1, arg2, ...)
        val valueArgs = findChildByType(callSuffix, "value_arguments")
        if (valueArgs != null) {
            val args = findAllChildrenByType(valueArgs, "value_argument")
            for (arg in args) {
                val visited = visitChildren(arg)
                if (visited.isEmpty()) {
                    result.add(GenericNode(nodeText(arg), locationOf(arg), emptyList()))
                } else {
                    result.addAll(visited)
                }
            }
        }

        // annotated_lambda: trailing lambda { ... }
        val lambda = findChildByType(callSuffix, "annotated_lambda")
        if (lambda != null) {
            val lambdaLit = findChildByType(lambda, "lambda_literal")
            if (lambdaLit != null) {
                result.addAll(visitLambdaLiteral(lambdaLit))
            }
        }

        return result
    }

    private fun visitConditionChildren(node: TSNode): List<IRNode> {
        // Visit all children that are NOT control_structure_body (i.e. the condition part)
        val result = mutableListOf<IRNode>()
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull && child.type != "control_structure_body") {
                result.addAll(visit(child))
            }
        }
        return result
    }

    private fun visitTryBody(node: TSNode): List<IRNode> {
        // The try body is the `statements` child directly under try_expression
        val result = mutableListOf<IRNode>()
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull && child.type == "statements") {
                result.addAll(visitChildren(child))
                break
            }
        }
        return result
    }

    private fun visitPropertyInitializer(node: TSNode): List<IRNode> {
        val skipTypes = setOf("variable_declaration", "binding_pattern_kind", "modifiers")
        val result = mutableListOf<IRNode>()
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull && child.type !in skipTypes) {
                result.addAll(visit(child))
            }
        }
        return result
    }

    private fun extractParameters(funcNode: TSNode): List<Parameter> {
        val paramsNode = findChildByType(funcNode, "function_value_parameters") ?: return emptyList()
        val params = mutableListOf<Parameter>()
        for (i in 0 until paramsNode.namedChildCount) {
            val paramChild = paramsNode.getNamedChild(i)
            if (!paramChild.isNull && paramChild.type == "parameter") {
                val name = findSimpleIdentifier(paramChild)
                val type = extractParameterType(paramChild)
                params.add(Parameter(name, type))
            }
        }
        return params
    }

    private fun extractParameterType(paramNode: TSNode): String? {
        val userType = findChildByType(paramNode, "user_type") ?: return null
        return nodeText(userType)
    }

    private fun extractPropertyType(varDecl: TSNode): String? {
        val userType = findChildByType(varDecl, "user_type") ?: return null
        return nodeText(userType)
    }

    private fun extractMethodName(navExpr: TSNode): String {
        val suffix = findChildByType(navExpr, "navigation_suffix") ?: return ""
        return findSimpleIdentifier(suffix)
    }

    private fun findObjectInNavigation(navExpr: TSNode): TSNode? {
        // The object is the first named child that isn't navigation_suffix
        for (i in 0 until navExpr.namedChildCount) {
            val child = navExpr.getNamedChild(i)
            if (!child.isNull && child.type != "navigation_suffix") {
                return child
            }
        }
        return null
    }

    private fun findCollectionInFor(node: TSNode): String? {
        // In for_statement: variable_declaration, then the collection expression
        var foundVarDecl = false
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull) {
                if (child.type == "variable_declaration") {
                    foundVarDecl = true
                } else if (foundVarDecl && child.type != "control_structure_body") {
                    return nodeText(child)
                }
            }
        }
        return null
    }

    private fun extractLambdaParamNames(paramsNode: TSNode?): List<String> {
        if (paramsNode == null) return emptyList()
        val names = mutableListOf<String>()
        for (i in 0 until paramsNode.namedChildCount) {
            val child = paramsNode.getNamedChild(i)
            if (!child.isNull && child.type == "variable_declaration") {
                names.add(findSimpleIdentifier(child))
            }
        }
        return names
    }

    private fun findSimpleIdentifier(node: TSNode): String {
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull && child.type == "simple_identifier") {
                return nodeText(child)
            }
        }
        return ""
    }

    private fun findChildByType(
        node: TSNode,
        type: String,
    ): TSNode? {
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull && child.type == type) {
                return child
            }
        }
        return null
    }

    private fun findAllChildrenByType(
        node: TSNode,
        type: String,
    ): List<TSNode> {
        val result = mutableListOf<TSNode>()
        for (i in 0 until node.namedChildCount) {
            val child = node.getNamedChild(i)
            if (!child.isNull && child.type == type) {
                result.add(child)
            }
        }
        return result
    }

    private fun nodeText(node: TSNode): String {
        if (node.isNull) return ""
        val start = node.startByte.coerceAtMost(source.length)
        val end = node.endByte.coerceAtMost(source.length)
        return source.substring(start, end)
    }

    private fun locationOf(node: TSNode): SourceLocation = SourceLocation(filePath, node.startPoint.row + 1, node.startPoint.column + 1)
}

/** Node types that are transparent — visit children without producing IR nodes. */
private val TRANSPARENT_TYPES =
    setOf(
        "source_file",
        "statements",
        "class_body",
        "control_structure_body",
        "parenthesized_expression",
        "assignment",
        "directly_assignable_expression",
        "jump_expression",
        "check_expression",
        "elvis_expression",
        "infix_expression",
        "conjunction_expression",
        "disjunction_expression",
        "equality_expression",
        "comparison_expression",
        "additive_expression",
        "multiplicative_expression",
        "as_expression",
        "prefix_expression",
        "postfix_expression",
        "indexing_expression",
        "spread_expression",
        "when_entry",
        "when_condition",
        "value_argument",
        "value_arguments",
        "string_literal",
        "interpolated_expression",
    )

private val iterationMethods: Set<String> by lazy {
    val registry = LanguageSemanticsRegistry.loadDefaults()
    val methods = mutableSetOf<String>()
    for (name in KNOWN_ITERATION_CANDIDATES) {
        val semantics = registry.classify(Language.KOTLIN, name)
        if (semantics?.category == SemanticCategory.ITERATION) {
            methods.add(name)
        }
    }
    methods.add("removeIf")
    methods
}

private val KNOWN_ITERATION_CANDIDATES =
    setOf(
        "forEach",
        "forEachIndexed",
        "map",
        "flatMap",
        "mapNotNull",
        "fold",
        "reduce",
        "onEach",
        "groupBy",
        "associateBy",
        "associate",
        "partition",
    )

private fun kotlinLoopKindFor(methodName: String): LoopKind? = if (methodName in iterationMethods) LoopKind.HIGHER_ORDER else null

/**
 * Refines the loop kind for chained calls like `parallelStream().forEach()` or `stream().forEach()`.
 * Without this, the Kotlin parser would classify all forEach calls as HIGHER_ORDER,
 * missing the parallel-stream distinction that rules like ParallelPipelineBottleneckRule need.
 */
private fun refineLoopKind(
    methodName: String,
    targetText: String,
    default: LoopKind,
): LoopKind =
    when {
        methodName == "forEach" && targetText.contains(".parallelStream()") -> LoopKind.PARALLEL_STREAM_FOR_EACH
        methodName == "forEach" && targetText.contains(".stream()") -> LoopKind.STREAM_FOR_EACH
        else -> default
    }

/**
 * Kotlin factory calls that produce constant-size collections.
 * Iterating over these is effectively O(1) and should not be flagged.
 */
private val CONSTANT_SIZE_FACTORIES =
    setOf(
        "listOf",
        "setOf",
        "arrayOf",
        "mutableListOf",
        "mutableSetOf",
        "arrayListOf",
        "hashSetOf",
        "linkedSetOf",
        "listOfNotNull",
        "setOfNotNull",
        "emptyList",
        "emptySet",
        "emptyMap",
    )

private const val SMALL_COLLECTION_THRESHOLD = 8

/**
 * Returns true when the target of a chained call is a known constant-size factory
 * (e.g. `listOf(a, b)`) with at most [SMALL_COLLECTION_THRESHOLD] elements.
 */
private fun isConstantSizeFactory(targetChildren: List<IRNode>): Boolean {
    val call = targetChildren.filterIsInstance<FunctionCall>().firstOrNull() ?: return false
    return call.name in CONSTANT_SIZE_FACTORIES && call.arguments.size <= SMALL_COLLECTION_THRESHOLD
}
