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

The Android capture-to-library walking slice and platform-only encrypted repository are
implementation candidates. Secure PDF/JPEG export is an Android executable candidate. Their Apple
counterparts and cross-platform evidence remain pending under the linked TLY-006D, TLY-006F and
TLY-012 work.

Every current gap is explicit in the parity matrix and blocks beta/release parity approval.
