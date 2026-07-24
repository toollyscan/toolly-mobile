# Release Evidence Template

Complete this template for every production release.  Attach the completed
document to the release PR and link it from the GitHub Release.

Release evidence is required by the Production Gate before any build that
processes real user data or connects to the production Firebase project is
merged to `main`.

---

## Release metadata

| Field | Value |
|-------|-------|
| Release version | `vMAJOR.MINOR.PATCH` |
| Git tag | |
| Release commit SHA | |
| Release date | |
| Release engineer | |
| Production Gate approval | Linked issue: |

## CI evidence

| Check | Status | Run link |
|-------|--------|----------|
| `markdown-lint` | | |
| `benchmark-contracts` | | |
| `dependency-policy` | | |
| `firebase-governance` | | |
| `ci-trust-policy` | | |
| `secret-scan` | | |

All checks must show **passed** on the release commit.  Link each run.

## Dependency evidence

- [ ] `validate_dependency_policy.py --self-test` passed on the release commit.
- [ ] Resolved transitive SBOM is attached or linked.
- [ ] No unapproved dependency was introduced since the last release.

## Secret-scan evidence

- [ ] `scan_secrets.py` reports zero unexcepted findings on the release commit.
- [ ] Full history was scanned (`--no-history` was not used).

## Signing evidence

- [ ] Release artifact is signed (Android `.aab` / iOS `.ipa`).
- [ ] Signing key identity is recorded (key alias, certificate fingerprint — no key material).
- [ ] Signing was performed in the approved CI environment, not on a developer machine.

## Firebase environment evidence

- [ ] Release build is bound to the production Firebase project only.
- [ ] Staging and development project bindings are absent from the release artifact.
- [ ] Firebase service-boundaries.json has been reviewed and matches the release.

## Manual review

- [ ] The release changelog has been reviewed for accuracy.
- [ ] No TODO/FIXME comments without linked issues are present in changed files.
- [ ] A rollback plan is documented in `docs/operations/RELEASE_AND_ROLLBACK.md`
      and is executable for this release.

## Approvals

| Role | Name | Date |
|------|------|------|
| Release engineer | | |
| Code owner | | |
| Security reviewer (SEV gate) | | |

---

*Template version: 1.0 | Owner: shivayogih*
