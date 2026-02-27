# Quick Start

## Scan a Project

Point Algorilla at your project root:

```bash
java -jar algorilla.jar /path/to/your/project
```

Algorilla automatically detects the project structure, scans source files, and excludes test code and build output.

## Scan Specific Files

```bash
java -jar algorilla.jar src/main/java/com/example/OrderService.java
```

## Output Formats

Console output (default):

```bash
java -jar algorilla.jar .
```

SARIF for CI/CD integration:

```bash
java -jar algorilla.jar --format sarif --output results.sarif .
```

JSON for programmatic processing:

```bash
java -jar algorilla.jar --format json --output results.json .
```

## Common Options

```bash
# Show only errors (skip warnings)
java -jar algorilla.jar --severity error .

# Exclude specific directories
java -jar algorilla.jar --exclude "**/generated/**" .

# Include test code in analysis
java -jar algorilla.jar --include-tests .

# Verbose output for debugging
java -jar algorilla.jar -v .
```

## Exit Codes

| Code | Meaning |
|------|---------|
| 0 | No findings |
| 1 | Findings detected |
| 2 | Analysis error |
