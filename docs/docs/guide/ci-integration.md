# CI/CD Integration

Run Algorilla as part of your pipeline to catch algorithmic complexity issues before they reach production.

**Exit codes:** `0` = clean, `1` = findings detected, `2` = analysis error.

## GitHub Actions

### Using the Algorilla action (recommended)

The simplest way to add algorilla to a GitHub Actions workflow:

```yaml
# .github/workflows/algorilla.yml
name: Algorilla

on:
  pull_request:
    branches: [main]

jobs:
  algorilla:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: tvinke/algorilla@v0.2.0
        with:
          paths: '.'
          severity: 'warning'
          fail-on: 'error'
```

This automatically downloads algorilla, runs the analysis, and uploads SARIF results to GitHub Code Scanning — findings show up as annotations on the PR diff.

!!! tip "Version pinning"
    Pin to an exact version (`@v0.2.0`) for reproducible builds. You can also use the floating `@v0` tag to track the latest 0.x release automatically, but this means your CI may pick up new rules or behavior changes without warning.

#### Action inputs

| Input | Description | Default |
|-------|-------------|---------|
| `paths` | Directories to scan (space-separated) | `.` |
| `format` | Output format: `console`, `json`, `sarif` | `sarif` |
| `severity` | Minimum severity to report | `warning` |
| `fail-on` | Minimum severity to fail the build | `warning` |
| `rules` | Comma-separated rule IDs (empty = all) | all |
| `baseline` | Path to baseline file | none |
| `language` | Comma-separated languages to scan | all |
| `version` | Algorilla version to use | `latest` |

### With baseline (PR-only findings)

To only flag findings introduced in the PR, maintain a baseline on `main`:

```yaml
      - uses: tvinke/algorilla@v0.2.0
        with:
          paths: '.'
          baseline: '.algorilla/baseline.json'
```

See [Baseline Workflow](#baseline-workflow) below for how to keep the baseline up to date.

### Manual setup

If you prefer to manage the JAR yourself:

```yaml
jobs:
  algorilla:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Run Algorilla
        run: |
          curl -sL https://github.com/tvinke/algorilla/releases/latest/download/algorilla-0.2.0.jar -o algorilla.jar
          java -jar algorilla.jar \
            --no-cache \
            --format sarif \
            --output results.sarif \
            .

      - name: Upload SARIF
        if: always()
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: results.sarif
```

!!! note
    The `if: always()` on the upload step is important — Algorilla exits with `1` when it finds issues, which would skip the upload otherwise.

## GitLab CI

```yaml
# .gitlab-ci.yml
algorilla:
  stage: test
  image: ghcr.io/tvinke/algorilla:latest
  script:
    - algorilla
        --no-cache
        --format json
        --output algorilla-report.json
        .
  artifacts:
    paths:
      - algorilla-report.json
    when: always
  allow_failure:
    exit_codes:
      - 1
```

Setting `allow_failure` for exit code `1` lets the pipeline continue when findings exist, while still failing on actual errors (exit code `2`).

Alternatively, use `npx`:

```yaml
algorilla:
  stage: test
  image: node:20
  script:
    - npx algorilla --no-cache --format json --output algorilla-report.json .
```

## Baseline Workflow

Baselines let you avoid drowning in existing findings and focus on what the PR introduces.

### 1. Generate baseline on main

Run this once (or in a scheduled job) on your default branch:

```bash
algorilla --save-baseline .algorilla/baseline.json .
```

Commit `.algorilla/baseline.json` to the repo.

### 2. Compare in PRs

Pass `--baseline` in your CI job so only new findings are reported:

```bash
algorilla \
  --baseline .algorilla/baseline.json \
  --format sarif \
  --output results.sarif \
  .
```

The exit code will be `0` if all findings were already in the baseline.

### 3. Refresh after fixes

When you fix findings or intentionally accept them, regenerate:

```bash
algorilla --save-baseline .algorilla/baseline.json .
git add .algorilla/baseline.json
git commit -m "update algorilla baseline"
```

## Tips

**Use `--no-cache` in CI.** CI runners typically don't preserve the `.algorilla/cache/` directory between runs, so the cache check is wasted work. Skip it.

**Focus on specific languages** with `--language` if your repo is multi-language but you only care about a subset:

```bash
algorilla --no-cache --language java,kotlin .
```

**Control noise with `--severity`.** Start with `--severity error` to surface only the worst offenders, then lower it as you clean up:

```bash
algorilla --no-cache --severity error .
```

**Fail fast on errors.** If your CI treats any non-zero exit as failure, wrap the command to distinguish findings from errors:

```bash
algorilla --no-cache . || [ $? -eq 1 ]
```

This passes when exit code is `0` (clean) or `1` (findings), but fails on `2` (error).
