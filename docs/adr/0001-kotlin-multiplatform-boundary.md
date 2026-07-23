# ADR-0001 — Kotlin Multiplatform Boundary

| Field | Value |
|-------|-------|
| Status | Accepted; prototype evidence pending |
| Date | 2026-07-23 |
| Author | shivayogih |

## Context

Toolly targets Android and iOS. Sharing stable product policy reduces divergence, while camera,
image, cryptography, storage and UI surfaces need platform evidence and native integration.

## Decision

1. Share canonical models, validation, use cases, ports, processing recipes, sync/conflict policy
   and entitlement evaluation through KMP.
2. Keep camera sessions, image buffers/GPU resources, key protection, filesystem implementation,
   biometrics, PDF/share, push handling and billing SDKs platform-specific behind Toolly ports.
3. Use `expect`/`actual` only for small stable APIs; wrap large SDK surfaces through interfaces.
4. Use Jetpack Compose on Android and SwiftUI on iOS by default.
5. Do not share production UI/ViewModels until DA-001 and technical spikes provide evidence.
6. Firebase is implemented through adapters and is not a KMP domain dependency.

See [MODULE_BOUNDARIES.md](../architecture/MODULE_BOUNDARIES.md).

## Consequences

Positive:

- stable product policy is tested once;
- native camera, accessibility and performance paths remain available;
- platform/provider replacement does not change domain contracts.

Costs:

- explicit ports, mappings and platform implementations are required;
- Kotlin/Native/Swift interop and cancellation need contract tests;
- UI behavior must be verified on both platforms.

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Full shared UI immediately | Insufficient document-capture, accessibility and device evidence |
| Native-only Android first | Conflicts with approved dual-platform direction |
| Expose native SDK types to common code | Breaks replacement and test boundaries |

## Evidence required

- KMP build and Swift interop prototype;
- camera request/result boundary prototype;
- cancellation and memory behavior tests;
- Compose Multiplatform versus native benchmark before any UI decision changes.
