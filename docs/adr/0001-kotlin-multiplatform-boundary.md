# ADR-0001 — Kotlin Multiplatform and Shared UI Boundary

| Field | Value |
|-------|-------|
| Status | Accepted |
| Date | 2026-07-23; amended 2026-07-24 |
| Author | shivayogih |
| Supersedes | The earlier native-UI default in this ADR |

## Context

Toolly targets Android phones/tablets and iPhone/iPad as one product. Separate product UI
implementations would duplicate behavior, localization, accessibility state and privacy-sensitive
presentation logic. Camera, key protection, file operations, notifications and purchases still
require operating-system integrations.

## Decision

1. Kotlin Multiplatform owns canonical models, validation, use cases, ports, processing recipes,
   sync/conflict policy, entitlement evaluation and provider-neutral presentation state.
2. Compose Multiplatform owns Toolly-created screens, components, design tokens, navigation
   destinations, localization resources and accessibility semantics.
3. Android and iOS composition roots connect the shared application to platform adapters.
4. Camera sessions, native image buffers, key protection, filesystem implementation, biometrics,
   document pickers, PDF/share/print, push handling and store billing remain platform-specific.
5. Operating-system-controlled screens may look native, but must return the same Toolly-owned
   result and preserve the same product outcome.
6. Use `expect`/`actual` only for small stable APIs. Wrap large SDK surfaces through Toolly-owned
   interfaces.
7. Firebase remains behind adapters and is not a common domain or presentation dependency.
8. Provider types, platform paths, native exceptions and credentials must not enter common APIs.
9. An unequal Android/iOS feature state must be explicit in the parity matrix and linked to a
   tracking issue. Unexplained platform divergence fails CI.

See [MODULE_BOUNDARIES.md](../architecture/MODULE_BOUNDARIES.md) and
[PLATFORM_PARITY.md](../architecture/PLATFORM_PARITY.md).

## Consequences

Positive:

- product behavior, design, localization and accessibility evolve once;
- Android phone/tablet and iPhone/iPad remain aligned by default;
- native capture, security and system integration remain available;
- provider and platform replacement does not change domain contracts.

Costs:

- Android and iOS adapters, composition roots and device tests are still required;
- Kotlin/Native interop, cancellation, memory and lifecycle behavior require contract tests;
- iOS builds and release signing require macOS/Xcode infrastructure;
- platform differences must be intentionally documented and reviewed.

## Rejected alternatives

| Alternative | Reason |
|-------------|--------|
| Jetpack Compose plus separate SwiftUI product screens | Duplicates state, strings, accessibility and behavior and increases parity drift |
| Android implementation completed before iOS begins | Creates structural platform debt and conflicts with the active parity decision |
| Force system-controlled UI into shared rendering | Reduces platform reliability and may violate operating-system behavior |
| Expose native SDK types to common code | Breaks replacement, testing and provider boundaries |

## Evidence required

- common tests run on every pull request;
- Android application builds on Linux;
- iOS framework/application builds on a pinned macOS/Xcode runner;
- shared document library, review and viewer render on phone and expanded layouts;
- platform adapter contract tests cover success, cancellation, permission denial and failure;
- representative physical Android and Apple devices satisfy accessibility and performance gates.
