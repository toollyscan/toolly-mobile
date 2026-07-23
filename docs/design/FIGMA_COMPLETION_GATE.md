# Figma Completion Gate

Engineering implementation of any screen or component is blocked until the corresponding
Figma frame passes every applicable item in this gate.

This gate must be reviewed by `@shivayogih` before handoff to engineering. The gate applies
to each screen individually; it is not a single global approval.

---

## Per-screen gate checklist

For each screen in [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md), all applicable items below
must be checked before that screen is handed off to engineering.

### Design completeness

- [ ] The Figma frame for this screen exists and is named using the canonical screen ID from
      [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md).
- [ ] All required component states are present (see [COMPONENT_STATE_MATRIX.md](COMPONENT_STATE_MATRIX.md)).
- [ ] Design tokens are applied; no hardcoded colours, font sizes or spacing values.
- [ ] The design is complete at the phone (compact) breakpoint.
- [ ] Responsive variants are complete for all required breakpoints
      (see [RESPONSIVE_LAYOUTS.md](RESPONSIVE_LAYOUTS.md)).
- [ ] Landscape orientation variant is present if required (camera screens; library).

### Content

- [ ] All strings are present and follow [CONTENT_GUIDELINES.md](CONTENT_GUIDELINES.md).
- [ ] String keys are annotated in the Figma frame.
- [ ] Hindi and Kannada string variants are present in the Localisation Variants Figma page
      for this screen (see [LOCALIZATION_REQUIREMENTS.md](LOCALIZATION_REQUIREMENTS.md)).
- [ ] No unresolved placeholder text ("Lorem ipsum", "TODO", "[Placeholder]").

### Accessibility

- [ ] Accessibility annotations layer is present in the Figma frame.
- [ ] All interactive elements have content description / accessibility label annotations.
- [ ] Focus order is annotated.
- [ ] Touch target sizes meet the minimums in [ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md).
- [ ] Colour contrast of all text and non-text elements meets WCAG 2.1 AA (4.5 : 1 for
      normal text; 3 : 1 for large text and non-text elements).
- [ ] State must not be communicated by colour alone; icons or text labels are present.

### Assets

- [ ] All icons used in this screen are present in the Component / Iconography Figma page.
- [ ] Icons are named using the `ic_{name}_{size}` convention.
- [ ] Any illustrations are complete and named using the `il_{name}` convention.
- [ ] Export settings are configured for all assets.

### Design review

- [ ] The screen has been reviewed by at least one other person (peer design review).
- [ ] All design review comments on this frame are resolved or explicitly deferred with a
      linked issue.
- [ ] The screen has been reviewed by `@shivayogih` and approved for handoff.

---

## Global gate items

The following items apply once to the whole Figma file, not per screen.

### Design system

- [ ] Design System Figma page is complete: colour tokens, typography, spacing, elevation,
      iconography.
- [ ] All component variants and states are designed in the Components Figma page.
- [ ] Figma styles or variables are used for all tokens (no un-tokenised values).

### Information architecture

- [ ] All 52 screens in [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md) have a Figma frame.
- [ ] Figma page and frame hierarchy matches
      [FIGMA_INFORMATION_ARCHITECTURE.md](FIGMA_INFORMATION_ARCHITECTURE.md).
- [ ] Naming convention is consistent throughout the file.

### Flows

- [ ] All 14 flows in [USER_FLOW_MATRIX.md](USER_FLOW_MATRIX.md) are represented in the
      Figma file with prototype connections or flow annotations.

---

## Figma file evidence

The following evidence must be recorded before the global gate is considered complete.

| Evidence item | Status |
|--------------|--------|
| Figma file URL | Pending — not yet recorded |
| Figma file last-updated date | Pending |
| Screen count verified against SCREEN_INVENTORY.md | Pending |
| Component count verified against COMPONENT_INVENTORY.md | Pending |
| Contrast audit completed for all colour tokens | Pending |
| Design review sign-off by @shivayogih | Pending |

---

## Sign-off

| Item | Reviewer | Date | Signature |
|------|----------|------|-----------|
| Design system complete | shivayogih | | |
| All screens complete | shivayogih | | |
| Global gate approved | shivayogih | | |

---

## Relationship to the Production Gate

The Figma Completion Gate is a prerequisite for engineering implementation but is separate
from the [Production Gate](../execution/PRODUCTION_GATE.md). Passing the Figma Completion
Gate allows implementation to begin; the Production Gate governs when production features
can be merged to `main`.
