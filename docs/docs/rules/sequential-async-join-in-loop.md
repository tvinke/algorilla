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
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | WARNING — likely performance problem |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | HIGH — structurally proven |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n·wait) → O(max-wait) |

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

    Use [`Promise.all`](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise/all) to await all promises concurrently:

    ```javascript
    const results = await Promise.all(paymentPromises);
    ```

## How Detection Works

1. Identifies loop constructs
2. Inside each loop, looks for blocking calls (`.join()`, `.get()`)
3. Checks that the target variable name suggests a future or promise (`future`, `promise`, `async`, `completable`, `deferred`, `task`)

## Suggestion

> Collect all futures first, then call .join()/.get() outside the loop (e.g. CompletableFuture.allOf)
