# Security Policy

## Supported versions

Security support begins with the first public release. Before launch, fixes are made on `main`; after launch, the currently supported release range will be published here.

## Reporting a vulnerability

Do not open a public GitHub issue for a suspected vulnerability or include user data, credentials, documents or secrets in a report.

The private reporting address and backup channel are a pre-launch gate. Until they are verified and published, repository maintainers must use GitHub's private vulnerability-reporting feature. See the [vulnerability disclosure runbook](docs/operations/VULNERABILITY_DISCLOSURE.md).

Include:

- A clear description and affected version or commit.
- Reproduction steps or a minimal proof of concept.
- The likely impact and any observed data exposure.
- A safe way to contact the reporter.

Acknowledgement, assessment and remediation targets will be published only after the monitored channel, staffing and escalation rotation are verified. Toolly does not promise a bounty, legal safe harbour or a fixed disclosure date in this baseline.

## Security posture

- Toolly intends to keep document content and sensitive identity or cryptographic data out of telemetry and logs; this is enforced through the [telemetry policy](docs/security/TELEMETRY_POLICY.md) and release evidence.
- Local vault and optional backup protection follow a versioned envelope design that remains proposed until qualified cryptography review. See [ADR-0007](docs/adr/0007-encryption-envelope-and-key-hierarchy.md).
- Hardware-backed key protection is used when supported and verified; it is not assumed to exist on every supported device.
- Provider inability to read backed-up document content is a target property that must be demonstrated by implementation and restore evidence before it becomes a production claim.
- Firebase credentials and operational secrets remain outside source control and are checked by secret scanning.

## Coordinated disclosure

Toolly will coordinate remediation and disclosure based on verified severity, exploitation risk, affected users, dependency/vendor coordination and legal obligations. Researchers should avoid privacy violations, destructive testing and service disruption. Credit is offered with the reporter's consent when doing so is safe.

Operational handling is defined in:

- [Security incident response](docs/operations/SECURITY_INCIDENT_RESPONSE.md)
- [Vulnerability disclosure](docs/operations/VULNERABILITY_DISCLOSURE.md)
- [Security baseline](docs/security/SECURITY_BASELINE.md)
