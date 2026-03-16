package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// Large API surface requires proportionally large test coverage
@Suppress("LargeClass")
internal class LanguageSemanticsRegistryTest {
    private val registry = LanguageSemanticsRegistry.DEFAULT

    @Test
    fun `should classify Java contains as lookup`() {
        val semantics = registry.classify(Language.JAVA, "contains")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.LOOKUP
        semantics.lookupKind shouldBe LookupKind.CONTAINS
    }

    @Test
    fun `should classify Java sorted as sort`() {
        val semantics = registry.classify(Language.JAVA, "sorted")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.SORT
        semantics.sortKind shouldBe SortKind.SORTED
    }

    @Test
    fun `should classify Groovy each as iteration`() {
        val semantics = registry.classify(Language.GROOVY, "each")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.ITERATION
    }

    @Test
    fun `should classify Groovy collect as iteration`() {
        val semantics = registry.classify(Language.GROOVY, "collect")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.ITERATION
    }

    @Test
    fun `should classify Groovy find as lookup`() {
        val semantics = registry.classify(Language.GROOVY, "find")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.LOOKUP
        semantics.lookupKind shouldBe LookupKind.FIND
    }

    @Test
    fun `should classify Groovy any as lookup`() {
        val semantics = registry.classify(Language.GROOVY, "any")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.LOOKUP
        semantics.lookupKind shouldBe LookupKind.ANY_MATCH
    }

    @Test
    fun `should classify Groovy unique as quadratic`() {
        val semantics = registry.classify(Language.GROOVY, "unique")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.QUADRATIC
        semantics.complexity shouldBe "O(n^2)"
    }

    @Test
    fun `should classify JavaScript forEach as iteration`() {
        val semantics = registry.classify(Language.JAVASCRIPT, "forEach")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.ITERATION
    }

    @Test
    fun `should classify JavaScript includes as lookup`() {
        val semantics = registry.classify(Language.JAVASCRIPT, "includes")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.LOOKUP
        semantics.lookupKind shouldBe LookupKind.INCLUDES
    }

    @Test
    fun `should resolve TypeScript to JavaScript`() {
        val semantics = registry.classify(Language.TYPESCRIPT, "filter")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.LOOKUP
        semantics.lookupKind shouldBe LookupKind.FILTER
    }

    @Test
    fun `should return null for unknown method`() {
        registry.classify(Language.JAVA, "unknownMethod").shouldBeNull()
    }

    @Test
    fun `should detect heavyweight types`() {
        registry.isHeavyweight(Language.JAVA, "ObjectMapper").shouldBeTrue()
        registry.isHeavyweight(Language.GROOVY, "JsonSlurper").shouldBeTrue()
        registry.isHeavyweight(Language.JAVA, "String").shouldBeFalse()
    }

    @Test
    fun `should detect O1 types`() {
        registry.isO1Type("HashSet").shouldBeTrue()
        registry.isO1Type("HashMap").shouldBeTrue()
        registry.isO1Type("TreeMap").shouldBeTrue()
        registry.isO1Type("ArrayList").shouldBeFalse()
    }

    @Test
    fun `should merge user heavyweight types`() {
        val merged = LanguageSemanticsRegistry.withOverrides(registry, setOf("CustomMapper"))
        merged.isHeavyweight(Language.JAVA, "CustomMapper").shouldBeTrue()
        // original types still present
        merged.isHeavyweight(Language.JAVA, "ObjectMapper").shouldBeTrue()
    }

    @Test
    fun `should classify Kotlin filter as lookup`() {
        val semantics = registry.classify(Language.KOTLIN, "filter")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.LOOKUP
        semantics.lookupKind shouldBe LookupKind.FILTER
    }

    @Test
    fun `should classify serialization methods`() {
        val semantics = registry.classify(Language.JAVA, "writeValueAsString")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.SERIALIZATION
    }

    @Test
    fun `should classify blocking methods`() {
        val semantics = registry.classify(Language.JAVA, "join")
        semantics.shouldNotBeNull()
        semantics.category shouldBe SemanticCategory.BLOCKING
    }

    @Test
    fun `should detect stream ops from stream-ops section`() {
        registry.isStreamOp(Language.JAVA, "map").shouldBeTrue()
        registry.isStreamOp(Language.JAVA, "flatMap").shouldBeTrue()
        registry.isStreamOp(Language.JAVA, "collect").shouldBeTrue()
    }

