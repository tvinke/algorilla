# Sequential Async Join in Loop

**Rule ID:** `sequential-async-join-in-loop` · **Severity:** WARNING · **Complexity:** O(n·wait) → O(max-wait)

## Description

Detects blocking calls (`.join()`, `.get()`) on futures inside loops. Awaiting each future sequentially negates the benefit of async execution — the total wall time becomes the sum of all individual waits instead of the maximum.

## Bad Example

=== "Java"

    ```java
    for (CompletableFuture<Result> future : futures) {
        Result result = future.join(); // Blocks on each future sequentially
        results.add(result);
    }
    ```

=== "JavaScript"

    ```javascript
    for (const promise of promises) {
        const result = await promise; // Sequential await
        results.push(result);
    }
    ```

## Good Example

=== "Java"

    ```java
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    for (CompletableFuture<Result> future : futures) {
        results.add(future.join()); // All already completed
    }
    ```

=== "JavaScript"

    ```javascript
    const results = await Promise.all(promises);
    ```

## How Detection Works

1. Identifies loop constructs
2. Inside each loop, looks for blocking calls (`.join()`, `.get()`)
3. Checks that the target variable name suggests a future or promise (`future`, `promise`, `async`, `completable`, `deferred`, `task`)

## Suggestion

> Collect all futures first, then call .join()/.get() outside the loop (e.g. CompletableFuture.allOf)
