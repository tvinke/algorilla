# Understanding Output

## Console output format

Findings are grouped by file. Each file header shows the path and finding count:

```
⏺ src/main/java/com/example/shop/service/OrderService.java (2 findings)
```

Each finding then shows:

```
     warning  · nested-lookup · Loop amplifiers · O(orders × items) → O(orders + items)
    com.example.shop.service.OrderService:45

      Linear contains on 'items' inside for-each loop
      → Build a HashSet/Map from 'items' before the loop

          38 │ public List<Order> findMatchingOrders(List<Order> orders, List<String> items) {
             │
          44 │ for (Order order : orders) {
          45 │     if (items.contains(order.getItemId())) {
          46 │         matched.add(order);

      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'items' OrderService.java:45 O(items) ← bottleneck
```

The format breaks down as:

1. **Severity, rule ID, category, and complexity** on the first line
2. **Fully qualified location** (class name and line number)
3. **Description** of the detected pattern
4. **Suggestion** prefixed with `→`
5. **Code snippet** with line numbers showing the relevant context
6. **Evidence chain** with tree markers (`⎿`) tracing the path from outer context to the bottleneck

### Severity levels

- **warning** — likely performance problems worth investigating
- **info** — informational findings that may or may not matter in practice

### Evidence chain

The evidence chain shows the execution path that leads to the finding. The `← bottleneck` annotation marks the innermost expensive operation. Indentation reflects nesting depth:

```
      ⎿  for-each loop over orders OrderService.java:44 O(orders)
        ⎿  contains on 'items' OrderService.java:45 O(items) ← bottleneck
```

For cross-method findings, the chain shows the call path:

```
      ⎿  for-each loop over products ProductService.java:31 O(products)
        ⎿  lookupCategory() inside loop ProductService.java:33 O(categories)
          ⎿  contains on 'categories' CategoryHelper.java:12 O(categories) ← bottleneck
```

### Finding count per evidence entry

When a call is repeated, the evidence shows the count:

```
      ⎿  getStringOrNull() (1st call) OrderView.java:29
      ⎿  getStringOrNull() (duplicate) OrderView.java:31
      ⎿  getStringOrNull() (duplicate) OrderView.java:33
```

### Summary line

Every scan ends with a summary:

```
Scanned 695 files in 1.6s. Found 94 issues (13 warnings, 81 info) across 49 files.
```

## Multiple findings per file

Files with multiple findings show them sequentially under the same file header:

```
⏺ src/main/java/com/example/shop/service/ProductService.java (3 findings)

     warning  · sort-for-last · Sort abuse · O(n log n) → O(n)
    com.example.shop.service.ProductService:67

      Sorting entire collection just to access a single element
      → Use .max(Comparator.comparing(...)) or .min(...) instead of sorting

          65 │ return products.stream()
          66 │         .sorted(Comparator.comparing(Product::getPrice))
          67 │         .findFirst();

      ⎿  sorted call ProductService.java:66 O(n log n) ← bottleneck
        ⎿  .findFirst after sort ProductService.java:67 O(1)

     info  · filter-after-sort · Sort abuse · O(n log n + k) → O(k log k + n)
    com.example.shop.service.ProductService:89

      filter() after sorted() — sorting all elements before filtering
      → Move filter() before sorted() to sort fewer elements

          87 │ return products.stream()
          88 │         .sorted(Comparator.comparing(Product::getRating).reversed())
          89 │         .filter(p -> p.isInStock())
          90 │         .collect(Collectors.toList());

      ⎿  sorted call — sorts all N elements ProductService.java:88 O(n log n) ← bottleneck
        ⎿  filter after sort — discards elements after sorting ProductService.java:89 O(n)

     info  · redundant-expensive-call · Redundancy · O(2x) → O(1x)
    com.example.shop.service.ProductService:112

      calculateDiscount() called 2 times with same arguments in applyPromotions()
      → Cache the result in a local variable

          110 │ if (calculateDiscount(order.getItems()).compareTo(BigDecimal.ZERO) > 0) {
              │
          112 │     applyDiscount(order, calculateDiscount(order.getItems()));
          113 │ }

      ⎿  calculateDiscount() (1st call) ProductService.java:110
      ⎿  calculateDiscount() (duplicate) ProductService.java:112
```

## SARIF output

SARIF (Static Analysis Results Interchange Format) output follows the SARIF 2.1.0 specification and integrates with:

- GitHub Code Scanning
- Visual Studio Code (SARIF Viewer extension)
- Azure DevOps

```bash
java -jar algorilla.jar --format sarif --output results.sarif .
```

## JSON output

Machine-readable JSON output for custom tooling:

```bash
java -jar algorilla.jar --format json --output results.json .
```
