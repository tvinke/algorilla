---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Sequential Async Join in Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `sequential-async-join-in-loop` |
    | **Severity** | WARNING |
    | **Confidence** | HIGH |
    | **Category** | Loop amplifiers |
    | **Complexity** | O(n·wait) → O(max-wait) |

## Description

Detects blocking calls (`.join()`, `.get()`) on futures inside loops. Awaiting each future sequentially negates the benefit of async execution — the total wall time becomes the sum of all individual waits instead of the maximum.

## Typical

=== "Java"

    ```java
    for (CompletableFuture<PaymentResult> paymentFuture : paymentFutures) {
        PaymentResult result = paymentFuture.join(); // Blocks on each payment gateway response sequentially
        results.add(result);
    }
    ```

=== "JavaScript"

    ```javascript
    for (const paymentPromise of paymentPromises) {
        const result = await paymentPromise; // Sequential await on payment gateway
        results.push(result);
    }
    ```

## After

=== "Java"

    ```java
    CompletableFuture.allOf(paymentFutures.toArray(new CompletableFuture[0])).join();
    for (CompletableFuture<PaymentResult> paymentFuture : paymentFutures) {
        results.add(paymentFuture.join()); // All already completed
    }
    ```

=== "JavaScript"

    ```javascript
    const results = await Promise.all(paymentPromises);
    ```

## How Detection Works

1. Identifies loop constructs
2. Inside each loop, looks for blocking calls (`.join()`, `.get()`)
3. Checks that the target variable name suggests a future or promise (`future`, `promise`, `async`, `completable`, `deferred`, `task`)

## Suggestion

> Collect all futures first, then call .join()/.get() outside the loop (e.g. CompletableFuture.allOf)
