# Toolly Copilot Instructions

These instructions apply to every Copilot suggestion and agent session in this repository.

---

## Before implementing anything

1. Read the complete issue, including all comments.
2. Read the relevant product documents in `docs/product/`.
3. Read `docs/architecture/README.md`.
4. Read all ADRs in `docs/adr/`.
5. Read the [Definition of Done](docs/quality/DEFINITION_OF_DONE.md).
6. Read the [Production Gate](docs/execution/PRODUCTION_GATE.md).
7. Confirm the change is within the scope of the assigned issue before writing any code.

---

## Scope discipline

- Implement only the scope described in the assigned issue. Do not add unrelated refactoring or generic boilerplate.
- Do not copy code from the previous Toolly repository or any external repository.
- Do not add Gradle modules, Android, iOS, Firebase or AWS code unless the issue explicitly requires it and the Production Gate is approved.
- Do not introduce commercial scanning, OCR, PDF or image-processing SDKs without an approved Architecture Decision Record.

---

## Architecture constraints

- **Domain independence.** The `domain` module must not import Android, iOS, Firebase or AWS types. Keep domain code independent of all infrastructure providers.
- **Provider-neutral.** Firebase UID must not become the canonical document-owner ID. Canonical IDs belong to Toolly.
- **No SDK leakage.** Provider SDK types must not appear in shared domain models, DTOs, contracts or schemas.
- **Offline-first.** All capture, processing, organisation and local export must work without a network connection.
- **Clean Architecture.** Dependencies flow inward: `presentation → domain ← data`. Never reverse this direction.

---

## Privacy and security rules

Never log or send to analytics:

- Document content or page images.
- OCR text or recognised data.
- Filenames or document metadata.
- Phone numbers, email addresses or any PII.
- OTPs, authentication tokens or session identifiers.
- Encryption keys, key material or derived secrets.

---

## Dependency policy

Before introducing any new dependency:

1. Record the licence and confirm compatibility.
2. Check the security record for known CVEs.
3. Estimate the binary-size impact.
4. Document the removal plan if the dependency becomes abandoned.

Never add a dependency without completing all four steps. Never add a commercial SDK without an approved ADR.

---

## Testing and verification

- Include tests for failure, interruption, retry, offline and recovery paths — not only the happy path.
- Never claim a build or test passed unless it was executed in CI or documented locally with the exact command and output.
- Do not disable or remove unrelated tests.

---

## Documentation

- Update `docs/product/DECISION_REGISTER.md` when a product or architecture decision is made or reversed.
- Update the relevant ADR or create a new ADR when a contract, schema or architectural decision changes.
- Update `CONTRIBUTING.md` when the development process changes.

---

## Abstractions to avoid

- Do not create generic `BaseActivity`, `BaseViewModel` or `BaseRepository` superclasses.
- Prefer composition and interfaces over deep inheritance hierarchies.
- Name types by their domain role, not their layer (e.g., `DocumentRepository`, not `BaseRepository`).

---

## Commit and PR standards

- Use [Conventional Commits](https://www.conventionalcommits.org/).
- Complete every item in the pull-request template before requesting review.
- Link every PR to an open issue.
