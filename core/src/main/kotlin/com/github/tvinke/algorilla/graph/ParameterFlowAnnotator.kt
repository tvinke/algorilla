package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FlowTarget
import com.github.tvinke.algorilla.model.FunctionCall
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.IRNode
import com.github.tvinke.algorilla.model.LookupCall
import com.github.tvinke.algorilla.model.LoopNode
import com.github.tvinke.algorilla.model.ParameterFlow
import com.github.tvinke.algorilla.model.VariableDecl
import com.github.tvinke.algorilla.util.findDescendants
import com.github.tvinke.algorilla.util.referencesName
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Pass 2.75: annotates each [FunctionDecl] with parameter-flow information.
 *
 * For each parameter, determines which operations in the function body consume
 * the parameter's value: loop iteration, method call receiver, function argument.
 *
 * Uses heuristic name-matching against IR node fields (iteratedVariable,
 * targetVariable, qualifiedTarget). Also tracks single-hop variable aliasing:
 * when a local variable is initialized from a parameter, references to that
 * variable count as references to the parameter.
 */
public class ParameterFlowAnnotator(
    @Suppress("UnusedPrivateProperty") // Reserved for cross-function flow resolution
    private val symbolTable: SymbolTable,
) {
    /**
     * Annotates all [FunctionDecl] nodes in the given IR trees with parameter flows.
     */
    public fun annotate(irTrees: Map<String, FileRoot>) {
        var annotated = 0
        for ((_, fileRoot) in irTrees) {
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                if (fn.parameters.isEmpty()) continue
                fn.parameterFlows = computeFlows(fn)
                if (fn.parameterFlows.isNotEmpty()) annotated++
            }
        }
        logger.info { "Pass 2.75 complete: $annotated functions with parameter flows" }
    }

    internal fun computeFlows(fn: FunctionDecl): List<ParameterFlow> {
        val flows = mutableListOf<ParameterFlow>()
        for ((index, param) in fn.parameters.withIndex()) {
            val flowSet = computeFlowsForParam(fn, param.name)
            if (flowSet.isNotEmpty()) {
                flows.add(ParameterFlow(paramIndex = index, paramName = param.name, flowsInto = flowSet))
            }
        }
        return flows
    }

    private fun computeFlowsForParam(
        fn: FunctionDecl,
        paramName: String,
    ): Set<FlowTarget> {
        // Build the flow name set: starts with the parameter name,
        // expanded by single-hop variable aliasing
        val flowNames = mutableSetOf(paramName)
        for (varDecl in fn.findDescendants<VariableDecl>()) {
            if (initializerReferencesAny(varDecl, flowNames)) {
                flowNames.add(varDecl.name)
            }
        }

        val targets = mutableSetOf<FlowTarget>()
        collectFlowTargets(fn.children, flowNames, targets)
        return targets
    }

    /**
     * Returns true if the variable's initializer or children reference any name in [names].
     */
    private fun initializerReferencesAny(
        varDecl: VariableDecl,
        names: Set<String>,
    ): Boolean {
        // Check the initializer tree for any reference to a tracked name
        for (child in varDecl.children) {
            if (names.any { child.referencesName(it) }) return true
            if (descendantReferencesAny(child, names)) return true
        }
        return false
    }

    private fun descendantReferencesAny(
        node: IRNode,
        names: Set<String>,
    ): Boolean {
        for (child in node.children) {
            if (names.any { child.referencesName(it) }) return true
            if (descendantReferencesAny(child, names)) return true
        }
        return false
    }

    private fun collectFlowTargets(
        nodes: List<IRNode>,
        flowNames: Set<String>,
        targets: MutableSet<FlowTarget>,
    ) {
        for (node in nodes) {
            when {
                node is LoopNode && node.iteratedVariable in flowNames -> {
                    targets.add(FlowTarget.LoopIteration(node.location))
                }
                node is LookupCall && node.targetVariable in flowNames -> {
                    targets.add(FlowTarget.MethodCallReceiver(node.kind.label, node.location))
                }
                node is FunctionCall && node.qualifiedTarget in flowNames -> {
                    targets.add(FlowTarget.MethodCallReceiver(node.name, node.location))
                }
                node is FunctionCall && hasArgumentReferencingAny(node, flowNames) -> {
                    targets.add(FlowTarget.FunctionArgument(node.name, node.location))
                }
            }
            collectFlowTargets(node.children, flowNames, targets)
        }
    }

    /**
     * Checks if any argument expression in the function call references a tracked name.
     */
    private fun hasArgumentReferencingAny(
        call: FunctionCall,
        names: Set<String>,
    ): Boolean {
        for (arg in call.arguments) {
            if (names.any { arg.referencesName(it) }) return true
            if (descendantReferencesAny(arg, names)) return true
        }
        return false
    }
}
