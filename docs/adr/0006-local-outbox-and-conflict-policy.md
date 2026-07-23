# ADR-0006 — Local Outbox and Conflict Policy

| Field | Value |
|-------|-------|
| Status | Accepted; implementation evidence pending |
| Date | 2026-07-23 |
| Author | shivayogih |

## Context

The local vault is authoritative while backup and future synchronization are asynchronous.
Networks, process death and provider retries produce duplicate and out-of-order delivery. Multiple
devices can create divergent revisions.

## Decision

1. A local mutation, immutable revision and outbox entry commit atomically.
2. Delivery is at least once; adapters provide effectively-once application through stable
   operation IDs and idempotency keys.
3. Per-aggregate operation ordering is preserved.
4. Revision ancestry, not wall-clock time, determines fast-forward versus divergence.
5. Divergent branches are preserved as a conflict set. Timestamp-only last-write-wins and blanket
   local-wins are rejected.
6. Auto-merge is limited to documented commutative operations.
7. Conflict resolution creates a revision with all resolved heads as parents.
8. Remote metadata and objects are staged and verified before a local commit.
9. Firebase implements Toolly-owned sync ports; provider types and paths do not cross the adapter.

See [SYNC_AND_FIREBASE_CONTRACTS.md](../architecture/SYNC_AND_FIREBASE_CONTRACTS.md).

## Consequences

Positive:

- retries do not duplicate user intent;
- cloud state cannot silently overwrite local work;
- failures remain resumable and diagnosable;
- the same contract can be tested against fake and Firebase emulator adapters.

Costs:

- revision ancestry and tombstones require metadata;
- conflicts need product UX and retention policy;
- adapter and transaction contract tests are mandatory.

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Cloud as source of truth | Violates offline-first behavior |
| Timestamp last-write-wins | Clock skew and silent data loss |
| Blanket local-wins | Discards legitimate remote/multi-device changes |
| Exactly-once transport claim | Not realistic across mobile/network/provider failures |