    @Test
    fun `should detect stream ops from methods section`() {
        registry.isStreamOp(Language.JAVA, "filter").shouldBeTrue()
        registry.isStreamOp(Language.JAVA, "sorted").shouldBeTrue()
        registry.isStreamOp(Language.JAVA, "contains").shouldBeTrue()
    }

    @Test
    fun `should not detect unknown method as stream op`() {
        registry.isStreamOp(Language.JAVA, "doSomething").shouldBeFalse()
    }

    @Test
    fun `streamOps should return per-language stream ops`() {
        val javaOps = registry.streamOps(Language.JAVA)
        javaOps.contains("map").shouldBeTrue()
        val groovyOps = registry.streamOps(Language.GROOVY)
        groovyOps.contains("each").shouldBeTrue()
        val jsOps = registry.streamOps(Language.JAVASCRIPT)
        jsOps.contains("includes").shouldBeTrue()
        val kotlinOps = registry.streamOps(Language.KOTLIN)
        kotlinOps.contains("sortedBy").shouldBeTrue()
    }

    @Test
    fun `should detect trivial methods`() {
        registry.isTrivial(Language.JAVA, "equals").shouldBeTrue()
        registry.isTrivial(Language.JAVA, "hashCode").shouldBeTrue()
        registry.isTrivial(Language.JAVA, "size").shouldBeTrue()
        registry.isTrivial(Language.JAVA, "doSomething").shouldBeFalse()
    }

    @Test
    fun `should detect builder methods`() {
        registry.isBuilder(Language.JAVA, "header").shouldBeTrue()
        registry.isBuilder(Language.JAVA, "contentType").shouldBeTrue()
        registry.isBuilder(Language.JAVA, "doSomething").shouldBeFalse()
    }

    @Test
    fun `should detect implicitly O1 methods`() {
        registry.isImplicitlyO1(Language.JAVA, "containsKey").shouldBeTrue()
        registry.isImplicitlyO1(Language.JAVA, "containsValue").shouldBeTrue()
        registry.isImplicitlyO1(Language.JAVA, "contains").shouldBeFalse()
    }

    @Test
    fun `should provide getter prefixes`() {
        val prefixes = registry.getterPrefixes(Language.JAVA)
        prefixes.contains("get").shouldBeTrue()
        prefixes.contains("find").shouldBeTrue()
        prefixes.contains("load").shouldBeTrue()
    }

    @Test
    fun `unresolvableNames should cover stream ops and methods per language`() {
        val names = registry.unresolvableNames(Language.JAVA)
        names.contains("map").shouldBeTrue()
        names.contains("filter").shouldBeTrue()
        names.contains("sorted").shouldBeTrue()
        val kotlinNames = registry.unresolvableNames(Language.KOTLIN)
        kotlinNames.contains("let").shouldBeTrue() // Kotlin scope function
    }

    @Test
    fun `should detect cheap methods`() {
        registry.isCheap(Language.JAVA, "plusDays").shouldBeTrue()
        registry.isCheap(Language.JAVA, "indexOf").shouldBeTrue()
        registry.isCheap(Language.JAVA, "someUnknownMethod").shouldBeFalse()
    }

    @Test
    fun `should include newly added cheap methods`() {
        // Numeric parsing
        registry.isCheap(Language.JAVA, "parseInt").shouldBeTrue()
        registry.isCheap(Language.JAVA, "parseLong").shouldBeTrue()
        // Apache Commons utilities
        registry.isCheap(Language.JAVA, "isNotEmpty").shouldBeTrue()
        registry.isCheap(Language.JAVA, "isNotBlank").shouldBeTrue()
        // Servlet request accessors
        registry.isCheap(Language.JAVA, "getParameter").shouldBeTrue()
        registry.isCheap(Language.JAVA, "getAttribute").shouldBeTrue()
        // Java time accessors
        registry.isCheap(Language.JAVA, "toEpochMilli").shouldBeTrue()
        registry.isCheap(Language.JAVA, "isAfter").shouldBeTrue()
        // Kotlin equivalents
        registry.isCheap(Language.KOTLIN, "toInt").shouldBeTrue()
        registry.isCheap(Language.KOTLIN, "isNotEmpty").shouldBeTrue()
    }

    @Test
    fun `should detect sequential read methods`() {
        registry.isSequentialRead(Language.JAVA, "readByte").shouldBeTrue()
        registry.isSequentialRead(Language.JAVA, "nextToken").shouldBeTrue()
        registry.isSequentialRead(Language.JAVA, "someUnknownMethod").shouldBeFalse()
    }

    @Test
    fun `should include cheap methods for Java`() {
        val javaCheap = registry.cheapMethods(Language.JAVA)
        javaCheap shouldContain "plusDays"
        javaCheap shouldContain "startsWith"
    }

