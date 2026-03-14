---
tags:
  - Java
  - Kotlin
  - Groovy
  - JavaScript
---

# Expensive Construction

!!! info "Rule details"
    | | |
    |---|---|
    | **Rule ID** | `expensive-construction` |
    | **Severity** | INFO |
    | **Confidence** | MEDIUM |
    | **Category** | Construction cost |
    | **Complexity** | O(init) per call → O(1) amortized |

## Description

Detects creation of heavyweight objects (like `ObjectMapper`, `Gson`, `DocumentBuilderFactory`) inside method bodies. These objects are expensive to construct and should typically be reused as static fields or injected dependencies.

## Typical

```java
public String generateJson(Invoice invoice) {
    ObjectMapper mapper = new ObjectMapper(); // Created on every call
    return mapper.writeValueAsString(invoice);
}
```

## After

```java
private static final ObjectMapper MAPPER = new ObjectMapper();

public String generateJson(Invoice invoice) {
    return MAPPER.writeValueAsString(invoice);
}
```

## Default Heavyweight Types

- `ObjectMapper`
- `Gson`
- `XmlMapper`
- `DocumentBuilderFactory`
- `TransformerFactory`

Additional types can be configured in `.algorilla.yml`:

```yaml
heavyweight-types:
  - ObjectMapper
  - Gson
  - JAXBContext
  - MyCustomExpensiveFactory
```

## Suggestion

> Reuse as a static final field or inject via dependency injection
