# Repeated Regex in Loop

**Rule ID:** `repeated-regex-in-loop` · **Severity:** WARNING · **Complexity:** O(n·compile) → O(n)

## Description

Detects regex pattern compilation inside loops. Compiling a regular expression is expensive — doing it on every iteration wastes CPU cycles when the compiled pattern could be reused.

## Bad Example

=== "Java"

    ```java
    for (String line : lines) {
        Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}"); // Recompiled every iteration
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            dates.add(m.group());
        }
    }
    ```

=== "JavaScript"

    ```javascript
    lines.forEach(line => {
        const pattern = new RegExp('\\d{4}-\\d{2}-\\d{2}'); // Recompiled every iteration
        const match = line.match(pattern);
        if (match) dates.push(match[0]);
    });
    ```

## Good Example

=== "Java"

    ```java
    Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    for (String line : lines) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            dates.add(m.group());
        }
    }
    ```

=== "JavaScript"

    ```javascript
    const pattern = /\d{4}-\d{2}-\d{2}/;
    lines.forEach(line => {
        const match = line.match(pattern);
        if (match) dates.push(match[0]);
    });
    ```

## Suggestion

> Compile the pattern once outside the loop and reuse the compiled Pattern
