package com.github.tvinke.algorilla.graph

import com.github.tvinke.algorilla.model.ClassNode
import com.github.tvinke.algorilla.model.FileRoot
import com.github.tvinke.algorilla.model.FunctionDecl
import com.github.tvinke.algorilla.model.PathContext
import com.github.tvinke.algorilla.semantics.LanguageSemanticsRegistry
import com.github.tvinke.algorilla.util.findDescendants

/**
 * Annotates [FunctionDecl] nodes with [PathContext] metadata based on entry-point
 * annotations and bounded call-graph reachability.
 *
 * Foundation layer for future segmented output. Does NOT affect confidence, demotion,
 * detection, or sort order.
 */
public class PathContextAnnotator(
    private val registry: LanguageSemanticsRegistry,
    private val callGraph: CallGraph,
    private val maxHops: Int = 3,
) {
    public fun annotate(irTrees: Map<String, FileRoot>) {
        annotateDirect(irTrees)
        propagate(irTrees)
    }

    private fun annotateDirect(irTrees: Map<String, FileRoot>) {
        for ((_, fileRoot) in irTrees) {
            val language = fileRoot.language
            val requestAnnotations = registry.extraSection(language, "request-handler-annotations")
            val controllerAnnotations = registry.extraSection(language, "controller-class-annotations")
            val lifecycleAnnotations = registry.extraSection(language, "lifecycle-method-annotations")
            val lifecycleInterfaces = registry.extraSection(language, "lifecycle-interfaces")
            val lifecycleCallbacks = buildLifecycleCallbacks(lifecycleInterfaces)

            for (classNode in fileRoot.findDescendants<ClassNode>()) {
                val isControllerClass = classNode.annotations.any { it in controllerAnnotations }
                val isLifecycleClass = classNode.supertypes.any { it in lifecycleInterfaces }
                annotateClassMethods(
                    classNode,
                    isControllerClass,
                    isLifecycleClass,
                    requestAnnotations,
                    lifecycleAnnotations,
                    lifecycleCallbacks,
                )
            }
        }
    }

    private fun annotateClassMethods(
        classNode: ClassNode,
        isControllerClass: Boolean,
        isLifecycleClass: Boolean,
        requestAnnotations: Set<String>,
        lifecycleAnnotations: Set<String>,
        lifecycleCallbacks: Set<String>,
    ) {
        for (fn in classNode.findDescendants<FunctionDecl>()) {
            fn.pathContext =
                detectDirect(
                    fn,
                    isControllerClass,
                    isLifecycleClass,
                    requestAnnotations,
                    lifecycleAnnotations,
                    lifecycleCallbacks,
                )
        }
    }

    @Suppress("ReturnCount")
    private fun detectDirect(
        fn: FunctionDecl,
        isControllerClass: Boolean,
        isLifecycleClass: Boolean,
        requestAnnotations: Set<String>,
        lifecycleAnnotations: Set<String>,
        lifecycleCallbacks: Set<String>,
    ): PathContext? {
        if (fn.annotations.any { it in requestAnnotations } || isControllerClass) return PathContext.REQUEST
        if (fn.annotations.any { it in lifecycleAnnotations }) return PathContext.LIFECYCLE
        if (isLifecycleClass && fn.name in lifecycleCallbacks) return PathContext.LIFECYCLE
        return null
    }

    private fun propagate(irTrees: Map<String, FileRoot>) {
        val resolved = mutableMapOf<String, PathContext>()
        for ((_, fileRoot) in irTrees) {
            for (fn in fileRoot.findDescendants<FunctionDecl>()) {
                val ctx = fn.pathContext ?: continue
                resolved[fn.qualifiedName] = ctx
            }
        }

        repeat(maxHops) {
            val changed = propagateOneRound(irTrees, resolved)
            if (!changed) return
        }
    }

    private fun propagateOneRound(
        irTrees: Map<String, FileRoot>,
        resolved: MutableMap<String, PathContext>,
    ): Boolean {
        var changed = false
        for ((_, fileRoot) in irTrees) {
            fileRoot
                .findDescendants<FunctionDecl>()
                .filter { it.pathContext == null }
                .forEach { fn ->
                    val propagated = resolveFromCallers(fn.qualifiedName, resolved)
                    if (propagated != null) {
                        fn.pathContext = propagated
                        resolved[fn.qualifiedName] = propagated
                        changed = true
                    }
                }
        }
        return changed
    }

    private fun resolveFromCallers(
        qualifiedName: String,
        resolved: Map<String, PathContext>,
    ): PathContext? {
        val callerNames = callGraph.callers(qualifiedName)
        if (callerNames.isEmpty()) return null
        val callerContexts = callerNames.mapNotNull { resolved[it] }.toSet()
        if (callerContexts.isEmpty()) return null
        return if (callerContexts.size == 1) callerContexts.first() else PathContext.MIXED
    }

    private fun buildLifecycleCallbacks(lifecycleInterfaces: Set<String>): Set<String> =
        KNOWN_CALLBACKS.entries
            .filter { it.key in lifecycleInterfaces }
            .map { it.value }
            .toSet()

    private companion object {
        private val KNOWN_CALLBACKS =
            mapOf(
                "InitializingBean" to "afterPropertiesSet",
                "DisposableBean" to "destroy",
                "SmartLifecycle" to "start",
                "SmartInitializingSingleton" to "afterSingletonsInstantiated",
            )
    }
}
