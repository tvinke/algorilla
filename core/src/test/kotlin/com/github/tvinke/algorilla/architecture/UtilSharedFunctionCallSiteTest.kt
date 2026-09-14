package com.github.tvinke.algorilla.architecture

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

/**
 * Guards which packages may call the eight shared cross-method `util` functions covered by
 * [UtilKDocContractTest] - verified today: any file under `rules/builtin/`,
 * `engine/AnalysisEngine.kt`, `graph/CallGraphBuilder.kt`, and `util/ParameterFlowQuery.kt`
 * itself (it calls `CrossMethodResolver.resolve()` internally). A caller outside that set
 * means either a new, legitimate use that should be added to the allowlist below, or a
 * cross-method resolution creeping into a place that wasn't reviewed for it - either way,
 * worth a deliberate look rather than silently expanding.
 *
 * Uses ArchUnit, not Konsist: [UtilKDocContractTest] documents that Konsist has no
 * call-graph/reference API (no way to ask "who calls function X" from source text/PSI alone).
 * ArchUnit does - [com.tngtech.archunit.core.domain.JavaMethod.getCallsOfSelf] walks the real
 * invoke instructions in compiled bytecode, so this check is exact (a true call graph), not a
 * text-pattern search over source. `Layer`/`dependsOn` (Konsist's own architecture API) was
 * also considered and doesn't fit either way: it resolves layer membership by whole-package
 * glob, so a layer scoped to the `util` package would cover every function there, not just
 * these eight - broader than this batch, and the package-direction check a future layer test
 * over the whole pipeline should own instead (a different failure mode: bytecode/layer-direction
 * vs. a narrower, function-scoped call-graph check like this one).
 *
 * Kotlin compilation detail this check has to account for: the eight functions compile to
 * four distinct bytecode owner classes, not eight. `CrossMethodResolver.resolve` and
 * `ParameterFlowQuery.parameterFlowsThrough` are members of a Kotlin `object`, so they're
 * instance methods on that singleton class. `IRNodeExtensions.kt`'s three functions
 * (`hasO1Type`, `isCollectionLookup` - both overloads, `isFollowedByExit`) and
 * `RecursionDetector.kt`'s two (`isSelfCallOf`, `isRecursive`) are top-level extension
 * functions, so they compile to static methods on a generated file-facade class
 * (`IRNodeExtensionsKt`, `RecursionDetectorKt`) - see [SharedCrossMethodFunctions] for the
 * canonical name-to-owner-class mapping, shared with [UtilKDocContractTest].
 */
internal class UtilSharedFunctionCallSiteTest {
    // Kotlin object members and top-level-function file facades - see class kdoc above and
    // SharedCrossMethodFunctions for why these four classes, not eight, carry the compiled
    // methods.
    private val targetFunctionsByOwnerClass = SharedCrossMethodFunctions.namesByOwnerClass

    // A caller in the function's own owner class is always allowed - that's the function's
    // own file calling itself or a same-file sibling (resolveAndFindInternal calling
    // resolve(), isCollectionLookup's 2-arg overload delegating to the 4-arg one, isRecursive
    // calling isSelfCallOf), not a new external use.
    private val alwaysAllowedCallerClasses = targetFunctionsByOwnerClass.keys

    private val allowedCallerClasses =
        setOf(
            "com.github.tvinke.algorilla.engine.AnalysisEngine",
            "com.github.tvinke.algorilla.graph.CallGraphBuilder",
        )

    private val allowedCallerPackage = "com.github.tvinke.algorilla.rules.builtin"

    @Test
    fun `only rules-builtin, AnalysisEngine, CallGraphBuilder and each function's own file call the shared cross-method util functions`() {
        // Imported from compiled bytecode, not the classpath, so test classes (which call
        // these functions directly and are expected callers by construction, e.g.
        // CrossMethodResolverTest) never enter the scan in the first place.
        findOffendingCalls(MainClassesFixture.mainClasses).shouldBeEmpty()
    }

    private fun findOffendingCalls(mainClasses: JavaClasses): List<String> =
        targetFunctionsByOwnerClass.flatMap { (ownerClassName, functionNames) ->
            val owner = mainClasses.get(ownerClassName)
            owner.methods
                .filter { it.name in functionNames }
                .flatMap { method -> offendingCallsTo(owner, method) }
        }

    private fun offendingCallsTo(
        owner: JavaClass,
        method: JavaMethod,
    ): List<String> =
        method.callsOfSelf
            .filterNot { isAllowedCaller(it.originOwner) }
            .map { call -> "${owner.name}#${method.name} called from ${call.originOwner.name} at ${call.sourceCodeLocation}" }

    private fun isAllowedCaller(callerClass: JavaClass): Boolean =
        callerClass.name in alwaysAllowedCallerClasses ||
            callerClass.name in allowedCallerClasses ||
            callerClass.packageName == allowedCallerPackage ||
            callerClass.packageName.startsWith("$allowedCallerPackage.")
}
