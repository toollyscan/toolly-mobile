# Developer Handoff

This document defines the process for transferring design specifications from Figma to
engineering. No screen or component may be implemented in engineering until it has passed the
[FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md) for that item.

Figma design is currently **evidence-pending**; see [FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

---

## Handoff prerequisites

Before a screen or component is handed off to engineering, all of the following must be true:

- [ ] The Figma frame for this screen or component is complete and approved in design review.
- [ ] All required component states are designed (see [COMPONENT_STATE_MATRIX.md](COMPONENT_STATE_MATRIX.md)).
- [ ] Design tokens are applied consistently (no hardcoded colours, sizes or spacing).
- [ ] Accessibility annotations are attached (content descriptions, focus order, touch targets).
- [ ] Localisation annotations are present (string key references; Hindi and Kannada variants reviewed).
- [ ] Responsive variants are designed for all required breakpoints (see [RESPONSIVE_LAYOUTS.md](RESPONSIVE_LAYOUTS.md)).
- [ ] The screen has passed Figma Completion Gate review (see [FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md)).

---

## Handoff artefacts

For each screen or component handed off to engineering, the designer must provide:

| Artefact | Description |
|---------|-------------|
| Figma link | Direct link to the approved Figma frame or component. |
| Screen ID | Canonical screen ID from [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md). |
| Token references | List of design tokens used; engineer must use the token, not the raw value. |
| Asset exports | Icons, illustrations and images exported at the required densities. |
| Redline annotations | Spacing, alignment and size annotations in the Figma frame. |
| Accessibility annotations | Content descriptions, focus order, touch target sizes. |
| State coverage | Confirmation that all states in [COMPONENT_STATE_MATRIX.md](COMPONENT_STATE_MATRIX.md) are present. |
| String references | String key names matching the externalised string resources. |

---

## Asset export specifications

### Icons

- Format: SVG for vector icons (preferred); PDF as fallback for iOS.
- Android: export at 1× (mdpi) baseline; the build system scales to other densities.
  Alternatively, use vector drawables (XML) when icons are simple enough.
- iOS: export as PDF 1× or SVG; Xcode scales using `@1x`, `@2x`, `@3x` asset slots.
- Name format: `ic_{name}_{size}` (e.g., `ic_capture_24`, `ic_folder_24`).

### Illustrations

- Format: SVG or Lottie (JSON) for animated illustrations.
- Static illustrations: SVG at the largest required size.
- Animated illustrations (e.g., empty states with animation): Lottie JSON.
  — Lottie must not require a commercial licence.
  — Lottie animations must respect system reduced-motion settings.
- Name format: `il_{name}` (e.g., `il_empty_library`, `il_scan_success`).

### App icon

- Android: provide as adaptive icon layers (foreground and background) plus legacy icon.
- iOS: provide as a 1024 × 1024 pt PNG for the App Store; Xcode generates all sizes from
  the asset catalogue.

---

## Token implementation

Engineering must implement design tokens as a single source of truth, not as scattered
hardcoded values. Recommended implementation:

- Shared module (`design` or `ui-tokens`): defines all colour, typography, spacing, radius
  and motion tokens as type-safe constants.
- Platform-specific bindings: Compose `MaterialTheme` for Android; SwiftUI `Environment`
  values for iOS.
- Token names must exactly match the token names defined in [DESIGN_TOKENS.md](DESIGN_TOKENS.md).

---

## Handoff workflow

1. **Designer completes a screen or component** in Figma and confirms all gate criteria are met.
2. **Designer updates the Figma Completion Gate** (see [FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md))
   to mark the item as approved.
3. **Designer opens a handoff issue** in GitHub using the design handoff issue template, linking:
   - The screen ID(s).
   - The Figma frame link.
   - The list of assets to export.
4. **Engineer reviews the Figma frame**, asks any clarification questions in the GitHub issue.
5. **Engineer implements the screen or component**, referencing design tokens and screen IDs.
6. **Engineer opens a PR** linking the handoff issue; the PR description includes:
   - Screenshot or recording of the implementation on Android and/or iOS.
   - Confirmation that token values are used (not hardcoded values).
   - Confirmation that accessibility labels and states are implemented.
7. **Designer reviews the implementation** against the Figma frame and approves or requests
   changes.
8. **PR is merged** after all review criteria pass.

---

## Design-engineering sync

- A weekly design-engineering sync is recommended during active implementation phases.
- Open design questions must be tracked as GitHub issues, not resolved verbally.
- Breaking design changes (changes that invalidate already-implemented screens) must be
  communicated with sufficient notice; an updated Figma frame and a new handoff issue must
  be opened.

---

## Divergence from Figma

If an engineer identifies a reason to diverge from the Figma design:

1. Document the reason in the PR description.
2. Tag the designer for review.
3. Update the Figma frame to reflect the final implementation, or create a new Figma frame
   as a "built as" variant.

Engineering must not deviate from approved Figma designs without designer acknowledgement.

---

## Handoff status tracking

Track handoff status per screen in [FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md) and in the
GitHub project board.

| Status | Meaning |
|--------|---------|
| Pending | No handoff artefacts produced. |
| Handoff ready | Designer has completed the Figma frame and opened a handoff issue. |
| In engineering | Engineer is implementing. |
| In review | Engineering PR open; awaiting design and code review. |
| Implemented | PR merged. |
