# Developer Handoff

Defines the handoff standard that must be met before any Figma screen or component is handed off for implementation.

---

## Purpose

This document specifies what must be present in the Figma Developer Handoff page (Page 14) and what information engineers need to implement each screen and component correctly.

Handoff is not complete until every item in the checklist below is present in the relevant Figma frame or annotation.

---

## Handoff checklist per screen or component

For every screen and component approved for implementation, the following information must be present in the Figma handoff layer or annotation:

### Reference

- [ ] Figma frame or component node path (e.g., Page 04 / Onboarding / Phone / AU-05 OTP verification)
- [ ] Screen or component ID from SCREEN_INVENTORY.md or COMPONENT_INVENTORY.md
- [ ] Linked requirement or acceptance criterion ID

### Tokens

- [ ] Background colour token
- [ ] Text colour token(s)
- [ ] Spacing tokens for padding, margin and gap values
- [ ] Radius tokens
- [ ] Elevation token
- [ ] Typography token(s)

### Component identity

- [ ] Component name matching COMPONENT_INVENTORY.md
- [ ] Variant name (e.g., `PrimaryButton/loading`)
- [ ] State documented (e.g., default, focused, error)

### Behaviour

- [ ] Phone layout specification
- [ ] Tablet layout specification (including any two-pane or NavigationRail change)
- [ ] Immersive mode behaviour (navigation hidden/shown)
- [ ] Maximum content width (where applicable)

### Accessibility

- [ ] Semantic role (button, image, heading, text field, etc.)
- [ ] Accessible label or label pattern
- [ ] Accessible hint (where required)
- [ ] Focus order relative to adjacent elements
- [ ] Custom accessibility action (where drag interactions exist)
- [ ] State announcement text (for loading, error, success)

### Localization

- [ ] String resource key
- [ ] Long-string behaviour (wrap or truncate rule)
- [ ] Pluralization rule (if count-dependent)
- [ ] Locale variants shown in frame (at minimum en-IN and hi-IN)

### Error and loading states

- [ ] Loading state frame or reference
- [ ] Error state frame or reference
- [ ] Empty state frame or reference (where applicable)
- [ ] Offline state frame or reference (where applicable)

### Motion

- [ ] Entry transition token and description
- [ ] Exit transition token and description
- [ ] Reduced-motion alternative
- [ ] Any shared-element or hero animation specification

### Assets

- [ ] Asset name and export format (SVG, PNG, WebP)
- [ ] Asset ownership confirmed (not copied from external sources)
- [ ] Required resolutions or density variants

### Implementation notes

- [ ] Any platform-specific behaviour (Android vs. iOS)
- [ ] Known constraints or design limitations
- [ ] Dependencies on other components or screens

### Test expectations

- [ ] Visible state the engineer can verify in a screenshot or UI test
- [ ] Accessibility assertion expected (e.g., "content description reads 'Capture page'")
- [ ] Behaviour to verify at 200% text scale

---

## Handoff stages

Handoff proceeds in stages aligned with the Figma completion gates (see FIGMA_COMPLETION_GATE.md):

| Stage | Gate | What is handed off |
|-------|------|--------------------|
| 1 — Foundations | G2 | Design tokens and typography |
| 2 — Components | G3 | All component variants and states |
| 3 — Core screens | G4 (partial) | Auth, home, scan capture |
| 4 — All screens | G4 (complete) | All screens in SCREEN_INVENTORY.md |
| 5 — Prototypes | G5 | Flow annotations and prototype references |
| 6 — Localization | G6 | Localized frames and string keys |
| 7 — Accessibility | G7 | Accessibility annotations |
| 8 — Adaptive | G8 | Phone and tablet specifications |
| 9 — Final | G9 | Complete annotated handoff for all approved screens |

A stage must not be declared complete until the corresponding gate is approved.

---

## Component-to-design-system name mapping

Component names in Figma must match the canonical component names in COMPONENT_INVENTORY.md.

This enables direct mapping from design to production code without renaming.

| Figma component name | Design-system name | Notes |
|---------------------|--------------------|-------|
| `BottomNavBar` | `BottomNavBar` | Exact match required |
| `NavigationRail` | `NavigationRail` | Exact match required |
| `PrimaryButton` | `PrimaryButton` | |
| `SecondaryButton` | `SecondaryButton` | |
| `TextButton` | `TextButton` | |
| `DestructiveButton` | `DestructiveButton` | |
| `IconButton` | `IconButton` | |
| `ToggleIconButton` | `ToggleIconButton` | |
| `ScanFab` | `ScanFab` | |
| `TextField` | `TextField` | |
| `PasswordField` | `PasswordField` | |
| `OtpField` | `OtpField` | |
| `SearchBar` | `SearchBar` | |
| `FilterChip` | `FilterChip` | |
| `DocumentCard` | `DocumentCard` | |
| `PageThumbnail` | `PageThumbnail` | |
| `StatusChip` | `StatusChip` | |
| `InfoBanner` | `InfoBanner` | |
| `ActionBanner` | `ActionBanner` | |
| `AlertDialog` | `AlertDialog` | |
| `ModalBottomSheet` | `ModalBottomSheet` | |
| `Snackbar` | `Snackbar` | |
| `LinearProgressBar` | `LinearProgressBar` | |
| `CircularProgressIndicator` | `CircularProgressIndicator` | |
| `SkeletonLoader` | `SkeletonLoader` | |
| `EmptyState` | `EmptyState` | |
| `ErrorState` | `ErrorState` | |
| `OfflineIndicator` | `OfflineIndicator` | |
| `SyncIndicator` | `SyncIndicator` | |
| `PremiumLockBadge` | `PremiumLockBadge` | |
| `SubscriptionStatusChip` | `SubscriptionStatusChip` | |
| `CaptureButton` | `CaptureButton` | |
| `FlashToggle` | `FlashToggle` | |
| `BatchCounter` | `BatchCounter` | |
| `CaptureWarningOverlay` | `CaptureWarningOverlay` | |
| `CropHandle` | `CropHandle` | |
| `MagnifiedHandleInset` | `MagnifiedHandleInset` | |
| `OcrStatusIndicator` | `OcrStatusIndicator` | |
| `AutosaveIndicator` | `AutosaveIndicator` | |
| `TrustedDeviceCard` | `TrustedDeviceCard` | |
| `RecoveryPhraseDisplay` | `RecoveryPhraseDisplay` | |

---

## Handoff audit status

Repository handoff specification: **Complete** (this document).

Figma Developer Handoff page (Page 14): **Evidence pending** — page existence and annotation coverage not verified.

Handoff completion is gated on G9 (see FIGMA_COMPLETION_GATE.md).
