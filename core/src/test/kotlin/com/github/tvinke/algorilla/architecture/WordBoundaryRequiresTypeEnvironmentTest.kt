package com.github.tvinke.algorilla.architecture

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethodCall
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.library.freeze.FreezingArchRule
import org.junit.jupiter.api.Test

/**
 * A `rules/builtin` class that classifies a variable or receiver name against a
 * type-suggestive pattern (`startsWithAtWordBoundary`/`endsWithAtWordBoundary`/
 * `containsAtWordBoundary`/`containsAnyAtWordBoundary`/`matchesAnyTargetPattern` from
 * `WordBoundaryMatching.kt`) should also consult [com.github.tvinke.algorilla.semantics.TypeEnvironment]
 * somewhere in the same file - a name is a hint, not proof, and today's campaign (the
 * name-vs-type recursion family, the bare-`contains()` sweep) is one long list of bugs from
 * trusting the hint alone. This pins that discipline down as a standing architecture rule
 * instead of a one-off campaign finding.
 *
 * New, unvalidated rule, not a re-confirmation of something already known-good - built and
 * run in audit mode first (read every hit by hand) before wiring it up here. Of 28 rule
 * files, 13 call a word-boundary matcher without also calling TypeEnvironment in the same
 * file (rule class + its file's top-level-function facade class - same Kotlin-compilation
 * nuance as [UtilSharedFunctionCallSiteTest]: `ChainedGettersRule.kt`'s private top-level
 * `isGetterPattern` compiles onto `ChainedGettersRuleKt`, not the `ChainedGettersRule` class
 * itself, so checking only the class body would miss it).
 *
 * Of those 13, five are permanent exceptions (see [permanentExceptions] below) - either the
 * word-boundary match is against a *method name* (is this called like a getter / bytecode
 * instruction / type-check predicate), which isn't a variable-type question at all, or the
 * file already does the right layered thing through a different, equally legitimate
 * resolution path this check can't see (`RegexRecompilationInLoopRule` checks an actual
 * resolved `declaredType` string first and only falls back to the name heuristic when that's
 * null - it just gets the type from somewhere other than the `TypeEnvironment` class, so
 * this is an audit blind spot, not technical debt, and is labeled as such rather than folded
 * in with the other four as if it were the same kind of exception).
 *
 * The other eight are genuine gaps - a receiver name is checked against a repo/DOM/date/
 * regex/future-ish pattern with nothing to confirm it. Real, but not this batch's to fix:
 * eight existing rule files, each needing its own look at whether adding a TypeEnvironment
 * check is safe there (fixture coverage, false-positive risk) - tracked as follow-up work
 * elsewhere, not resolved in this branch. [FreezingArchRule] is exactly the tool for that
 * split: it freezes today's eight into `src/test/resources/frozen` (committed, so CI and
 * every clone start from the same baseline) so the build stays green without either
 * silently ignoring them or bundling an eight-file fix into this PR - and still fails hard
 * the moment a *ninth*, new violation shows up anywhere in `rules/builtin`.
 */
internal class WordBoundaryRequiresTypeEnvironmentTest {
    private val wordBoundaryOwner = "com.github.tvinke.algorilla.util.WordBoundaryMatchingKt"
    private val wordBoundaryFunctions =
        setOf(
            "startsWithAtWordBoundary",
            "endsWithAtWordBoundary",
            "containsAtWordBoundary",
            "containsAnyAtWordBoundary",
            "matchesAnyTargetPattern",
        )
    private val typeEnvironmentOwner = "com.github.tvinke.algorilla.semantics.TypeEnvironment"
    private val typeEnvironmentMethods =
        setOf("typeOf", "isO1", "isCollection", "isBoundedSmallCollection", "isString", "isList", "declaredTypeName")

    // Permanent, hand-reviewed exceptions - not technical debt, not frozen violations.
    private val permanentExceptions =
        mapOf(
            "ChainedGettersRule" to "matches call.name against getter prefixes - a method-name check, no variable-type question exists",
            "HiddenNestedLoopRule" to
                "matches method names against known string/byte-iteration skip patterns - method-name check, not a variable type",
            "RedundantExpensiveCallRule" to
                "matches call.name against type-check-predicate/bytecode-instruction name patterns - method-name check",
            "UncachedGetterRule" to "matches call.name against getter prefixes - a method-name check, no variable-type question exists",
            "RegexRecompilationInLoopRule" to
                "already checks an actual resolved declaredType first and only falls back to the name heuristic when that's " +
                "null - a real layered defense, just not through the TypeEnvironment class specifically, so this check " +
                "can't see it: an audit blind spot, not a gap",
        )

    @Test
    fun `rule classes that classify a name against a type-suggestive pattern also consult TypeEnvironment`() {
        val mainClasses = MainClassesFixture.mainClasses

        val rule =
            classes()
                .that()
                .resideInAPackage("com.github.tvinke.algorilla.rules.builtin")
                .and()
                .haveSimpleNameEndingWith("Rule")
                .should(consultTypeEnvironmentAlongsideWordBoundary(mainClasses))

        FreezingArchRule.freeze(rule).check(mainClasses)
    }

    private fun consultTypeEnvironmentAlongsideWordBoundary(mainClasses: JavaClasses): ArchCondition<JavaClass> =
        object : ArchCondition<JavaClass>(
            "call a TypeEnvironment method somewhere in the file when calling a word-boundary matcher",
        ) {
            override fun check(
                item: JavaClass,
                events: ConditionEvents,
            ) {
                if (item.name.substringAfterLast('.') in permanentExceptions) return

                val facade = mainClasses.firstOrNull { it.name == "${item.name}Kt" }
                val calls = item.methodCallsFromSelf + facade?.methodCallsFromSelf.orEmpty()
                val usesWordBoundary = calls.callsAnyOf(wordBoundaryOwner, wordBoundaryFunctions)
                val usesTypeEnvironment = calls.callsAnyOf(typeEnvironmentOwner, typeEnvironmentMethods)

                if (usesWordBoundary && !usesTypeEnvironment) {
                    events.add(
                        SimpleConditionEvent.violated(
                            item,
                            "${item.name} calls a word-boundary matcher but never calls TypeEnvironment in the same file",
                        ),
                    )
                }
            }
        }

    // Kotlin functions with a default parameter (word-boundary matchers all default
    // ignoreCase) compile an omitted-arg call site to a synthetic `name$default` bridge
    // method, not `name` itself - matching either form catches both.
    private fun Collection<JavaMethodCall>.callsAnyOf(
        owner: String,
        names: Set<String>,
    ): Boolean =
        any { call ->
            call.target.owner.name == owner && names.any { call.target.name == it || call.target.name == "$it\$default" }
        }
}
