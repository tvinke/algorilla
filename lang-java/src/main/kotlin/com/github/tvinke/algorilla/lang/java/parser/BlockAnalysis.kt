package com.github.tvinke.algorilla.lang.java.parser

import com.github.tvinke.algorilla.model.BranchNode
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.SourceLocation
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Processes block statements with early-return detection. When an if-without-else
 * ends with return/throw, the remaining statements become the implicit else branch
 * inside a [BranchNode].
 *
 * Shared by all language-specific visitors (Java, Kotlin, Groovy) since they all
 * use the same ANTLR Java grammar and [JavaParserBaseVisitor].
 */
public fun processBlockStatements(
    stmts: List<JavaParser.BlockStatementContext>,
    startIdx: Int,
    visitor: JavaParserBaseVisitor<List<IRNode>>,
    locationOf: (ParserRuleContext) -> SourceLocation,
): List<IRNode> {
    val result = mutableListOf<IRNode>()
    var i = startIdx
    while (i < stmts.size) {
        val innerStmt = stmts[i].statement()
        val isIfWithReturn =
            innerStmt != null &&
                innerStmt.IF() != null &&
                innerStmt.ELSE() == null &&
                endsWithReturnOrThrow(innerStmt.statement(0))
        if (isIfWithReturn && i + 1 < stmts.size) {
            val conditionNodes = innerStmt.expression(0)?.let { visitor.visit(it) } ?: emptyList()
            val thenBranch = innerStmt.statement(0)?.let { visitor.visitChildren(it) } ?: emptyList()
            val elseBranch = processBlockStatements(stmts, i + 1, visitor, locationOf)
            result.addAll(conditionNodes)
            result.add(BranchNode(listOf(thenBranch, elseBranch), locationOf(innerStmt)))
            return result
        }
        result.addAll(visitor.visitChildren(stmts[i]))
        i++
    }
    return result
}

/**
 * Checks whether a statement unconditionally ends with a return or throw,
 * making any subsequent code in the same block unreachable (implicit else).
 *
 * Handles: direct return/throw, block-last-statement, try-catch (all paths),
 * and if-else (both branches).
 */
@Suppress("ReturnCount")
public fun endsWithReturnOrThrow(stmt: JavaParser.StatementContext?): Boolean {
    if (stmt == null) return false
    if (stmt.RETURN() != null || stmt.THROW() != null) return true
    // Block-as-statement: check last statement
    if (stmt.blockLabel != null) {
        return endsWithReturnOrThrow(stmt.blockLabel.lastStatement())
    }
    // Try-catch: all paths must return/throw
    if (stmt.TRY() != null) {
        val tryReturns = endsWithReturnOrThrow(stmt.block()?.lastStatement())
        val catches = stmt.catchClause()
        return tryReturns &&
            catches.isNotEmpty() &&
            catches.all { endsWithReturnOrThrow(it.block()?.lastStatement()) }
    }
    // If-else: both branches must return/throw
    if (stmt.IF() != null && stmt.ELSE() != null) {
        return endsWithReturnOrThrow(stmt.statement(0)) && endsWithReturnOrThrow(stmt.statement(1))
    }
    return false
}

private fun JavaParser.BlockContext.lastStatement(): JavaParser.StatementContext? = blockStatement()?.lastOrNull()?.statement()

/**
 * Extracts lambda parameter names from a [JavaParser.LambdaParametersContext].
 * Handles single identifier (`m`), identifier list (`(x, y)`), and typed parameters.
 */
public fun extractLambdaParamNames(ctx: JavaParser.LambdaParametersContext?): List<String> {
    if (ctx == null) return emptyList()
    val names = mutableListOf<String>()
    for (id in ctx.identifier()) {
        names.add(id.text)
    }
    ctx.formalParameterList()?.let { paramList ->
        for (param in paramList.formalParameter()) {
            param.variableDeclaratorId()?.text?.let { names.add(it) }
        }
    }
    return names
}
