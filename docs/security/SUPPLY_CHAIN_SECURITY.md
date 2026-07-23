# Software Supply-Chain Security

## Threats

| Threat | Primary controls |
|--------|------------------|
| Typosquatted or dependency-confusion package | Approved repositories, exact coordinates, registry review |
| Mutable version or CI tag changes silently | Exact versions, locks, Action SHA and image digest pins |
| Artifact replaced under same coordinates | Gradle SHA-256/signature verification |
| Vulnerable direct/transitive package | Resolved graph, advisory scan, SLA and release block |
| Malicious or abandoned maintainer transition | Ownership/release review and removal plan |
| Provider SDK leaks into domain | Path/import/dependency fitness checks |
| Build injects unrecorded artifact | Locked graph, SBOM, provenance and ephemeral CI |
| Compromised release/signing process | Protected environment, least privilege, attestation and verification |
| Licence or commercial surprise | SPDX evidence, legal gate and cost/usage review |
| SDK collects document/user data | Data-flow inventory, network/permission review and adapter isolation |

## Trust boundaries

- Source review approves intent, not downloaded artifact integrity.
- Maven/Google/Plugin repositories distribute artifacts but do not approve them.
- GitHub Actions executes third-party code inside CI and receives only explicitly granted permissions.
- The build runner is trusted to execute the workflow; release provenance must identify it.
- Firebase SDKs are infrastructure dependencies and remain outside canonical domain contracts.
- Future AWS evaluation does not authorize AWS code now.

## CI identity

All workflow dependencies are immutable. Workflows default to `contents: read`. Write permissions are granted only to the release job that needs them and only after protected-environment approval.

Fork and external pull-request workflows must not receive production secrets, signing material or privileged cloud credentials.

## Artifact integrity

Gradle verification checks integrity and, where reviewed signatures exist, publisher provenance. It does not detect known vulnerabilities; advisory scanning is a separate control.

Release AAB/IPA and their SBOM/provenance are identified by SHA-256. Consumers and operators verify the artifact rather than trusting a filename or workflow status screenshot.

## Provider isolation

The executable source scan enforces:

- AWS namespaces are prohibited in the current phase;
- Firebase namespaces appear only in Firebase adapters and composition roots;
- Play Billing and StoreKit appear only in billing adapters/composition roots;
- Android/AndroidX and Apple platform types do not enter common/domain modules;
- database drivers and provider DTOs do not enter domain APIs.

Static scanning is an early guard. Gradle dependency graph and public API signature tests become required after scaffolding.

## Incident signals

Escalate immediately when:

- verification metadata or lock state changes unexpectedly;
- a pinned artifact digest no longer resolves;
- an upstream account/repository is transferred or compromised;
- a dependency is malicious, known exploited or remotely exploitable in Toolly's path;
- a licence changes or a previously bundled feature becomes network-dependent;
- SBOM differs from the reviewed resolved graph;
- provider types bypass the approved adapter.

Follow [Dependency Vulnerability Response](../operations/DEPENDENCY_VULNERABILITY_RESPONSE.md) and the general [Security Incident Response](../operations/SECURITY_INCIDENT_RESPONSE.md).
