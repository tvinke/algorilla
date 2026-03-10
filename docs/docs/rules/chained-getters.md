# Chained Getters

**Rule ID:** `chained-getters` · **Severity:** INFO · **Complexity:** O(n^k) → O(n)

## Description

Detects cascading getter patterns where the result of one lookup feeds into another. If each getter performs a linear scan, the chain multiplies to O(n^k) where k is the chain length.

## Bad Example

```java
public String getCustomerName(Long orderId) {
    Order order = findOrder(orderId);                       // O(n)
    Payment payment = findPayment(order.getPaymentId());    // O(n), depends on result above
    Customer customer = findCustomer(payment.getCustomerId()); // O(n), depends on result above
    return customer.getName();
}
```

## Good Example

```java
// Pre-build lookup maps
Map<Long, Order> orderMap = buildOrderMap();
Map<Long, Payment> paymentMap = buildPaymentMap();
Map<Long, Customer> customerMap = buildCustomerMap();

public String getCustomerName(Long orderId) {
    Order order = orderMap.get(orderId);                    // O(1)
    Payment payment = paymentMap.get(order.getPaymentId()); // O(1)
    Customer customer = customerMap.get(payment.getCustomerId()); // O(1)
    return customer.getName();
}
```

## How Detection Works

1. Identifies variables initialized by getter-style calls (`get*`, `find*`, `load*`, `fetch*`)
2. Builds chains where a getter's argument references a variable produced by another getter
3. Flags chains of length 2 or more
4. Validates that at least one getter in the chain involves linear lookups

## Suggestion

> Pre-build lookup maps or use a join query to avoid cascading lookups

## Related Rules

- [Uncached Getter](uncached-getter.md) — same getter called multiple times with the same argument
- [N+1 Query](n-plus-one-query.md) — single-record fetch inside a loop
