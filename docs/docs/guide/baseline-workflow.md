# Baseline Workflow

Baselines let you acknowledge existing findings and only report new ones. This is useful for gradually adopting Algorilla in a large codebase.

## Save a Baseline

```bash
algorilla --save-baseline .algorilla/baseline.json .
```

This saves fingerprints of all current findings. The fingerprints are based on file path, rule ID, and message content — making them resilient to minor line number shifts.

## Run with Baseline

```bash
algorilla --baseline .algorilla/baseline.json .
```

Only findings not present in the baseline are reported. The exit code is 0 if all findings are baseline-known.

## CI Integration Pattern

```yaml
# .github/workflows/algorilla.yml
- uses: tvinke/algorilla@v0.2.0
  with:
    paths: '.'
    baseline: '.algorilla/baseline.json'
```

Or manually:

```bash
algorilla \
  --baseline .algorilla/baseline.json \
  --format sarif \
  --output results.sarif \
  .
```

## Updating the Baseline

After fixing or accepting findings:

```bash
algorilla --save-baseline .algorilla/baseline.json .
```

Commit the updated baseline file alongside your code changes.
