package com.github.tvinke.algorilla.rules.builtin

import com.github.tvinke.algorilla.rules.Rule

/**
 * Central registry of all built-in rules. Used by CLI, Gradle plugin, and
 * any other entry point that needs the default rule set.
 */
public object BuiltinRules {
    public fun all(): List<Rule> =
        listOf(
            NestedLookupRule(),
            SortForLastRule(),
            ExpensiveSortComparatorRule(),
            ExpensiveCallbackRule(),
            RepeatedLinearScanRule(),
            FullScanForSingleLookupRule(),
            HeavyweightObjectPerInvocationRule(),
            RepeatedRegexInLoopRule(),
            ExpensiveSerializationInLoopRule(),
            SequentialAsyncJoinInLoopRule(),
            InLoopCollectionBuildingRule(),
            NPlusOneRepositoryCallRule(),
            RedundantExpensiveCallRule(),
            UncachedGetterRule(),
            ChainedGettersRule(),
            FilterAfterSortRule(),
            HiddenNestedLoopRule(),
            ImplicitRegexInLoopRule(),
            StringConcatInLoopRule(),
            QuadraticRemovalRule(),
            RepeatedReflectionInLoopRule(),
            ParallelPipelineBottleneckRule(),
        )
}
