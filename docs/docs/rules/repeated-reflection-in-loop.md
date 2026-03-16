---
tags:
  - Java
  - Kotlin
  - Groovy
---

# Repeated Reflection in Loop

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `repeated-reflection-in-loop` |
    | **[Severity](/algorilla/guide/understanding-output/#severity-levels)** | INFO — worth knowing, may not matter at your scale |
    | **[Confidence](/algorilla/guide/understanding-output/#confidence-levels)** | MEDIUM — likely correct, some context-dependent |
    | **Category** | Loop amplifiers |
    | **[Complexity](/algorilla/concepts/big-o-primer/)** | O(n·reflection) → O(n) |

## Description

Detects Java/Kotlin reflection calls inside loops. Reflection methods like `getDeclaredMethods()`, `getDeclaredFields()`, and `getAnnotations()` are 10-100x slower than direct access. They also allocate new arrays on each call, adding GC pressure. Results should be cached outside the loop.

This rule only flags the expensive reflection methods that perform class scanning or array allocation. Cheap accessors like `getModifiers()`, `getName()`, and `getReturnType()` are excluded.

## Typical

=== "Java"

    ```java
    for (Class<?> orderType : orderTypes) {
        Method[] methods = orderType.getDeclaredMethods();   // Array allocated every iteration
        for (Method m : methods) {
            if (m.isAnnotationPresent(OrderProcessor.class)) {
                processorMethods.add(m);
            }
        }
    }
    ```

## After

=== "Java"

    ```java
    Map<Class<?>, Method[]> methodCache = new HashMap<>();
    for (Class<?> orderType : orderTypes) {
        Method[] methods = methodCache.computeIfAbsent(
            orderType, Class::getDeclaredMethods);           // Cached per class
        for (Method m : methods) {
            if (m.isAnnotationPresent(OrderProcessor.class)) {
                processorMethods.add(m);
            }
        }
    }
    ```

## Suggestion

> Cache the reflection result in a local variable or Map<Class, ...> outside the loop

## Detected Methods

`getDeclaredMethods`, `getDeclaredFields`, `getDeclaredConstructors`, `getDeclaredAnnotations`, `getMethods`, `getFields`, `getConstructors`, `getAnnotations`, `getAnnotationsByType`, `getDeclaredAnnotationsByType`, `getGenericInterfaces`, `getInterfaces`, `getDeclaredClasses`, `getClasses`, `getParameterTypes`, `getGenericParameterTypes`, `getExceptionTypes`
