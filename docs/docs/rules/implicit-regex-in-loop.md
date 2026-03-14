---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Implicit Regex in Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `implicit-regex-in-loop` |
    | **Severity** | WARNING |
    | **Confidence** | MEDIUM |
    | **Category** | Loop amplifiers |
    | **Complexity** | O(n·compile) → O(n) |

## Description

Detects `String` methods that internally compile a regular expression on every call when used inside loops. Methods like `matches()`, `split()`, `replaceAll()`, and `replaceFirst()` call `Pattern.compile()` under the hood — compiling the same regex pattern on every iteration wastes CPU.

This rule is a companion to [`repeated-regex-in-loop`](repeated-regex-in-loop.md), which detects explicit `Pattern.compile()` calls. This rule catches the implicit variant hiding inside innocent-looking `String` methods.

## Typical

=== "Java"

    ```java
    for (Order order : orders) {
        if (order.getCategory().matches("^[A-Z][a-z]+$")) {  // Pattern compiled every iteration
            validOrders.add(order);
        }
    }
    ```

=== "Groovy"

    ```groovy
    orders.each { order ->
        def parts = order.description.split("\\s*,\\s*")     // Pattern compiled every iteration
        parts.each { descriptionParts.add(it.trim()) }
    }
    ```

## After

=== "Java"

    ```java
    Pattern categoryPattern = Pattern.compile("^[A-Z][a-z]+$");
    for (Order order : orders) {
        if (categoryPattern.matcher(order.getCategory()).matches()) { // Compiled once
            validOrders.add(order);
        }
    }
    ```

=== "Groovy"

    ```groovy
    def descriptionSplitter = Pattern.compile("\\s*,\\s*")
    orders.each { order ->
        def parts = descriptionSplitter.split(order.description) // Compiled once
        parts.each { descriptionParts.add(it.trim()) }
    }
    ```

## Suggestion

> Pre-compile with Pattern.compile() outside the loop and use Matcher directly

## Affected Methods

| Method | What it does internally |
|--------|----------------------|
| `String.matches(regex)` | `Pattern.compile(regex).matcher(this).matches()` |
| `String.split(regex)` | `Pattern.compile(regex).split(this)` |
| `String.replaceAll(regex, replacement)` | `Pattern.compile(regex).matcher(this).replaceAll(replacement)` |
| `String.replaceFirst(regex, replacement)` | `Pattern.compile(regex).matcher(this).replaceFirst(replacement)` |
