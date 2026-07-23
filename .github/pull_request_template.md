## Summary

<!-- Describe what this PR does and why. One paragraph is usually enough. -->

Closes #

---

## Type of change

- [ ] Documentation / governance
- [ ] Architecture Decision Record (ADR)
- [ ] Bug fix
- [ ] New feature
- [ ] Refactoring (no behaviour change)
- [ ] CI / tooling

---

## Pre-merge checklist

### Scope

- [ ] The change implements only the scope described in the linked issue.
- [ ] No unrelated refactoring or boilerplate is included.
- [ ] No Android, iOS, Firebase or AWS code is added unless the Production Gate is approved and the issue requires it.

### Architecture

- [ ] Domain modules do not import Android, iOS, Firebase or AWS types.
- [ ] No provider SDK type appears in shared domain models or DTOs.
- [ ] Canonical IDs belong to Toolly, not to a provider.
- [ ] Offline-first paths are preserved.

### Privacy and security

- [ ] No document content, OCR text, filenames, PII, OTPs, tokens or key material appears in logs or analytics.
- [ ] No secrets, credentials or personal data are committed.

### Dependencies

- [ ] Any new dependency has a documented licence, security check, binary-size estimate and removal plan.
- [ ] No commercial scanning/OCR/PDF SDK is introduced without an approved ADR.

### Tests

- [ ] Failure, interruption, retry, offline and recovery paths are tested.
- [ ] All existing tests continue to pass.
- [ ] Test results are attached or linked (not self-certified without evidence).

### Documentation

- [ ] DECISION_REGISTER.md is updated if a product or architecture decision was made.
- [ ] The relevant ADR is created or updated if a contract or schema changed.
- [ ] README links are valid.

### CI

- [ ] The documentation workflow (Markdown lint + secret scan) passes.

---

## Validation performed

<!-- List the exact commands run and attach or link the output. -->

```bash
# Example
npx markdownlint-cli2 "**/*.md"
```

---

## Screenshots / diagrams

<!-- Add if applicable. -->
