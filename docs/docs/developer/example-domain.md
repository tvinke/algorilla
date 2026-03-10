# Example Domain

All documentation examples and rule-level tests use a single **e-commerce order processing** domain. This keeps examples consistent and immediately recognizable to enterprise developers.

For guidelines on how to write examples, admonitions, and other documentation conventions, see [Documentation Guidelines](documentation-guidelines.md).

## Domain classes

| Class | Key fields |
|-------|-----------|
| `Order` | id, customerId, status, orderDate (ISO string), lineItems, total |
| `LineItem` | id, orderId, productId, quantity, unitPrice |
| `Payment` | id, orderId, amount, method, status, transactionRef |
| `Invoice` | id, orderId, issuedDate, dueDate (ISO string), total |
| `Product` | id, name, category, price |
| `Customer` | id, name, email, region |

## Services and repositories

| Class | Purpose |
|-------|---------|
| `OrderService` / `OrderRepository` | Core order CRUD and business logic |
| `PaymentService` / `PaymentRepository` | Payment processing and lookups |
| `ProductService` / `ProductRepository` | Product catalog |
| `InvoiceService` | Invoice generation (JSON serialization examples) |
| `DiscountService` | Discount eligibility checks (replaces generic "eligibility" examples) |
| `DiscountRule` | Individual discount rule that checks category eligibility |
| `PromotionRepository` / `PromotionService` | Promotion data when needed |
| `CustomerRepository` | Customer lookups |

Additional services like `ShippingService` or `RefundRepository` may appear where a specific rule example needs them — the constraint is staying within the orders & payments domain.

## Relationships

```
Customer ──1:n──▶ Order ──1:n──▶ LineItem
                    │
                    ├──1:1──▶ Payment
                    └──1:1──▶ Invoice

Product ◀── referenced by LineItem
DiscountRule ──▶ checks Order/Product category eligibility
```

## Conventions

- **Status values**: PENDING, PAID, SHIPPED, CANCELLED (for Order); PENDING, COMPLETED, FAILED (for Payment)
- **Payment methods**: CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PAYPAL
- **Date fields**: ISO 8601 strings (e.g. `"2024-03-15"`) — used in sort comparator examples to show parsing cost
- **Transaction refs**: Pattern `INV-\d{4}-\d{6}` — used in regex compilation examples

## Use in tests

**Rule-level tests** (the ones that verify "does this rule detect the pattern?") should use the same domain model. This way the test fixtures mirror the documentation — a contributor reading a rule page and then looking at its test sees the same `Order`, `DiscountService`, `PaymentRepository` classes.

**Parser and IR-level tests** don't need to follow this convention. Those test specific syntax constructs, edge cases, and boundary conditions where a minimal `Foo`/`Bar`/`items` example is clearer. Forcing domain context there would add noise without helping readability.

## Why a single domain?

A unified domain makes the documentation and rule tests feel cohesive. Readers build mental context once and carry it across all rule pages, the Big-O primer, the hidden-complexity walkthrough, and the output format reference. It also makes it easier for contributors to write new examples that fit naturally.

When writing new documentation, rule examples, or rule tests, use classes from this domain model. If you need an entity that doesn't exist yet, extend the model in a way that stays within the e-commerce order processing context.
