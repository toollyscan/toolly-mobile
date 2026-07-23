# Definition of Done

A pull request may not be merged unless every applicable item in this checklist is satisfied. Non-applicable checks must be marked N/A with a short reason; they must never be represented as executed or passed.

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

- [ ] Unit tests cover the changed domain logic, or N/A is justified for documentation-only work.
- [ ] Tests cover failure, interruption, retry, offline and recovery paths where executable behavior changes.
- [ ] All applicable existing tests continue to pass.
- [ ] Test results are linked or attached; unexecuted checks are never marked passed.
- [ ] Benchmark-related changes pass `python3 scripts/validate_benchmark_evidence.py --self-test`.
- [ ] Any benchmark claim links raw measurements, run manifest, exact commit, corpus version and environment; summary-only evidence is rejected.

## Documentation

- [ ] `DECISION_REGISTER.md` is updated if a product or architecture decision was made or reversed.
- [ ] The relevant ADR is created or updated if a contract or schema changed.
- [ ] `CONTRIBUTING.md` is updated if the development process changed.
- [ ] README links are valid.
- [ ] Markdown linting passes.
- [ ] If design-related: Figma frame reference, screen ID and component name are included in the PR description.

## Review

- [ ] All items in the pull-request template are completed.
- [ ] At least one code-owner approval has been received.
- [ ] All review comments are resolved or explicitly deferred with a linked issue.

---

## Production-only additional criteria

For PRs that implement production features (after the Production Gate is approved):

- [ ] Accepted raw benchmarks on the approved representative physical-device matrix meet evidence-backed thresholds in `BENCHMARK_PLAN.md`.
- [ ] Accessibility requirements are met (minimum WCAG 2.1 AA; TalkBack / VoiceOver).
- [ ] English, Hindi and Kannada strings are complete.
- [ ] The `staging` environment deployment is verified before merging to `main`.
- [ ] Figma G4 (screen coverage) and G9 (developer handoff) are approved for the implemented screen.
- [ ] Component names in code match the canonical names in `COMPONENT_INVENTORY.md`.
- [ ] Design tokens are referenced by semantic name; no hardcoded hex values in UI code.
