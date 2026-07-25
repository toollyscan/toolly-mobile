# TLY-006F — Android Platform Vault Foundation

- **Issue:** #26
- **ADR:** ADR-0012
- **Status:** Candidate implementation; not a complete production vault
- **Date:** 2026-07-24

## Included

- canonical metadata associated-data encoding;
- versioned bounded metadata-envelope codec;
- unique random per-record AES-256 data key;
- Android Keystore AES-GCM wrapping key;
- platform AES-GCM metadata encryption and authentication;
- fail-closed invalid-envelope, tamper, AAD-substitution and missing-key behavior;
- Android platform image decoding without a third-party loader or persistent plaintext cache.

## Follow-on implementation

The encrypted Android document repository is defined in
[TLY006F_ENCRYPTED_DOCUMENT_REPOSITORY.md](TLY006F_ENCRYPTED_DOCUMENT_REPOSITORY.md). It integrates
this candidate with persistent encrypted manifests, per-asset authenticated chunks, atomic
publication, bounded legacy migration and memory-only rendering.

## Still not included

- key rotation, backup or multi-device recovery;
- Apple Keychain/CryptoKit adapter;
- production cryptographic approval.

PDF and thumbnail asset kinds remain separate product slices. The previous
`AppPrivateDocumentRepository` is removed from production source after its version-one migration
reader is covered.

## Boundary

```mermaid
flowchart LR
    Metadata["Canonical metadata bytes"] --> Cipher["AndroidMetadataCipher"]
    AAD["Canonical immutable context"] --> Cipher
    Cipher --> DEK["Random per-record DEK"]
    Keystore["Android Keystore wrapping key"] --> Wrap["AES-GCM key wrap"]
    DEK --> Content["AES-GCM content encryption"]
    Wrap --> Envelope["Versioned metadata envelope"]
    Content --> Envelope
```

Android Keystore and JCA types remain inside `vault/crypto`. The codec contains no Android type and
can become a shared contract only after Android/Apple interoperability vectors pass.

## Evidence

JVM tests cover canonical purpose separation and strict envelope framing. Android instrumented tests
cover:

- encryption/decryption through separate adapter instances;
- absence of the plaintext fixture in the encoded envelope;
- ciphertext tamper rejection;
- associated-data substitution rejection;
- missing wrapping-key failure without reset;
- distinct envelopes for repeated encryption.

This slice uses test-only synthetic bytes. It contains no hardcoded production content, user data,
credential, API key or Firebase identifier.
