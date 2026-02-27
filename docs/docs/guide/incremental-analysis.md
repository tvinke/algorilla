# Incremental Analysis

Algorilla caches analysis results per file using content hashing. On subsequent runs, only files whose content has changed are re-analyzed.

## How It Works

1. First run: all files are parsed and analyzed. Results are cached to `.algorilla/analysis-cache.json`
2. Subsequent runs: file content hashes are compared. Only changed files are re-parsed
3. Cached findings from unchanged files are included in the output without re-analysis

## Cache Location

The cache is stored at `.algorilla/analysis-cache.json` relative to the scan root directory.

## Disabling the Cache

```bash
java -jar algorilla.jar --no-cache .
```

## Performance Impact

Typical improvement on a ~1500-file codebase:

| Run | Time |
|-----|------|
| First (no cache) | ~1.4s |
| Second (cached) | ~0.1s |

## Cache Invalidation

The cache is automatically invalidated when:

- A file's content changes (SHA-256 hash mismatch)
- The cache file is corrupted or from an incompatible version
- `--no-cache` is specified

To manually clear the cache, delete the `.algorilla/` directory.
