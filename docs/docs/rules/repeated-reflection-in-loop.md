# Repeated Reflection in Loop

**Rule ID:** `repeated-reflection-in-loop` · **Severity:** INFO · **Complexity:** O(n·reflection) → O(n)

## Description

Detects Java/Kotlin reflection calls inside loops. Reflection methods like `getDeclaredMethods()`, `getDeclaredFields()`, and `getAnnotations()` are 10-100x slower than direct access. They also allocate new arrays on each call, adding GC pressure. Results should be cached outside the loop.

This rule only flags the expensive reflection methods that perform class scanning or array allocation. Cheap accessors like `getModifiers()`, `getName()`, and `getReturnType()` are excluded.

## Bad Example

=== "Java"

    ```java
    for (Class<?> animalType : animalTypes) {
        Method[] methods = animalType.getDeclaredMethods();  // Array allocated every iteration
        for (Method m : methods) {
            if (m.isAnnotationPresent(Feeding.class)) {
                feedingMethods.add(m);
            }
        }
    }
    ```

## Good Example

=== "Java"

    ```java
    Map<Class<?>, Method[]> methodCache = new HashMap<>();
    for (Class<?> animalType : animalTypes) {
        Method[] methods = methodCache.computeIfAbsent(
            animalType, Class::getDeclaredMethods);          // Cached per class
        for (Method m : methods) {
            if (m.isAnnotationPresent(Feeding.class)) {
                feedingMethods.add(m);
            }
        }
    }
    ```

## Suggestion

> Cache the reflection result in a local variable or Map<Class, ...> outside the loop

## Detected Methods

`getDeclaredMethods`, `getDeclaredFields`, `getDeclaredConstructors`, `getDeclaredAnnotations`, `getMethods`, `getFields`, `getConstructors`, `getAnnotations`, `getAnnotationsByType`, `getDeclaredAnnotationsByType`, `getGenericInterfaces`, `getInterfaces`, `getDeclaredClasses`, `getClasses`, `getParameterTypes`, `getGenericParameterTypes`, `getExceptionTypes`
