# Security Policy

## Supported versions

Only the latest release on the `main` branch receives security fixes.

## Reporting a vulnerability

**Do not open a public GitHub issue for security vulnerabilities.**

Please report security issues by emailing **security at toollyscan.com**.

Include:

- A clear description of the vulnerability.
- Steps to reproduce or a proof-of-concept.
- The potential impact.
- Any suggested mitigations.

We will acknowledge receipt within 48 hours and aim to provide an initial assessment within 5 business days.

## Security commitments

- Document content, OCR text, filenames, phone numbers, email addresses, OTPs, tokens and key material are never logged or sent to analytics.
- Encryption keys are never persisted in plaintext outside a hardware-backed keystore.
- Cloud backup is end-to-end encrypted; the cloud provider cannot read user documents.
- Firebase credentials and API keys are never committed to this repository.
- All provider credentials are managed outside source control.

## Threat model summary

A full threat model is maintained in [docs/security/SECURITY_BASELINE.md](docs/security/SECURITY_BASELINE.md).

## Disclosure policy

We follow a coordinated disclosure process. We ask that you:

1. Give us a reasonable time (up to 90 days) to address the issue before public disclosure.
2. Make a good-faith effort to avoid privacy violations, data destruction or service disruption.

We will credit researchers who responsibly disclose vulnerabilities unless they prefer anonymity.
