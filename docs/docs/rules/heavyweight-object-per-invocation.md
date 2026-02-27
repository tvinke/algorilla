# Heavyweight Object per Invocation

**Rule ID:** `heavyweight-object-per-invocation` · **Severity:** WARNING

## Description

Detects creation of heavyweight objects (like `ObjectMapper`, `Gson`, `DocumentBuilderFactory`) inside method bodies. These objects are expensive to construct and should typically be reused as static fields or injected dependencies.

## Bad Example

```java
public String serialize(Order order) {
    ObjectMapper mapper = new ObjectMapper(); // Created on every call
    return mapper.writeValueAsString(order);
}
```

## Good Example

```java
private static final ObjectMapper MAPPER = new ObjectMapper();

public String serialize(Order order) {
    return MAPPER.writeValueAsString(order);
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
