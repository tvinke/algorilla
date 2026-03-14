---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# IO In Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `io-in-loop` |
    | **Severity** | WARNING |
    | **Confidence** | MEDIUM |
    | **Category** | Loop amplifiers |
    | **Complexity** | O(n·IO) → O(1·IO + n) |

## Description

Detects network, database, or file system calls inside loops. Each iteration incurs a full round-trip (network latency, disk seek, query execution), so the total wall-clock time scales linearly with the loop size — even when the computational cost is trivial.

This is one of the most impactful performance patterns in production code. A loop that makes 100 HTTP calls takes 100× the latency of a single batch call, regardless of how fast the server responds.

## Typical

=== "Java"

    ```java
    for (Order order : orders) {
        // HTTP round-trip per iteration
        OrderDetails d = restTemplate.getForObject(
            "/api/details/" + order.getId(), OrderDetails.class);
        details.add(d);
    }
    ```

=== "Java (JDBC)"

    ```java
    for (String id : orderIds) {
        ResultSet rs = conn.prepareStatement(
            "SELECT * FROM orders WHERE id = ?").executeQuery();
        result.add(mapRow(rs));
    }
    ```

=== "JavaScript"

    ```javascript
    for (const id of orderIds) {
        const response = await fetch(`/api/orders/${id}`);
        orderDetails.push(await response.json());
    }
    ```

## After

=== "Java"

    ```java
    // Single batch call
    List<OrderDetails> details = restTemplate.postForObject(
        "/api/details/batch", orderIds, List.class);
    ```

=== "Java (JDBC)"

    ```java
    // Single query with IN clause
    ResultSet rs = conn.prepareStatement(
        "SELECT * FROM orders WHERE id IN (?)").executeQuery();
    ```

=== "JavaScript"

    ```javascript
    // Single batch fetch
    const response = await fetch('/api/orders/batch', {
        method: 'POST',
        body: JSON.stringify(orderIds),
    });
    const orderDetails = await response.json();
    ```

## Suggestion

> Batch the IO operation outside the loop, or use a bulk API (e.g. saveAll, findAllById)

## Covered IO Categories

The rule detects IO methods from YAML semantics files (`io-methods` section). Coverage includes:

- **Database:** JDBC (`executeQuery`, `prepareStatement`), Groovy SQL (`eachRow`, `rows`)
- **HTTP:** Spring RestTemplate (`getForObject`, `exchange`, etc.), Ktor Client, `fetch()` in JS
- **File system:** Kotlin file extensions (`readText`, `writeText`), Node.js sync fs operations

Framework overlays (Spring, Ktor, Node) extend the base language methods with framework-specific IO calls.
