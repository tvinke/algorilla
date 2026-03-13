---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Regex Recompilation in Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `regex-recompilation-in-loop` |
    | **Severity** | WARNING |
    | **Confidence** | MEDIUM |
    | **Category** | Loop amplifiers |
    | **Complexity** | O(n·compile) → O(n) |

## Description

Detects methods that recompile a regular expression on every call when used inside loops. On the JVM, methods like `matches()`, `split()`, `replaceAll()`, and `replaceFirst()` call `Pattern.compile()` under the hood. In JS/TS, regex literals passed to `replace`/`match`/`split` are recompiled each iteration.

This rule is a companion to [`repeated-regex-in-loop`](repeated-regex-in-loop.md), which detects explicit `Pattern.compile()` or `new RegExp()` calls. This rule catches the hidden recompilation inside innocent-looking string methods and regex literals.

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
