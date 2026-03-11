---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Repeated Regex in Loop

**Rule ID:** `repeated-regex-in-loop` · **Severity:** WARNING · **Complexity:** O(n·compile) → O(n)

## Description

Detects regex pattern compilation inside loops. Compiling a regular expression is expensive — doing it on every iteration wastes CPU cycles when the compiled pattern could be reused.

## Bad Example

=== "Java"

    ```java
    for (String transactionRef : transactionRefs) {
        Pattern pattern = Pattern.compile("INV-\\d{4}-\\d{6}"); // Recompiled every iteration
        Matcher m = pattern.matcher(transactionRef);
        if (m.find()) {
            invoiceRefs.add(m.group());
        }
    }
    ```

=== "JavaScript"

    ```javascript
    transactionRefs.forEach(ref => {
        const pattern = new RegExp('INV-\\d{4}-\\d{6}'); // Recompiled every iteration
        const match = ref.match(pattern);
        if (match) invoiceRefs.push(match[0]);
    });
    ```

## Good Example

=== "Java"

    ```java
    Pattern pattern = Pattern.compile("INV-\\d{4}-\\d{6}");
    for (String transactionRef : transactionRefs) {
        Matcher m = pattern.matcher(transactionRef);
        if (m.find()) {
            invoiceRefs.add(m.group());
        }
    }
    ```

=== "JavaScript"

    ```javascript
    const pattern = /INV-\d{4}-\d{6}/;
    transactionRefs.forEach(ref => {
        const match = ref.match(pattern);
        if (match) invoiceRefs.push(match[0]);
    });
    ```

## Suggestion

> Compile the pattern once outside the loop and reuse the compiled Pattern
