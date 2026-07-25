# Shared Capture Contract

## Decision

Toolly capture is defined in the dependency-free `shared-core` Kotlin Multiplatform module.
Android and Apple capture engines are replaceable platform adapters behind the same
`DocumentScanner` port.

This extraction changes ownership, not Android behavior. Android adapters and tests import the
canonical `com.toolly.shared.capture` package directly.

## Dependency direction

| Layer | May depend on | Must not expose |
|---|---|---|
| Shared capture contract | Kotlin standard library | Android, Apple or provider types |
| Android adapter | Shared contract and approved Google Android APIs | Paths, URIs or SDK exceptions |
| Apple adapter | Shared contract and first-party Apple APIs | UIKit/VisionKit objects or file URLs |
| Shared presentation | Shared Toolly models and actions | Provider selection or permission APIs |

The shared boundary contains only configuration, ordered pages, opaque temporary-asset identifiers
and allowlisted terminal outcomes. It contains no document bytes, OCR text, user metadata, cloud
identifier, credential, path, URI or arbitrary exception message.

## Temporary-asset ownership

1. A platform adapter creates and validates app-private temporary assets.
2. Success transfers opaque identifiers to the caller in page order.
3. The caller releases each asset after display or promotion to the encrypted vault.
4. Cancellation and failure transfer nothing; the adapter cleans up its session assets.
5. Partial capture exposes only validated pages and an allowlisted reason.

## Compatibility boundary

The obsolete Android-local contract declarations have been removed. All adapters import the
canonical shared package directly, while provider implementations remain outside `shared-core`.

## Verification

Multiplatform CI compiles and tests `shared-core` for Android and the iOS simulator, and compiles
the device target. Android CI also builds the existing capture app through the shared project
dependency. This slice adds no permission, network request, cloud integration or external
dependency.

The first-party Apple implementation and physical iPhone/iPad evidence remain tracked in
[TLY-012A](https://github.com/toollyscan/toolly-mobile/issues/48) and
[TLY-012B](https://github.com/toollyscan/toolly-mobile/issues/49).
