# Architecture Fitness Functions

These checks convert Toolly architecture rules into required CI evidence. This document defines the
check contracts now; executable tasks are added with module scaffolding and become required before
production code merges.

## Required checks

| Check name | Failure condition | Initial implementation |
|------------|-------------------|------------------------|
| `architecture:dependencyDirection` | A module depends contrary to `MODULE_BOUNDARIES.md` | Gradle/Xcode dependency graph assertion |
| `architecture:forbiddenImports` | Shared/domain source references platform, provider, billing or DB namespaces | Static source/API scan |
| `architecture:publicApiLeakage` | Provider/platform types appear in public Toolly contracts | Kotlin/Swift API signature inspection |
| `architecture:canonicalIds` | Provider UID/path is used as canonical owner/object identity | Architecture test plus mapping tests |
| `architecture:providerIsolation` | Firebase code exists outside allowed adapters/composition roots | Path/import/dependency rule |
| `architecture:offlineCore` | Capture/processing/organisation/export use cases require network ports | Use-case dependency tests |
| `architecture:outboxAtomicity` | Mutation can commit without revision/outbox or duplicate on retry | Transaction contract tests |
| `architecture:schemaCompatibility` | Old fixture cannot be read/migrated or unknown mandatory version is accepted | Golden fixture suite |
| `architecture:recipeCompatibility` | Recipe history is rewritten or unsupported mandatory steps execute | Recipe contract tests |
| `architecture:sensitiveTelemetry` | Prohibited fields/types reach logger/analytics APIs | Compile-time wrappers plus static/runtime tests |
| `architecture:adapterContracts` | Fake and Firebase emulator differ semantically | Shared contract suite |
| `architecture:dependencyGovernance` | Unapproved dependency/licence/version enters build | Version/dependency policy check |

## Namespace denylist categories

The executable rule must cover categories rather than only the examples below:

- Android and AndroidX in common/domain modules;
- Apple platform and Objective-C/Swift framework types in common/domain modules;
- Firebase and Google service SDKs outside Firebase adapters/composition roots;
- AWS SDKs everywhere in the current phase;
- Play Billing and StoreKit outside billing adapters;
- database driver, SQL row and encrypted-file implementation types in domain;
- provider futures/tasks, snapshots, timestamps and path objects in public APIs.

Generated code and transitive public APIs are included.

## Architecture test fixtures

The repository will keep intentionally invalid fixture modules or source snippets for each rule.
CI must prove each violation is detected; a scanner that only passes valid code is not sufficient.

## Contract test matrix

| Contract | Fake | Platform test | Emulator/staging |
|----------|------|---------------|------------------|
| Vault transactions and recovery | Required | Required | N/A |
| Processing recipe/version behavior | Required | Required on device matrix | N/A |
| Sync/outbox/idempotency | Required | Integration | Firebase emulator required |
| Authentication mapping | Required | Integration | Firebase emulator/staging |
| Entitlement mapping | Required | Store sandbox | Verification staging |
| Schema/backup restore | Required | Required | Emulator for remote manifests |

No production credentials or personal documents are used.

## CI rollout

1. Documentation phase: Markdown and secret scanning validate this baseline.
2. Scaffolding phase: dependency, forbidden-import and public-API checks become required.
3. Adapter phase: shared fake/emulator contract suites become required.
4. Production phase: schema fixtures, device benchmarks and privacy tests are required.

A check cannot be marked passed before its executable implementation exists. Until then, it is
`Not implemented` or `N/A` with a reason.

## Exception process

An exception requires:

- linked issue and ADR;
- exact rule/path/type exception;
- business and technical rationale;
- accountable owner;
- expiry date;
- risk controls and removal test.

Broad wildcards and permanent suppressions are forbidden. Expired exceptions fail CI.

## Evidence retention

CI retains machine-readable reports for dependency graphs, public API, schema compatibility,
adapter contracts and security/privacy scans. Pull requests link the actual run; copied text or
self-certification is not evidence.
