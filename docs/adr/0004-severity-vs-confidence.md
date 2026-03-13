# ADR-0004: Severity vs Confidence — Two Dimensions of Finding Quality

## Status

Accepted

## Context

Algorilla findings have a single quality axis: severity (INFO / WARNING / ERROR). Severity expresses
how impactful an anti-pattern is IF it's a true positive — an O(n²) nested lookup is a WARNING
because it matters at scale, while a redundant-but-cheap call is INFO because the impact is minor.

But severity doesn't capture how confident Algorilla is that a finding is correct. A nested-lookup
finding where the target variable has a resolved type declaration (`List<String>`) is much more
trustworthy than one where the type is unknown (it could be a HashSet, making the finding a false
positive). Both get the same WARNING severity today.

This conflation forces a tradeoff: either show everything (noisy) or demote uncertain findings to
INFO (hiding the severity signal). Neither serves the user well.

## Decision

Add a **Confidence** dimension (HIGH / MEDIUM / LOW) orthogonal to Severity.

- **Severity** = "How bad is this pattern if it's real?" Set per rule based on the anti-pattern category.
- **Confidence** = "How sure are we this instance is real?" Set per finding based on available evidence.

Confidence signals include:
- **Type resolution**: Was the target variable's type resolved from declarations? (HIGH signal)
- **Structural certainty**: Is the pattern unambiguous regardless of types? E.g., string concat in a
  for-loop is always real. (HIGH signal)
- **Cross-method inference**: Was the finding discovered by following calls into other functions?
  (MEDIUM signal — the called function might not behave as inferred)
- **Name-based heuristic**: Was the detection based purely on method/variable names without type proof?
  (LOW signal)

### CLI behavior

- `--confidence high` shows only high-confidence findings
- `--confidence medium` shows medium and high (default)
- `--confidence low` shows everything

### Pipeline model: confidence + severity + fail-on

Findings flow through a two-stage filter:
1. **`--confidence`** filters which findings are visible (trust gate)
2. **`--severity`** filters by severity on the visible set
3. **`--fail-on`** checks severity on the remaining set (action trigger)

### Not affected

- **Baseline fingerprints**: A finding's identity doesn't change when its confidence changes.
  The same finding at HIGH or LOW confidence produces the same fingerprint.

## Consequences

- Users see fewer, more trustworthy findings by default — better first impression.
- Rules that rely on heuristics aren't penalized with lower severity — they keep their correct
  severity but get lower confidence, which is the honest signal.
- The engine can improve confidence over time (better type inference = more HIGH findings)
  without changing rule logic.
- JSON output gains a `confidence` field (additive, backward-compatible).
- SARIF output maps confidence to the `rank` property on results (0-100 scale).
