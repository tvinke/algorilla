package com.github.tvinke.algorilla.semantics

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class MethodPurityTest {
    @Test
    fun `getter methods are pure`() {
        MethodPurity.classify("getName") shouldBe Purity.PURE
        MethodPurity.classify("getOrDefault") shouldBe Purity.PURE
        MethodPurity.classify("getValue") shouldBe Purity.PURE
    }

    @Test
    fun `finder methods are pure`() {
        MethodPurity.classify("findById") shouldBe Purity.PURE
        MethodPurity.classify("findAll") shouldBe Purity.PURE
    }

    @Test
    fun `compute and calculate methods are pure`() {
        MethodPurity.classify("computeHash") shouldBe Purity.PURE
        MethodPurity.classify("calculateTotal") shouldBe Purity.PURE
    }

    @Test
    fun `boolean check methods are pure`() {
        MethodPurity.classify("isEmpty") shouldBe Purity.PURE
        MethodPurity.classify("isValid") shouldBe Purity.PURE
        MethodPurity.classify("hasPermission") shouldBe Purity.PURE
        MethodPurity.classify("canAccess") shouldBe Purity.PURE
        MethodPurity.classify("containsKey") shouldBe Purity.PURE
    }

    @Test
    fun `conversion methods are pure`() {
        MethodPurity.classify("toString") shouldBe Purity.PURE
        MethodPurity.classify("toList") shouldBe Purity.PURE
        MethodPurity.classify("convertValue") shouldBe Purity.PURE
        MethodPurity.classify("parseJson") shouldBe Purity.PURE
        MethodPurity.classify("formatDate") shouldBe Purity.PURE
    }

    @Test
    fun `factory methods are pure`() {
        MethodPurity.classify("createInstance") shouldBe Purity.PURE
        MethodPurity.classify("buildRequest") shouldBe Purity.PURE
        MethodPurity.classify("of") shouldBe Purity.PURE
        MethodPurity.classify("fromString") shouldBe Purity.PURE
    }

    @Test
    fun `setter methods are side-effectful`() {
        MethodPurity.classify("setName") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("setValue") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `mutation methods are side-effectful`() {
        MethodPurity.classify("addItem") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("putValue") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("removeEntry") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("deleteAll") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("clearCache") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("insertRow") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("updateRecord") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("appendLine") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `IO methods are side-effectful`() {
        MethodPurity.classify("sendMessage") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("writeBytes") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("saveToFile") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("uploadData") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("downloadFile") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `logging methods are side-effectful`() {
        MethodPurity.classify("logError") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("println") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("debugLog") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `lifecycle methods are side-effectful`() {
        MethodPurity.classify("close") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("shutdown") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("destroy") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("dispose") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `assertion methods are side-effectful`() {
        MethodPurity.classify("assertEquals") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("verifyNoMoreInteractions") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("expectThrows") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `event methods are side-effectful`() {
        MethodPurity.classify("emitEvent") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("dispatchAction") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("publishMessage") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("notifyObservers") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("fireEvent") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `unknown methods return UNKNOWN`() {
        MethodPurity.classify("process") shouldBe Purity.UNKNOWN
        MethodPurity.classify("handle") shouldBe Purity.UNKNOWN
        MethodPurity.classify("doSomething") shouldBe Purity.UNKNOWN
        MethodPurity.classify("run") shouldBe Purity.UNKNOWN
    }

    @Test
    fun `classification is case-insensitive`() {
        MethodPurity.classify("GetName") shouldBe Purity.PURE
        MethodPurity.classify("SET_VALUE") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("IsValid") shouldBe Purity.PURE
    }

    @Test
    fun `side-effect prefixes take precedence over pure prefixes`() {
        // "should" appears in both lists but side-effect is checked first
        MethodPurity.classify("should") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `target-aware classification detects logger targets`() {
        MethodPurity.classify("getMessage", "logger") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("getMessage", "console") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("getMessage", "System.out") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `target-aware classification detects builder targets`() {
        MethodPurity.classify("path", "uriBuilder") shouldBe Purity.SIDE_EFFECT
        MethodPurity.classify("header", "request") shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `target-aware classification falls back to name when target is null`() {
        MethodPurity.classify("getName", null) shouldBe Purity.PURE
        MethodPurity.classify("setName", null) shouldBe Purity.SIDE_EFFECT
    }

    @Test
    fun `isSideEffect convenience method`() {
        MethodPurity.isSideEffect("setName") shouldBe true
        MethodPurity.isSideEffect("getName") shouldBe false
        MethodPurity.isSideEffect("process") shouldBe false
        MethodPurity.isSideEffect("getMessage", "logger") shouldBe true
    }
}
