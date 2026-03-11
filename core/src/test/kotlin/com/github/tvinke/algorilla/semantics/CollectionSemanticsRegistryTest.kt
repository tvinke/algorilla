package com.github.tvinke.algorilla.semantics

import com.github.tvinke.algorilla.model.Language
import com.github.tvinke.algorilla.model.LookupKind
import com.github.tvinke.algorilla.model.SortKind
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
internal class CollectionSemanticsRegistryTest {
    private val registry = CollectionSemanticsRegistry.loadDefaults()

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
        val merged = CollectionSemanticsRegistry.withOverrides(registry, setOf("CustomMapper"))
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
    fun `allStreamOps should include all languages`() {
        val all = registry.allStreamOps()
        all.contains("map").shouldBeTrue()
        all.contains("each").shouldBeTrue() // Groovy
        all.contains("includes").shouldBeTrue() // JS
        all.contains("sortedBy").shouldBeTrue() // Kotlin
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
    fun `allUnresolvableNames should cover stream ops and methods`() {
        val names = registry.allUnresolvableNames()
        names.contains("map").shouldBeTrue()
        names.contains("filter").shouldBeTrue()
        names.contains("sorted").shouldBeTrue()
        names.contains("let").shouldBeTrue() // Kotlin scope function
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
    fun `should include cheap methods in cross-language union`() {
        val all = registry.allCheapMethods()
        all shouldContain "plusDays"
        all shouldContain "startsWith"
    }

    @Test
    fun `should include sequential read methods in cross-language union`() {
        val all = registry.allSequentialReadMethods()
        all shouldContain "readByte"
        all shouldContain "nextToken"
        all shouldContain "poll"
    }
}
