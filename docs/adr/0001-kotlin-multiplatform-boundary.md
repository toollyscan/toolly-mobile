# ADR-0001 — Kotlin Multiplatform Boundary

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2025-07-01 |
| Author | shivayogih |

---

## Context

Toolly targets Android and iOS. Sharing business logic reduces maintenance cost and reduces the risk of behavioural divergence between platforms.

Kotlin Multiplatform (KMP) allows Kotlin code to compile to Android (JVM/Android) and iOS (Kotlin/Native). Compose Multiplatform (CMP) extends this to shared UI.

However, KMP/CMP maturity varies by API surface:

- Domain models, use cases and repository interfaces are well-supported.
- Platform APIs (camera, file system, cryptography, biometrics) require platform-specific implementations via `expect`/`actual`.
- CMP rendering on iOS has not yet been validated at production quality for document-scan preview.

---

## Decision

1. **Share via KMP:** domain models, use cases, repository interfaces, validation logic, canonical ID generation and sync contracts.
2. **Platform-specific via `expect`/`actual`:** camera capture, local storage, cryptographic key management, biometric authentication, share/export and push notification handling.
3. **Do not share UI via CMP until evidence is gathered** (see DA-001 in DESIGN_AUDIT.md). Each platform uses its native UI toolkit by default: Jetpack Compose on Android, SwiftUI on iOS.
4. ViewModels may be evaluated for KMP sharing after CMP evidence is gathered.

---

## Consequences

**Positive:**

- Business logic is tested once and behaves identically on both platforms.
- Domain code is completely independent of Android and iOS frameworks.
- Provider migrations (Firebase → AWS) affect only data-layer implementations.

**Negative:**

- `expect`/`actual` boilerplate is required for every platform API.
- Build complexity is higher than a single-platform project.
- Kotlin/Native memory model and interop with Swift require care.

---

## Rejected alternatives

| Alternative | Reason rejected |
|-------------|----------------|
| Flutter | Dart ecosystem is not the team's primary competency; platform channel overhead for document processing. |
| React Native | JavaScript bridge latency is unsuitable for real-time camera preview and GPU image processing. |
| Native-only Android first | Creates divergent codebases and delays iOS launch. |
| Full CMP UI sharing | Insufficient production evidence for document-scan preview quality on iOS at this time. |

---

## Evidence required before changing status

- Benchmark of CMP rendering vs. native on document-scan preview on representative Android and iOS devices (see DA-001).
- Prototype of the `expect`/`actual` camera capture boundary.
