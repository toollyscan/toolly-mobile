# Contributing to Toolly

Thank you for your interest in contributing. Please read this guide before opening an issue or pull request.

---

## Code of conduct

Be respectful and constructive. Harassment of any kind is not tolerated.

---

## Before you start

1. Read the complete [architecture overview](docs/architecture/README.md).
2. Read all [Architecture Decision Records](docs/adr/), including ADR-0008 before dependency changes.
3. Read the [Definition of Done](docs/quality/DEFINITION_OF_DONE.md).
4. Read the [Production Gate](docs/execution/PRODUCTION_GATE.md).
5. Confirm your change is within the scope of the assigned issue.

---

## Architecture constraints

- **Offline-first.** All capture, processing, organisation and local export must work without a network connection.
- **Provider-neutral domain.** The `domain` module must not import Android, iOS, Firebase or AWS types.
- **Canonical identity.** Firebase UID must not become the canonical document-owner ID. Use Toolly-owned canonical IDs.
- **No SDK leakage.** Provider SDK types must not enter shared domain models, DTOs or contracts.
- **Privacy by design.** Document content, OCR text, filenames, phone numbers, email addresses, OTPs, tokens and key material must never appear in logs or analytics.

---

## Pull request process

1. Branch from `main`. Use the naming pattern `<type>/<tly-issue>-short-description`.
2. Complete the pull-request template.
3. Run the documentation workflow locally before opening the PR:

   ```bash
   # Markdown linting
   npx markdownlint-cli2 "**/*.md"

   # Benchmark and dependency governance
   python3 scripts/validate_benchmark_evidence.py --self-test
   python3 scripts/validate_dependency_policy.py --self-test
   ```

4. Self-review every item in the PR template before requesting review.
5. Link the PR to an open issue with `Closes #N`.
6. Do not merge without an approved review from a code owner.

---

## Commit style

Use [Conventional Commits](https://www.conventionalcommits.org/):

```text
<type>(<scope>): <short description>

[optional body]

[optional footer: Closes #N]
```

Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.

---

## Dependency policy

Before adding or updating any library, plugin, native package, GitHub Action or container:

1. Complete [DEPENDENCY_REVIEW_TEMPLATE.md](docs/architecture/DEPENDENCY_REVIEW_TEMPLATE.md).
2. Add/update the machine-readable dependency register and version catalog.
3. Update resolved lock and verification metadata when a build exists.
4. Run the dependency-policy validator and applicable resolved-graph/security checks.
5. Regenerate the SBOM and measure runtime dependency size when applicable.

Commercial scanning, OCR, PDF and image SDKs require a dedicated approved ADR. Unknown/custom licence terms require legal review. Mutable Actions, container tags, dynamic versions and unregistered coordinates are prohibited.

---

## Testing requirements

Every feature or fix must include:

- Unit tests for domain logic.
- Tests for failure, interruption, retry, offline and recovery paths.
- No test may claim a build or test passed unless it was executed in CI.

---

## Questions

Open a [GitHub Discussion](https://github.com/toollyscan/toolly-mobile/discussions) for design questions or open an issue using the appropriate template.