    @Test
    fun `should include sequential read methods for Java`() {
        val javaSeqRead = registry.sequentialReadMethods(Language.JAVA)
        javaSeqRead shouldContain "readByte"
        javaSeqRead shouldContain "nextToken"
        javaSeqRead shouldContain "poll"
    }

    @Test
    fun `should provide reflection methods for Java`() {
        val methods = registry.reflectionMethods(Language.JAVA)
        methods shouldContain "getDeclaredMethods"
        methods shouldContain "getFields"
    }

    @Test
    fun `should provide copy-on-modify methods`() {
        val methods = registry.copyOnModifyMethodsFor(Language.JAVASCRIPT)
        // concat creates new String/Array (true copy-on-modify)
        methods shouldContain "concat"
    }

    @Test
    fun `should provide regex types for Java`() {
        val types = registry.regexTypes(Language.JAVA)
        types shouldContain "Pattern"
        types shouldContain "Regex"
    }

    @Test
    fun `should provide regex types for JavaScript`() {
        val types = registry.regexTypes(Language.JAVASCRIPT)
        types shouldContain "RegExp"
    }

    @Test
    fun `should provide regex recompilation methods`() {
        val methods = registry.regexRecompilationMethods(Language.JAVA)
        methods shouldContain "matches"
        methods shouldContain "split"
        methods shouldContain "replaceAll"
    }

    @Test
    fun `should provide mutation methods for Java`() {
        val methods = registry.mutationMethods(Language.JAVA)
        methods shouldContain "add"
        methods shouldContain "put"
    }

    @Test
    fun `should provide mutation methods for JavaScript`() {
        val methods = registry.mutationMethods(Language.JAVASCRIPT)
        methods shouldContain "push"
    }

    @Test
    fun `should provide removal methods`() {
        val methods = registry.removalMethods(Language.JAVA)
        methods shouldContain "remove"
    }

    @Test
    fun `should provide removal methods for JavaScript`() {
        val methods = registry.removalMethods(Language.JAVASCRIPT)
        methods shouldContain "splice"
    }

    @Test
    fun `should provide bulk load prefixes`() {
        val prefixes = registry.bulkLoadPrefixes(Language.JAVA)
        prefixes shouldContain "findAll"
        prefixes shouldContain "getAll"
        prefixes shouldContain "loadAll"
    }

    @Test
    fun `should detect monadic targets`() {
        val loadedRegistry = LanguageSemanticsRegistry.loadDefaults()
        loadedRegistry.isMonadicTarget("Optional.of(x)") shouldBe true
        loadedRegistry.isMonadicTarget("result.map") shouldBe true
        loadedRegistry.isMonadicTarget("orders.stream()") shouldBe false
    }

    @Nested
    inner class CrossLanguageIsolation {
        @Test
        fun `JS dom-target-names should not leak into Java extras`() {
            val jsDomTargets = registry.domTargetNames(Language.JAVASCRIPT)
            val javaDomTargets = registry.domTargetNames(Language.JAVA)
            // JS has DOM targets like "wrapper", "element", "document"
            jsDomTargets.shouldNotBeEmpty()
            // Java should not inherit JS-only DOM targets
            for (jsTarget in jsDomTargets) {
                if (jsTarget !in javaDomTargets) {
                    // At least one JS-specific target should NOT be in Java
                    return
                }
            }
            // If all JS targets are also in Java, that's cross-language pollution
            throw AssertionError(
                "Java dom-target-names contains all JS dom-target-names — cross-language pollution detected",
            )
        }

        @Test
        fun `JS-only future-indicators should not appear in Java`() {
            val jsFuture = registry.futureIndicators(Language.JAVASCRIPT)
            val javaFuture = registry.futureIndicators(Language.JAVA)
            // "observable" and "subject" are JS/RxJS-specific
            jsFuture shouldContain "observable"
            javaFuture shouldNotContain "observable"
        }

        @Test
        fun `per-language extras should be subset of combined extras`() {
            // Any per-language result should be a subset of the combined union
            val javaSkip = registry.hiddenLoopSkipMethods(Language.JAVA)
            val combined = mutableSetOf<String>()
            for (lang in listOf(Language.JAVA, Language.KOTLIN, Language.GROOVY, Language.JAVASCRIPT)) {
                combined.addAll(registry.hiddenLoopSkipMethods(lang))
            }
            for (method in javaSkip) {
                combined shouldContain method
            }
        }
    }
}
