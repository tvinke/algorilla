# Workflow

This page walks through the typical workflow of using algorilla on a project: scan, triage, fix or accept, repeat.

## 1. Scan your project

Point algorilla at your project root. It auto-detects the build system and source directories:

```bash
algorilla .
```

For a first run on a large codebase, start with warnings only:

```bash
algorilla --severity warning .
```

Or focus on a single rule:

```bash
algorilla --rule nested-lookup .
```

!!! tip "List available rules"
    Run `algorilla --list-rules` to see all built-in rules with their severity, category, and supported languages.

## 2. Read the output

Findings are grouped by file and sorted by severity (errors first, then warnings, then info). Each finding shows:

- The **severity** and **rule ID**
- The **source location** and a **code snippet**
- A **description** of what was detected
- A **suggestion** for how to fix it
- An **evidence chain** tracing the path to the bottleneck
- A **fingerprint hash** (the `# abcdef1234567890` at the end) used for accepting findings

See [Understanding Output](understanding-output.md) for a detailed breakdown.

## 3. Triage: is this real?

Not every finding needs a fix. Look at:

- **The evidence chain.** Does the loop actually iterate over a large collection? A `contains()` call on a 3-element list is harmless.
- **The execution context.** Is this code on a hot path, or a one-time startup routine?
- **The suggestion.** Does it make sense for your codebase?

## 4. Fix, suppress, or accept

### Fix it

Apply the suggestion, then re-run algorilla. The finding disappears:

```bash
# fix the code, then:
algorilla .
```

Caching makes re-scans fast — only changed files are re-analyzed.

### Suppress inline

If a finding is wrong for a specific line (e.g. the collection is known to be small), suppress it with a comment:

```java
// algorilla:ignore nested-lookup
if (smallList.contains(value)) { ... }
```

See [Suppression](suppression.md) for all options.

### Accept it (ignore list)

If a finding is valid code that you've reviewed and decided is acceptable, accept it by fingerprint. Each finding shows a fingerprint hash in the output:

```
      ⎿  contains on 'items' OrderService.java:45 O(items) ← bottleneck
      # a1b2c3d4e5f6g7h8
```

Accept one or more findings:

```bash
algorilla --accept a1b2c3d4e5f6g7h8 .
```

This adds the finding to `.algorilla/ignore-list.json`. On subsequent runs, accepted findings are hidden automatically. The ignore list is project-local and can be committed to version control.

The difference between suppress and accept:

| | Inline suppress | Accept (ignore list) |
|---|---|---|
| Where | Comment in source code | `.algorilla/ignore-list.json` |
| Scope | Specific line | Finding by content fingerprint |
| Survives line shifts | No (tied to line number) | Yes (content-based matching) |
| Visible in code | Yes | No |

### Disable a rule entirely

If a rule consistently produces findings that aren't relevant to your project:

```yaml
# .algorilla.yml
rules:
  repeated-reflection-in-loop:
    enabled: false
```

## 5. Ongoing use

### Track progress

On a legacy codebase, save a baseline so you only see new findings going forward:

```bash
# on main branch:
algorilla --save-baseline .algorilla/baseline.json .

# on a feature branch:
algorilla --baseline .algorilla/baseline.json .
```

See [Baseline Workflow](baseline-workflow.md) for details.

### CI integration

Use `--fail-on` to control when algorilla fails a build:

```bash
# fail only on errors (warnings are advisory)
algorilla --fail-on error --format sarif -o results.sarif .

# fail on warnings and errors (strict)
algorilla --fail-on warning --format sarif -o results.sarif .
```

See [CI Integration](ci-integration.md) for GitHub Actions and GitLab CI examples.

## Quick reference

| Task | Command |
|---|---|
| Scan project | `algorilla .` |
| Warnings only | `algorilla --severity warning .` |
| Single rule | `algorilla --rule nested-lookup .` |
| List rules | `algorilla --list-rules` |
| Accept a finding | `algorilla --accept <hash> .` |
| Save baseline | `algorilla --save-baseline .algorilla/baseline.json .` |
| Compare to baseline | `algorilla --baseline .algorilla/baseline.json .` |
| CI (fail on errors) | `algorilla --fail-on error --format sarif -o results.sarif .` |
