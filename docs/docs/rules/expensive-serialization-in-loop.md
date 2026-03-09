# Expensive Serialization in Loop

**Rule ID:** `expensive-serialization-in-loop` · **Severity:** WARNING · **Complexity:** O(n·serialize) → O(n)

## Description

Detects serialization or deserialization calls inside loops. Operations like `writeValueAsString()`, `readValue()`, or `JSON.stringify()` are themselves O(n) over the object graph — calling them inside a loop compounds the cost.

## Bad Example

=== "Java"

    ```java
    for (Order order : orders) {
        String json = objectMapper.writeValueAsString(order); // Serialization per iteration
        messages.add(json);
    }
    ```

=== "JavaScript"

    ```javascript
    orders.forEach(order => {
        const json = JSON.stringify(order); // Serialization per iteration
        messages.push(json);
    });
    ```

## Good Example

```java
String json = objectMapper.writeValueAsString(orders); // Serialize the whole batch once
sendBatch(json);
```

When individual serialization is required, consider whether the loop itself is necessary — often the consumer can accept a batch.

## Suggestion

> Move serialization outside the loop, or use batch serialization
