# Security Policy

## Reporting a vulnerability

If you discover a security vulnerability in algorilla, please report it responsibly.

**Do not open a public issue.** Instead, email ted.vinke@gmail.com with:

- A description of the vulnerability
- Steps to reproduce
- Any relevant logs or output

I'll acknowledge receipt within 48 hours and work with you on a fix before any public disclosure.

## Scope

Algorilla is a static analysis tool that reads source files — it does not execute analyzed code, access networks, or modify files outside its cache directory. Security concerns are most likely to involve:

- Path traversal in file scanning
- Denial of service via crafted input files
- Dependency vulnerabilities
