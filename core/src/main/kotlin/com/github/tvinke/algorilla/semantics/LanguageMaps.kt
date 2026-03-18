package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.Language

@Suppress("LongParameterList")
internal class LanguageMaps(
    val methods: MutableMap<Language, Map<String, MethodSemantics>> = mutableMapOf(),
    val heavyweight: MutableMap<Language, Set<String>> = mutableMapOf(),
    val collectionTypes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val o1: MutableMap<Language, Set<String>> = mutableMapOf(),
    val streamOps: MutableMap<Language, Set<String>> = mutableMapOf(),
    val scopeOps: MutableMap<Language, Set<String>> = mutableMapOf(),
    val trivial: MutableMap<Language, Set<String>> = mutableMapOf(),
    val builder: MutableMap<Language, Set<String>> = mutableMapOf(),
    val getterPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val cheap: MutableMap<Language, Set<String>> = mutableMapOf(),
    val sequentialRead: MutableMap<Language, Set<String>> = mutableMapOf(),
    val reflection: MutableMap<Language, Set<String>> = mutableMapOf(),
    val copyOnModify: MutableMap<Language, Set<String>> = mutableMapOf(),
    val regexTypes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val regexRecompilation: MutableMap<Language, Set<String>> = mutableMapOf(),
    val mutation: MutableMap<Language, Set<String>> = mutableMapOf(),
    val removal: MutableMap<Language, Set<String>> = mutableMapOf(),
    val bulkLoadPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val fullScan: MutableMap<Language, Set<String>> = mutableMapOf(),
    val io: MutableMap<Language, Set<String>> = mutableMapOf(),
    val o1Factory: MutableMap<Language, Set<String>> = mutableMapOf(),
    val memoization: MutableMap<Language, Set<String>> = mutableMapOf(),
    val treeTraversal: MutableMap<Language, Set<String>> = mutableMapOf(),
    val visitedSetNames: MutableMap<Language, Set<String>> = mutableMapOf(),
    val purePrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val sideEffectPrefixes: MutableMap<Language, List<String>> = mutableMapOf(),
    val sideEffectTargets: MutableMap<Language, List<String>> = mutableMapOf(),
    val stringIndicators: MutableMap<Language, Set<String>> = mutableMapOf(),
    val stringNameSuffixes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val stringExactNames: MutableMap<Language, Set<String>> = mutableMapOf(),
    val monadicTypes: MutableMap<Language, Set<String>> = mutableMapOf(),
    val monadicVarNames: MutableMap<Language, Set<String>> = mutableMapOf(),
    val bulkAlternatives: MutableMap<Language, Map<String, String>> = mutableMapOf(),
    val extras: MutableMap<Language, MutableMap<String, Set<String>>> = mutableMapOf(),
) {
    @Suppress("CyclomaticComplexMethod", "LongMethod")
    fun merge(
        lang: Language,
        parsed: ParsedYaml,
    ) {
        methods[lang] = (methods[lang] ?: emptyMap()) + parsed.methods
        heavyweight[lang] = (heavyweight[lang] ?: emptySet()) + parsed.heavyweightTypes
        collectionTypes[lang] = (collectionTypes[lang] ?: emptySet()) + parsed.collectionTypes
        o1[lang] = (o1[lang] ?: emptySet()) + parsed.o1Types
        streamOps[lang] = (streamOps[lang] ?: emptySet()) + parsed.streamOps
        scopeOps[lang] = (scopeOps[lang] ?: emptySet()) + parsed.scopeOps
        trivial[lang] = (trivial[lang] ?: emptySet()) + parsed.trivialMethods
        builder[lang] = (builder[lang] ?: emptySet()) + parsed.builderMethods
        getterPrefixes[lang] = ((getterPrefixes[lang] ?: emptyList()) + parsed.getterPrefixes).distinct()
        cheap[lang] = (cheap[lang] ?: emptySet()) + parsed.cheapMethods
        sequentialRead[lang] = (sequentialRead[lang] ?: emptySet()) + parsed.sequentialReadMethods
        reflection[lang] = (reflection[lang] ?: emptySet()) + parsed.reflectionMethods
        copyOnModify[lang] = (copyOnModify[lang] ?: emptySet()) + parsed.copyOnModifyMethods
        regexTypes[lang] = (regexTypes[lang] ?: emptySet()) + parsed.regexTypes
        regexRecompilation[lang] = (regexRecompilation[lang] ?: emptySet()) + parsed.regexRecompilationMethods
        mutation[lang] = (mutation[lang] ?: emptySet()) + parsed.mutationMethods
        removal[lang] = (removal[lang] ?: emptySet()) + parsed.removalMethods
        bulkLoadPrefixes[lang] = ((bulkLoadPrefixes[lang] ?: emptyList()) + parsed.bulkLoadPrefixes).distinct()
        fullScan[lang] = (fullScan[lang] ?: emptySet()) + parsed.fullScanMethods
        io[lang] = (io[lang] ?: emptySet()) + parsed.ioMethods
        o1Factory[lang] = (o1Factory[lang] ?: emptySet()) + parsed.o1FactoryMethods
        memoization[lang] = (memoization[lang] ?: emptySet()) + parsed.memoizationMethods
        treeTraversal[lang] = (treeTraversal[lang] ?: emptySet()) + parsed.treeTraversalAccessors
        visitedSetNames[lang] = (visitedSetNames[lang] ?: emptySet()) + parsed.visitedSetNames
        purePrefixes[lang] = ((purePrefixes[lang] ?: emptyList()) + parsed.purePrefixes).distinct()
        sideEffectPrefixes[lang] = ((sideEffectPrefixes[lang] ?: emptyList()) + parsed.sideEffectPrefixes).distinct()
        sideEffectTargets[lang] = ((sideEffectTargets[lang] ?: emptyList()) + parsed.sideEffectTargets).distinct()
        stringIndicators[lang] = (stringIndicators[lang] ?: emptySet()) + parsed.stringIndicators
        stringNameSuffixes[lang] = (stringNameSuffixes[lang] ?: emptySet()) + parsed.stringNameSuffixes
        stringExactNames[lang] = (stringExactNames[lang] ?: emptySet()) + parsed.stringExactNames
        monadicTypes[lang] = (monadicTypes[lang] ?: emptySet()) + parsed.monadicTypes
        monadicVarNames[lang] = (monadicVarNames[lang] ?: emptySet()) + parsed.monadicVarNames
        bulkAlternatives[lang] = (bulkAlternatives[lang] ?: emptyMap()) + parsed.bulkAlternatives
        // Merge all extra sections
        val langExtras = extras.getOrPut(lang) { mutableMapOf() }
        for ((key, values) in parsed.extras) {
            langExtras[key] = (langExtras[key] ?: emptySet()) + values
        }
    }

    @Suppress("LongMethod") // Shallow-copies all 33 map fields — unavoidable width
    fun copy(): LanguageMaps =
        LanguageMaps(
            methods = methods.toMutableMap(),
            heavyweight = heavyweight.toMutableMap(),
            collectionTypes = collectionTypes.toMutableMap(),
            o1 = o1.toMutableMap(),
            streamOps = streamOps.toMutableMap(),
            scopeOps = scopeOps.toMutableMap(),
            trivial = trivial.toMutableMap(),
            builder = builder.toMutableMap(),
            getterPrefixes = getterPrefixes.toMutableMap(),
            cheap = cheap.toMutableMap(),
            sequentialRead = sequentialRead.toMutableMap(),
            reflection = reflection.toMutableMap(),
            copyOnModify = copyOnModify.toMutableMap(),
            regexTypes = regexTypes.toMutableMap(),
            regexRecompilation = regexRecompilation.toMutableMap(),
            mutation = mutation.toMutableMap(),
            removal = removal.toMutableMap(),
            bulkLoadPrefixes = bulkLoadPrefixes.toMutableMap(),
            fullScan = fullScan.toMutableMap(),
            io = io.toMutableMap(),
            o1Factory = o1Factory.toMutableMap(),
            memoization = memoization.toMutableMap(),
            treeTraversal = treeTraversal.toMutableMap(),
            visitedSetNames = visitedSetNames.toMutableMap(),
            purePrefixes = purePrefixes.toMutableMap(),
            sideEffectPrefixes = sideEffectPrefixes.toMutableMap(),
            sideEffectTargets = sideEffectTargets.toMutableMap(),
            stringIndicators = stringIndicators.toMutableMap(),
            stringNameSuffixes = stringNameSuffixes.toMutableMap(),
            stringExactNames = stringExactNames.toMutableMap(),
            monadicTypes = monadicTypes.toMutableMap(),
            monadicVarNames = monadicVarNames.toMutableMap(),
            bulkAlternatives = bulkAlternatives.toMutableMap(),
            extras = extras.toMutableMap(),
        )
}
