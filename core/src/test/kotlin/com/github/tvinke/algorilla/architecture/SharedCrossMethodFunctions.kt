package com.github.tvinke.algorilla.architecture

/**
 * The single, canonical definition of the eight shared cross-method `util` functions this
 * batch covers - [UtilKDocContractTest] and [UtilSharedFunctionCallSiteTest] each need a
 * slightly different shape of the same eight (a flat name set for Konsist's `withName`, an
 * owner-class-to-names grouping for ArchUnit's bytecode lookup), but both should derive from
 * this one list rather than maintaining their own copy - two independently hand-maintained
 * lists of "the eight functions" is exactly the kind of drift this whole batch exists to
 * guard against elsewhere.
 *
 * Keyed by function name to bytecode owner class - see [UtilSharedFunctionCallSiteTest]'s
 * class kdoc for why the owner is a Kotlin `object` class for two of them and a top-level-
 * function file-facade class for the other five.
 */
internal object SharedCrossMethodFunctions {
    val ownerClassByName: Map<String, String> =
        mapOf(
            "resolve" to "com.github.tvinke.algorilla.util.CrossMethodResolver",
            "parameterFlowsThrough" to "com.github.tvinke.algorilla.util.ParameterFlowQuery",
            "hasO1Type" to "com.github.tvinke.algorilla.util.IRNodeExtensionsKt",
            "isCollectionLookup" to "com.github.tvinke.algorilla.util.IRNodeExtensionsKt",
            "isFollowedByExit" to "com.github.tvinke.algorilla.util.IRNodeExtensionsKt",
            "isSelfCallOf" to "com.github.tvinke.algorilla.util.RecursionDetectorKt",
            "isRecursive" to "com.github.tvinke.algorilla.util.RecursionDetectorKt",
        )

    val names: Set<String> = ownerClassByName.keys

    val namesByOwnerClass: Map<String, Set<String>> =
        ownerClassByName.entries.groupBy({ it.value }, { it.key }).mapValues { it.value.toSet() }
}
