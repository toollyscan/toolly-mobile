# SBOM and Build Provenance

## Objectives

For every release candidate, Toolly must be able to answer:

- exactly which source commit and workflow produced the artifact;
- which direct and transitive components were resolved;
- which artifact digest was reviewed and distributed;
- whether the SBOM/provenance verifies independently;
- which dependency approval and vulnerability state applied.

## SBOM stages

| Stage | Output | Authority |
|-------|--------|-----------|
| Governance | CycloneDX preview generated from the dependency register | Shows approved direct/tooling inventory only |
| Build | CycloneDX JSON generated from resolved Gradle/native graphs | Required transitive release inventory |
| Release | SBOM bound to AAB/IPA/backend artifact digest | Production evidence |

The governance preview carries an explicit incomplete-transitives property and cannot satisfy a release gate.

Generate the current preview:

```bash
python3 scripts/validate_dependency_policy.py \
  --emit-sbom /tmp/toolly-governance.cdx.json
```

## Required release SBOM fields

- CycloneDX specification and serial/version;
- application component, version and commit;
- direct and transitive components with package URL where possible;
- resolved version, hashes, licence expression and supplier/source;
- dependency relationships;
- build tool/plugin components that affect output;
- generated timestamp and build invocation;
- artifact SHA-256;
- known completeness limits.

## Provenance

Release provenance follows the SLSA provenance model:

- subject artifact name and cryptographic digest;
- build type and external parameters;
- resolved source/dependency inputs where available;
- builder identity and invocation;
- start/finish timestamps.

Toolly does not claim a SLSA build level until the selected build platform, isolation, signer and verification process meet that level and evidence is reviewed.

## GitHub availability gate

GitHub artifact attestations can bind artifacts and SBOMs to Actions provenance, but availability for private repositories depends on the organization plan. Before enabling:

1. verify the organization plan and repository setting;
2. use a protected release environment;
3. grant `id-token` and `attestations` write only to the release job;
4. pin the attestation action to an immutable commit;
5. verify the attestation in a separate release-verification step;
6. preserve a fallback signed digest/SBOM flow if the feature is unavailable.

Generating an attestation without verifying it provides no consumer assurance.

## Retention

Release artifact, SBOM, provenance, lock state, verification metadata and approval snapshot are retained together for the supported release lifetime plus the approved security evidence period. Deletion requires release/security-owner approval.

## References

- [CycloneDX 1.6](https://cyclonedx.org/news/cyclonedx-v1.6-now-an-ecma-international-standard/)
- [GitHub artifact attestations](https://docs.github.com/en/actions/concepts/security/artifact-attestations)
- [SLSA provenance](https://slsa.dev/spec/v1.2/build-provenance)
