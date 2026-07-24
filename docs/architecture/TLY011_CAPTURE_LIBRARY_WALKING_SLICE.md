# TLY-011 Capture-to-Library Walking Slice

## Product outcome

This slice replaces the isolated capture harness with the first offline product loop:

```text
Document library → ML Kit capture → review → staged local save → library → reopen
```

The application works without Firebase configuration, network access, account sign-in, broad
storage permission or a commercial document SDK.

## Dependency direction

```mermaid
flowchart LR
    UI[Android Compose UI] --> UseCases[domain-usecases]
    UI --> Scanner[Toolly DocumentScanner port]
    UseCases --> Contracts[domain-contracts]
    UseCases --> Model[domain-model]
    Contracts --> Model
    Contracts --> Foundation[foundation]
    AndroidRepository[Android app-private repository candidate] --> Contracts
    MLKit[ML Kit adapter] --> Scanner
    Composition[Android composition root] --> AndroidRepository
    Composition --> MLKit
```

The domain packages intentionally contain no Android, Firebase, ML Kit, SQL, filesystem or
provider types. TLY-011 initially keeps them in the reviewed Android Gradle module to avoid adding
a build plugin solely for scaffolding. The later physical KMP-module extraction changes build
targets without changing these dependency rules or public APIs.

## Local transaction candidate

The TLY-011 adapter publishes a document with a same-filesystem staged directory:

1. resolve Toolly-owned temporary asset IDs inside the Android adapter;
2. validate and bounded-copy each complete JPEG into a transaction directory;
3. write and fsync the versioned manifest;
4. write and fsync the commit marker;
5. atomically rename the transaction directory into the visible document directory;
6. delete incomplete staging directories when the repository reopens.

Readers only expose directories containing a valid commit marker, manifest and complete assets.
Retries with the same document ID are idempotent.

## Security status

This adapter is a development-only candidate and is not the final encrypted vault. Android app
sandboxing, backup disabled, zero requested permission, bounded copies, atomic visibility and
content-safe error handling are enforced. Document assets are not uploaded and their paths,
filenames, titles or bytes are not logged.

The current candidate still stores committed JPEG bytes as app-private plaintext. It must not ship
to beta or production. TLY-006F must replace it with the ADR-0012 platform-key/encrypted-metadata and encrypted-asset
adapters, prove tamper/recovery/migration behavior and provide qualified
cryptographic review evidence. The `DocumentRepository` contract prevents that replacement from
changing product UI and use cases.

## Focused acceptance

- canonical IDs reject provider paths and non-canonical values;
- invalid or duplicate captured pages fail before persistence;
- incomplete writes are never listed;
- committed documents reopen through a new repository instance;
- platform image decoding creates no persistent plaintext cache;
- the manifest requests no Android permission;
- build, lint, unit tests and instrumented-test APK compilation pass;
- debug APKs remain downloadable from first-party GitHub Actions.

Full phone/tablet, performance, accessibility, localization, Figma and cryptographic evidence is a
beta/release gate. Critical crash, data-loss, privacy and architecture violations remain immediate
development blockers.
