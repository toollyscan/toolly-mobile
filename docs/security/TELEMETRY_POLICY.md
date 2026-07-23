# Telemetry Allowlist and Prohibited Data Tests

Telemetry is deny by default. An event or property is emitted only when registered here and in the
product metrics catalogue.

## Allowed property types

| Type | Examples |
|------|----------|
| Build | App version, build channel, schema version |
| Platform | Android/iOS, OS major version, device-class bucket |
| Operation | Canonical operation name, phase, success/cancel/failure |
| Performance | Duration, memory, page-count and size buckets |
| Reliability | Privacy-safe error code, retry count bucket |
| Feature | Local feature flag/version, backup enabled boolean |
| Locale/accessibility | Launch locale and coarse accessibility-mode boolean when approved |

Allowed values must be bounded enums or coarse buckets. Free text, full URLs, file paths and
arbitrary exception messages are prohibited.

## Prohibited data

- document images, PDFs, OCR text and extracted entities;
- titles, filenames, folders, tags and user-entered text;
- phone, email, name, address, IP or precise location;
- raw/stable account, device, provider, document, page, asset, operation or receipt IDs;
- OTP, password, token, cookie, secret, key, nonce and recovery material;
- full request/response bodies, provider snapshots and storage paths;
- clipboard, contacts, installed-app list or advertising identifier;
- accessibility text/labels derived from user content;
- support attachment content.

Hashing prohibited data does not make it allowed. High-cardinality or reversible encodings are
also prohibited.

## Event registration

Every event specifies:

- product/security question and owner;
- event name and version;
- exact allowed property names, types and value sets;
- prohibited examples;
- sampling and retention;
- destinations/processors;
- user control/notice requirement;
- test fixture and removal date/review.

Unregistered events fail CI.

## Logging API

Application code uses structured wrappers that accept only generated event/property types. Raw
logger/analytics/crash APIs are forbidden outside telemetry adapters. Exceptions are mapped to safe
codes at the boundary; messages are not forwarded.

## Required automated tests

1. Static rule rejects direct logger/analytics/crash SDK calls outside adapters.
2. Generated schema rejects unknown event/property names.
3. Property-based tests inject representative PII, content and secrets and verify rejection.
4. Snapshot tests inspect serialised events for exact allowlist conformance.
5. Redaction tests cover nested causes, URLs, paths and provider errors.
6. Runtime test captures all destinations during auth, scan, OCR, export, backup, recovery and
   deletion flows.
7. Crash tests verify breadcrumbs, custom keys and stack context.
8. Dependency upgrade tests detect new automatic collection/initialisation.

## Operational access

Telemetry access is least privilege, audited and environment-separated. Production exports are
approved, time-bounded and cannot be joined with identity or support data without a documented,
reviewed purpose.

## Opt-in services

Services that automatically collect data must remain disabled until inventory, notice, consent
where required, configuration and verification are complete. Inclusion of a Firebase SDK does not
constitute approval to enable its collection.
