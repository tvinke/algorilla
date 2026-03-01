package com.github.tvinke.algorilla.graph

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class CallGraphTest {
    @Test
    fun `should track caller-callee edges`() {
        val graph = CallGraph()
        graph.addEdge("A.process", "B.validate")

        graph.callees("A.process") shouldBe setOf("B.validate")
        graph.callers("B.validate") shouldBe setOf("A.process")
        graph.edgeCount() shouldBe 1
    }

    @Test
    fun `should return empty for unknown nodes`() {
        val graph = CallGraph()

        graph.callees("unknown") shouldBe emptySet()
        graph.callers("unknown") shouldBe emptySet()
    }

    @Test
    fun `should compute transitive callees with depth limit`() {
        val graph = CallGraph()
        graph.addEdge("A", "B")
        graph.addEdge("B", "C")
        graph.addEdge("C", "D")

        graph.transitiveCallees("A", maxDepth = 2).shouldContainExactlyInAnyOrder("B", "C")
        graph.transitiveCallees("A", maxDepth = 3).shouldContainExactlyInAnyOrder("B", "C", "D")
    }

    @Test
    fun `should handle cycles in transitive callees`() {
        val graph = CallGraph()
        graph.addEdge("A", "B")
        graph.addEdge("B", "C")
        graph.addEdge("C", "A")

        val result = graph.transitiveCallees("A", maxDepth = 10)
        result.shouldContainExactlyInAnyOrder("B", "C", "A")
    }
}
