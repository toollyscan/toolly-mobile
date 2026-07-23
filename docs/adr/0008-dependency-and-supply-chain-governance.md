# ADR-0008 — Dependency and Supply-Chain Governance

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2026-07-23 |
| Author | shivayogih |

## Context

Toolly will process sensitive documents on Android and iOS. Camera, image, PDF, OCR, cryptography, Firebase and billing dependencies can change privacy, security, binary size, cost, portability and product reliability. A version catalog alone does not approve a dependency or guarantee the resolved version.

The repository currently has CI tooling but no Gradle/KMP application scaffold. Governance must work now and fail closed when build manifests arrive.

## Decision

1. Every direct runtime, build, plugin, native, CI Action and container dependency is recorded in `config/dependencies/registry.json`.
2. An entry is approved only after purpose, alternatives, owner, licence evidence, vulnerability posture, transitive scope, size, data processing and removal plan are reviewed.
3. Gradle coordinates and plugin versions use `gradle/libs.versions.toml`; direct version literals are prohibited except reviewed build-tool bootstrap cases.
4. Version catalogs centralize requested versions but do not replace resolved-graph locking or verification.
5. Gradle builds enable strict dependency locking and commit generated lock state.
6. Gradle dependency verification uses reviewed SHA-256 metadata and signatures where independently verifiable. Generated verification metadata is never accepted without reviewing its diff.
7. Dynamic, changing, snapshot, local, flat-directory, JCenter and unapproved repositories are prohibited.
8. GitHub Actions use full commit SHAs and container actions use immutable image digests.
9. Release artifacts require a resolved transitive SBOM and build provenance. The governance-registry SBOM is a preview and cannot substitute for a resolved build SBOM.
10. Firebase remains behind Toolly-owned adapters. AWS SDKs are prohibited in the current phase.
11. Commercial scanner, OCR, PDF or image SDKs require a dedicated ADR, legal/licence review, privacy/data-flow review, measured size/performance, cost model and tested removal path.
12. Vulnerability exceptions require an owner, compensating control, expiry and linked issue. Critical or known-exploited risk cannot be silently waived.

## Consequences

Positive:

- dependency changes are explicit and reviewable;
- mutable CI references cannot silently change;
- provider and platform leakage is detected before modules mature;
- Firebase-to-future-provider portability remains enforceable;
- release SBOM and provenance have defined gates.

Costs:

- dependency upgrades require register, lock and verification updates;
- false positives need narrow, expiring exceptions;
- SBOM/attestation availability depends on the eventual build and GitHub plan;
- native and commercial SDK reviews require specialist input.

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Dependabot alone | Finds some updates but does not approve licences, privacy, size or architecture |
| Version catalog alone | Does not enforce resolved versions or artifact integrity |
| Trust all Maven Central/Google artifacts | Repository trust does not approve each package or prevent artifact replacement |
| Pin only direct dependencies | Transitive graph and build plugins remain mutable/unreviewed |
| Enable GitHub paid features without checking availability | Private-repository plan support must be verified first |
| Permit AWS dependencies for future readiness | Violates the Firebase-now, AWS-evaluation-later decision |

## Evidence

- `scripts/validate_dependency_policy.py --self-test`;
- pinned CI workflow references;
- machine-readable registry and policy;
- intentionally invalid self-test cases;
- CI dependency-policy job;
- release SBOM/provenance evidence after application scaffolding.

## References

- [Gradle version catalogs](https://docs.gradle.org/current/userguide/version_catalogs.html)
- [Gradle dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html)
- [Gradle dependency verification](https://docs.gradle.org/current/userguide/dependency_verification.html)
- [GitHub supply-chain security](https://docs.github.com/en/code-security/concepts/supply-chain-security/supply-chain-security)
- [GitHub artifact attestations](https://docs.github.com/en/actions/concepts/security/artifact-attestations)
- [SLSA build provenance](https://slsa.dev/spec/v1.2/build-provenance)
