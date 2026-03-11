# String Concat in Loop

**Rule ID:** `string-concat-in-loop` · **Severity:** WARNING · **Complexity:** O(n²) → O(n)

## Description

Detects `String.concat()` calls inside loops. Each `concat()` creates a new `String` object and copies the entire accumulated content, turning an O(n) loop into O(n²). For a loop of 1000 items building a string, that's ~500,000 character copies instead of ~1000.

## Bad Example

=== "Java"

    ```java
    String report = "";
    for (Animal animal : animals) {
        report = report.concat(animal.getName()).concat(", ");  // Copies entire string each time
    }
    ```

## Good Example

=== "Java"

    ```java
    StringBuilder report = new StringBuilder();
    for (Animal animal : animals) {
        report.append(animal.getName()).append(", ");           // Appends in-place
    }
    return report.toString();
    ```

## Suggestion

> Use a StringBuilder to accumulate the result, then call toString() after the loop

## Note

The more common `+=` operator on strings (`result += item`) has the same O(n²) behavior but is not yet detected by this rule (requires parser-level support for assignment expressions). The `String.concat()` variant is detected.
