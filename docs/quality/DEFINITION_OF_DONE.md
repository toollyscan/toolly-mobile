# Definition of Done

A pull request may not be merged unless every applicable item in this checklist is satisfied.

---

## Scope

- [ ] The change implements only the scope described in the linked issue.
- [ ] The linked issue is referenced in the PR description (`Closes #N`).
- [ ] No unrelated refactoring or generic boilerplate is included.

## Architecture

- [ ] Domain modules have no dependency on Android, iOS, Firebase or AWS types.
- [ ] No provider SDK type appears in domain models, use cases or repository interfaces.
- [ ] Canonical IDs belong to Toolly.
- [ ] Offline-first paths are preserved: capture, processing, organisation and local export work without a network connection.
- [ ] Clean Architecture direction is maintained: `presentation → domain ← data`.

## Privacy and security

- [ ] Document content, OCR text, filenames, PII, OTPs, tokens and key material do not appear in logs or analytics.
- [ ] No secrets, credentials or personal data are committed.
- [ ] Gitleaks secret scan passes.

## Dependency policy

- [ ] Any new dependency has a documented licence, security check, binary-size estimate and removal plan.
- [ ] No commercial scanning/OCR/PDF SDK is introduced without an approved ADR.

## Code quality

- [ ] The code compiles without warnings in the CI environment.
- [ ] No generic `BaseActivity`, `BaseViewModel` or `BaseRepository` superclasses are introduced.
- [ ] No `TODO` or `FIXME` comments are left without a linked issue.

## Tests

- [ ] Unit tests cover the changed domain logic.
- [ ] Tests cover failure, interruption, retry, offline and recovery paths.
- [ ] All existing tests continue to pass.
- [ ] Test results are linked or attached (not self-certified without evidence).

## Documentation

- [ ] `DECISION_REGISTER.md` is updated if a product or architecture decision was made or reversed.
- [ ] The relevant ADR is created or updated if a contract or schema changed.
- [ ] `CONTRIBUTING.md` is updated if the development process changed.
- [ ] README links are valid.
- [ ] Markdown linting passes.

## Design

For PRs that implement a UI screen or component:

- [ ] The screen or component has a corresponding entry in [SCREEN_INVENTORY.md](../design/SCREEN_INVENTORY.md) or [COMPONENT_INVENTORY.md](../design/COMPONENT_INVENTORY.md).
- [ ] The Figma Completion Gate has been passed for this screen or component (see [FIGMA_COMPLETION_GATE.md](../design/FIGMA_COMPLETION_GATE.md)).
- [ ] Design tokens are used; no hardcoded colour, spacing or typography values.
- [ ] All required component states from [COMPONENT_STATE_MATRIX.md](../design/COMPONENT_STATE_MATRIX.md) are implemented.
- [ ] Accessibility labels and focus order match the annotations in the Figma frame (see [ACCESSIBILITY_REQUIREMENTS.md](../design/ACCESSIBILITY_REQUIREMENTS.md)).
- [ ] All strings use externalised string resources; no hardcoded UI strings in code (see [LOCALIZATION_REQUIREMENTS.md](../design/LOCALIZATION_REQUIREMENTS.md)).

## Review

- [ ] All items in the pull-request template are completed.
- [ ] At least one code-owner approval has been received.
- [ ] All review comments are resolved or explicitly deferred with a linked issue.

---

## Production-only additional criteria

For PRs that implement production features (after the Production Gate is approved):

- [ ] Performance benchmarks on representative devices meet targets in `BENCHMARK_PLAN.md`.
- [ ] Accessibility requirements are met (minimum WCAG 2.1 AA; TalkBack / VoiceOver).
- [ ] English, Hindi and Kannada strings are complete.
- [ ] The `staging` environment deployment is verified before merging to `main`.
