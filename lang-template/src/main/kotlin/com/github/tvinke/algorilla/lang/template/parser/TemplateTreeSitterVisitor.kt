package com.github.tvinke.algorilla.lang.template.parser

import com.github.tvinke.algorilla.model.IRNode
import org.treesitter.TSNode

/**
 * Walks a tree-sitter CST for <LANGUAGE> and produces IR nodes.
 *
 * TODO: rename to match your language (e.g. PythonTreeSitterVisitor).
 * TODO: update the package from `lang.template` to `lang.<yourlanguage>`.
 *
 * Start by printing the tree-sitter CST for a small sample file to see which node
 * types your grammar produces. Then map each relevant node type to the correct IR node.
 *
 * See KotlinTreeSitterVisitor for a full working example.
 */
internal class TemplateTreeSitterVisitor(
    private val filePath: String,
    private val source: String,
) {

    fun visit(node: TSNode): List<IRNode> {
        if (node.isNull) return emptyList()
        return when (node.type) {
            // TODO: map your language's CST node types to the appropriate handler methods.
            // The node type strings come from the tree-sitter grammar; print the CST to discover them.
            //
            // "function_definition" -> visitFunctionDeclaration(node)
            // "for_statement"       -> visitForStatement(node)
            // "while_statement"     -> visitWhileStatement(node)
            // "call"                -> visitCallExpression(node)
            // "assignment"          -> visitVariableDeclaration(node)
            // "if_statement"        -> visitIfStatement(node)
            // ...

            else -> visitChildren(node)
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

    // -----------------------------------------------------------------------
    // IR node handlers — implement one per construct your language needs.
    // Each section has a brief note on which rules depend on the node type.
    // -----------------------------------------------------------------------

    // --- LoopNode ---
    // Used by: nested-lookup rule, nested-sort rule, loop-complexity analysis.
    // Map for/while/do-while to LoopNode with the right LoopKind.
    // Higher-order iteration (e.g. .forEach, .map) should use LoopKind.HIGHER_ORDER.
    //
    // TODO: implement visitForStatement, visitWhileStatement, etc.

    // --- FunctionDecl ---
    // Used by: scoping (rules check which function a finding is inside), method-level metrics.
    // Extract name, qualified name, parameters, and declaring class if applicable.
    //
    // TODO: implement visitFunctionDeclaration

    // --- FunctionCall ---
    // Used by: fallback when a call isn't classified as LookupCall, SortCall, or ObjectCreation.
    // Most calls start as FunctionCall and get reclassified by classifyChainedCall/classifyStandaloneCall.
    //
    // TODO: implement visitCallExpression

    // --- LookupCall ---
    // Used by: nested-lookup rule (the core rule — a LookupCall inside a LoopNode is a finding).
    // Calls like list.contains(), map.get(), list.indexOf() produce LookupCall nodes.
    // The isO1 flag distinguishes O(1) lookups (HashSet.contains) from O(n) ones (List.contains).
    // Use classifyChainedCall() from the java parser utils to handle classification.
    //
    // TODO: handle lookup-style calls in visitCallExpression or visitChainedCall

    // --- SortCall ---
    // Used by: nested-sort rule (a SortCall inside a LoopNode is a finding).
    // Calls like list.sort(), Collections.sort(), list.sorted() produce SortCall nodes.
    //
    // TODO: handle sort-style calls

    // --- ObjectCreation ---
    // Used by: data-structure upgrade suggestions (e.g. "use HashSet instead of ArrayList").
    // Constructor calls like `new HashMap<>()` or `HashMap()` produce ObjectCreation nodes.
    //
    // TODO: handle constructor/instantiation calls

    // --- VariableDecl ---
    // Used by: type tracking (knowing that `val x: List<String>` means `x` is a List helps
    // classify later calls like `x.contains()` correctly).
    //
    // TODO: implement visitVariableDeclaration or visitPropertyDeclaration

    // -----------------------------------------------------------------------
    // Utility methods
    // -----------------------------------------------------------------------

    // TODO: add helpers as needed. Common ones from existing parsers:
    //   - nodeText(node): extract source text for a TSNode
    //   - locationOf(node): create a SourceLocation from a TSNode
    //   - findChildByType(node, type): find the first named child with a given type
    //   - findAllChildrenByType(node, type): find all named children with a given type
}
