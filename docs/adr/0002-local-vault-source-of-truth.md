# ADR-0002 — Local Vault as Source of Truth

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2025-07-01 |
| Author | shivayogih |

---

## Context

Toolly is positioned as a privacy-first, offline-first document scanner. Users must be able to capture, organise and export documents without a network connection. Cloud backup is a convenience feature, not a requirement.

The design must ensure:

- All document content is encrypted at rest on the device.
- Cloud state never overwrites local state without explicit user consent.
- Account recovery must not depend on cloud availability.

---

## Decision

1. The encrypted local vault (on-device database) is the **sole source of truth** for all documents, pages, tags and user preferences.
2. Cloud storage holds encrypted copies only. It is a backup and sync target, not an authority.
3. The conflict-resolution policy is **local-wins**: if a cloud version and a local version conflict, the local version is preserved and the user is notified.
4. The vault must be encrypted using AES-256-GCM with keys stored in the platform hardware-backed keystore (Android Keystore / iOS Secure Enclave).
5. Vault schema migrations must be backward-compatible and tested with a corpus of production-representative documents.
6. The cloud sync engine must be resumable: partial uploads and downloads must not corrupt the local vault.

---

## Vault schema (logical)

| Entity | Canonical ID | Encrypted fields |
|--------|-------------|-----------------|
| Document | `DocumentId` (UUID v4, Toolly-generated) | pages, metadata, tags |
| Page | `PageId` (UUID v4, Toolly-generated) | image reference, OCR result |
| Account | `ToollyAccountId` (UUID v4, Toolly-generated) | display name, preferences |

---

## Consequences

**Positive:**

- All core features work offline, including capture, processing, organisation and local export.
- No cloud outage can cause data loss.
- The cloud provider can be replaced without migrating the canonical data model.

**Negative:**

- Conflict resolution is simplified to local-wins, which may surprise users who edit on multiple devices. Multi-device conflict resolution requires a future CRDT or operational-transform layer.
- On-device encryption adds complexity to the build and key-management stack.

---

## Rejected alternatives

| Alternative | Reason rejected |
|-------------|----------------|
| Cloud as source of truth | Violates offline-first requirement; creates privacy risk. |
| Unencrypted local storage | Violates privacy-first requirement; fails DPDP Act 2023 obligations. |
| Last-write-wins from cloud | May silently overwrite local edits made offline. |
