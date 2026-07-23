# ADR-0005 — Canonical Data and Operation Model

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2026-07-23 |
| Author | shivayogih |

## Context

Toolly needs stable ownership, document history, processing and entitlement contracts across
Android, iOS, Firebase and a possible future provider migration. Provider records and mutable
database rows cannot be canonical domain objects.

## Decision

1. Toolly owns typed canonical IDs for account, document, page, asset, revision, operation, device,
   folder, recipe and entitlement snapshot.
2. Source and derived assets, revisions and operations are immutable.
3. Document page order and metadata change through versioned operations and revisions.
4. Operation IDs and idempotency keys remain stable through retries.
5. Entitlements are historical snapshots and never determine ownership of local documents.
6. Canonical models are mapped explicitly to vault and provider records.
7. Every stored contract has an explicit schema or payload version and safe unknown-state behavior.

The detailed contract is
[CANONICAL_DOMAIN_MODEL.md](../architecture/CANONICAL_DOMAIN_MODEL.md).

## Consequences

Positive:

- provider and storage migrations do not redefine product identity;
- immutable history supports interruption recovery, sync and auditability;
- idempotency and conflict behavior are testable;
- billing/provider SDK types stay outside the domain.

Costs:

- explicit mapping and versioning code is required;
- tombstone/history retention consumes metadata;
- migrations and conflict UX require dedicated implementation.

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Firebase UID/path as identity | Provider lock-in and account-lifecycle coupling |
| Mutable page/image records | Weak recovery, replay and conflict semantics |
| Timestamp-only history | Cannot prove ancestry or resolve clock skew |
| Store receipt as entitlement model | Leaks provider semantics and can gate owned documents |
