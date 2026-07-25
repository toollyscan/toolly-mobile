# Android–iOS Product Parity

## Decision

Toolly is one product for Android phones/tablets and iPhone/iPad. Product behavior, information
architecture, design tokens, non-system UI, localization, accessibility, privacy and security
outcomes are shared requirements.

Compose Multiplatform is the default UI implementation for Toolly-owned screens. Kotlin
Multiplatform owns canonical models, validation, use cases, repository contracts, presentation
state and localization resources.

## Allowed platform differences

Only operating-system-controlled integrations may differ:

| Area | Android adapter | Apple adapter | Shared outcome |
|------|-----------------|---------------|----------------|
| Capture | ML Kit/CameraX | VisionKit/AVFoundation | Ordered captured pages |
| Permissions | Android permission APIs | Apple authorization APIs | Purpose-limited consent |
| Keys | Android Keystore | Apple Keychain/Secure Enclave where supported | Non-exportable key protection |
| File operations | Android system picker/share/print | Apple document picker/share/print | User-directed import/export |
| Notifications | FCM/Android notification APIs | APNs/Apple notification APIs | Same categories and consent |
| Purchases | Google Play Billing | StoreKit | Same Toolly entitlements |
| Navigation gesture | Android system Back | Apple back gesture | Same destination and state |

Platform adapters must return Toolly-owned results. Provider types, paths, tokens and exceptions
must not enter shared domain or presentation APIs.

The canonical provider-neutral capture boundary is now the dependency-free `shared-core` module.
Android uses temporary package aliases while the Apple adapter is implemented. See
[Shared Capture Contract](SHARED_CAPTURE_CONTRACT.md).

## Parity gate

The machine-readable source of truth is
[`config/platform/parity.json`](../../config/platform/parity.json).

A feature may be temporarily ahead on one platform only when:

1. the gap is explicit in the matrix;
2. a GitHub issue owns the missing implementation;
3. shared contracts and UI do not encode the leading platform;
4. the gap blocks beta/release parity approval.

An unexplained Android-only or iOS-only feature is a CI failure.

## String and fixture rule

- User-facing text and accessibility descriptions use localized resources.
- English, Hindi and Kannada resources use the same stable keys and plural semantics.
- Secrets, keys, project identifiers and environment values come from protected configuration.
- Sample documents, fixed IDs and synthetic payloads remain in test source sets or benchmark
  evidence directories and are never packaged into production applications.
- Internal format markers, schema keys and bounded protocol constants are code constants, not
  user-facing content.

## Current implementation status

Android capture-to-library and platform-only encrypted repository implementations are candidates.
Secure PDF/JPEG export is an Android executable candidate.

The first-party SwiftUI host now embeds the shared Compose framework for iPhone and iPad targets.
Multiplatform CI builds, installs and launches that host on iPhone and iPad simulators without app
permissions, network access, cloud configuration, distribution signing or a new dependency. This
is build-and-launch evidence only; it does not substitute for physical-device, VoiceOver, Apple
capture, vault or export validation.

The capture contract itself compiles and is tested for Android and iOS. TLY-012A owns the
first-party Apple adapter; TLY-012B owns physical-device and accessibility evidence.

Every current gap is explicit in the parity matrix and blocks beta/release parity approval.
