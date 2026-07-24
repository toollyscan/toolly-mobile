# ADR-0002 — Local Vault as Source of Truth

| Field | Value |
|-------|-------|
| Status | Accepted; storage and cryptographic evidence pending |
| Date | 2026-07-23 |
| Author | shivayogih |

## Context

Toolly must capture, organise and export without network access. Cloud backup is optional. Local
metadata and assets need atomic visibility, interruption recovery and explicit reconciliation.

## Decision

1. The encrypted local vault is authoritative for documents, pages, assets, revisions, operations,
   organisation metadata, outbox/checkpoints and entitlement cache.
2. Cloud stores encrypted copies and synchronization metadata; it does not directly overwrite
   committed local state.
3. Assets and revisions are immutable. A change creates new canonical IDs/revisions.
4. A local mutation, revision and outbox entry commit atomically.
5. Asset-bearing transactions use a recoverable staged protocol because database and filesystem
   writes are not assumed to be one transaction.
6. Sync compares revision ancestry. Divergence preserves both branches until an approved merge or
   user resolution; blanket local-wins and timestamp-only last-write-wins are rejected.
7. Restore validates versions, digests and references in quarantine before local publication.
8. ADR-0012 selects platform AES-GCM and platform key custody for implementation. Exact envelope
   encoding, metadata leakage, nonce, chunking, migration and recovery claims still require
   TLY-006 evidence and qualified review.

See:

- [VAULT_AND_PROCESSING_CONTRACTS.md](../architecture/VAULT_AND_PROCESSING_CONTRACTS.md)
- [SYNC_AND_FIREBASE_CONTRACTS.md](../architecture/SYNC_AND_FIREBASE_CONTRACTS.md)
- [SCHEMA_EVOLUTION.md](../architecture/SCHEMA_EVOLUTION.md)
- [ADR-0012 — Platform-only Local Vault Cryptography](0012-platform-only-local-vault-cryptography.md)

## Consequences

Positive:

- core work remains available during cloud outage;
- cloud cannot silently destroy local history;
- retries, restore and conflicts are testable;
- provider migration does not redefine local data.

Costs:

- staging journal, tombstones, revisions and conflict UX add complexity;
- storage/crypto selection requires device benchmarks and security review;
- migration fixtures must be retained for every release.

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Cloud authority | Violates offline-first and increases data-loss/privacy risk |
| Unencrypted local data | Violates the security objective |
| Blanket local-wins | Can discard legitimate remote device changes |
| Timestamp last-write-wins | Clock skew and silent overwrite risk |
| Hard-code crypto/storage before evidence | Premature security and performance claim |
