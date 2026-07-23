# Figma Completion Gate

Defines the ten completion gates (G1–G10) that must be passed before Toolly production implementation may proceed on design-dependent features.

---

## Audit status key

| Status | Meaning |
|--------|---------|
| Not started | Gate work has not begun |
| In progress | Gate work is under way |
| Evidence pending | Repository specification is complete; gate evidence not yet submitted or verified |
| Blocked | Gate cannot proceed; reason documented |
| Approved | Evidence submitted, reviewed and accepted by designated reviewer |
| Verified | Independently confirmed by a second reviewer |

> **Important:** No gate may be marked Approved or Verified without direct evidence from the Figma file. Because the Figma file has not been accessed during this repository work, all gates are currently **Evidence pending**.

---

## G1 — Brand

**Purpose:** Brand identity assets are complete and approved.

**Required evidence:**

- Figma Page 01 — Brand exists and contains all logo variants (full colour, monochrome, white).
- Minimum size and clear-space rules are documented on the page.
- Brand colour swatches reference semantic token names.
- Do/Do-not usage examples are present.

**Reviewer:** Design lead + repository owner (@shivayogih).

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G2 — Foundations

**Purpose:** Design token system is complete, accurate and linked throughout the file.

**Required evidence:**

- Figma Page 02 — Foundations exists and contains all token sections.
- All tokens in DESIGN_TOKENS.md are present in Figma as named styles or variables.
- Typography scale is complete and matches DESIGN_TOKENS.md.
- Spacing, radius, elevation and motion tokens are present.
- No hardcoded hex values used in component fills (verified by Figma token audit).

**Reviewer:** Design lead.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G3 — Components

**Purpose:** Full component library is complete with all variants and states.

**Required evidence:**

- Figma Page 03 — Components & Feedback exists.
- All component families in COMPONENT_INVENTORY.md are present.
- All states in COMPONENT_STATE_MATRIX.md are documented per component.
- No state is communicated by colour alone (verified by reviewer).
- Minimum touch targets are met (48 × 48 dp).
- All interactive components use auto-layout.
- Component names match COMPONENT_INVENTORY.md exactly.

**Reviewer:** Design lead + engineering lead.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G4 — Screen coverage

**Purpose:** All screens in SCREEN_INVENTORY.md exist in Figma with phone and tablet variants.

**Required evidence:**

- All 161 screens in SCREEN_INVENTORY.md have a corresponding Figma frame.
- Each screen has a phone variant and a tablet variant.
- Every screen has the required state variants (loading, error, offline, empty where applicable).
- Immersive screens (capture, crop, editor full-screen) show navigation hidden.
- Screens are placed in the correct Figma page per FIGMA_INFORMATION_ARCHITECTURE.md.

**Reviewer:** Design lead.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G5 — Prototypes

**Purpose:** All user flows in USER_FLOW_MATRIX.md have navigable prototype connections.

**Required evidence:**

- Figma Page 13 — Prototypes exists.
- Flows PT-01 through PT-10 have linked prototype connections.
- Each flow has a phone variant and a tablet variant.
- Happy path, alternate path and at least one failure/offline state are prototype-connected per flow.
- Prototype start frames are labelled with flow ID (e.g., "PT-01 Start").

**Reviewer:** Design lead.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G6 — Localization

**Purpose:** All screens have localized frames for all launch languages.

**Required evidence:**

- Hindi (hi-IN) frames exist for all screens in SCREEN_INVENTORY.md.
- Kannada (kn-IN) frames exist for all text-heavy screens.
- No text is clipped or overflowing in any localized frame.
- Devanagari and Kannada fonts render correctly.
- Numbers, dates and currency use locale-aware formatting.
- No user-facing text is placed in raster assets.
- Long-string variants are shown for component states that have text labels.

**Reviewer:** Design lead + Hindi/Kannada language reviewer.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G7 — Accessibility

**Purpose:** All screens and components meet WCAG 2.1 AA accessibility requirements.

**Required evidence:**

- Accessibility annotation layer present on all screens in Figma Developer Handoff page.
- Semantic labels present for all interactive elements (per ACCESSIBILITY_REQUIREMENTS.md).
- Focus order documented on each screen.
- Contrast ratios verified for all text/background combinations (tooling evidence required).
- State is not communicated by colour alone (visual audit evidence).
- Crop handle, page reorder and other drag interactions have accessible alternatives documented.
- Destructive action confirmations are fully labeled.
- Subscription comparison screen accessibility reviewed.

**Reviewer:** Design lead + accessibility reviewer.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G8 — Platforms and adaptive layouts

**Purpose:** All major workflows have correct phone and tablet layout specifications.

**Required evidence:**

- All workflow layout specifications in RESPONSIVE_LAYOUTS.md are implemented in Figma.
- Two-pane layouts exist for: home/library, settings, document editor on tablet.
- NavigationRail is implemented for tablet.
- Landscape variants exist for scan capture, crop and enhancement.
- Split-screen behaviour is documented.
- Large text scaling verified (200% scale variants or annotation).
- Foldable posture is documented (non-blocking; must be noted before GA).

**Reviewer:** Design lead + engineering lead.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G9 — Developer handoff

**Purpose:** Figma Developer Handoff page is complete and actionable for all approved screens.

**Required evidence:**

- Figma Page 14 — Developer Handoff exists.
- Annotation layer present for all approved screens.
- Token mappings present (verified against DESIGN_TOKENS.md).
- Component names in annotations match COMPONENT_INVENTORY.md.
- Accessibility semantics annotated.
- Localization notes present.
- Motion specifications documented.
- Asset export format specified.
- Phone and tablet behaviour annotated.
- Test expectations noted.

**Reviewer:** Engineering lead + design lead.

**Status:** Evidence pending.

**Blocking findings:** None documented.

**Approval date:** Not approved.

---

## G10 — Product and design sign-off

**Purpose:** Product owner and design lead confirm the complete design system is ready for production implementation.

**Required evidence:**

- All gates G1–G9 are approved.
- No outstanding blocking findings.
- Figma file version is recorded and frozen (version ID documented here).
- Product owner (@shivayogih) sign-off recorded.
- Design lead sign-off recorded.

**Reviewer:** Product owner + design lead.

**Status:** Evidence pending.

**Blocking findings:** Gates G1–G9 not yet approved.

**Approval date:** Not approved.

---

## Gate summary

| Gate | Title | Reviewer | Status | Approved |
|------|-------|----------|--------|---------|
| G1 | Brand | Design lead + @shivayogih | Evidence pending | No |
| G2 | Foundations | Design lead | Evidence pending | No |
| G3 | Components | Design lead + engineering lead | Evidence pending | No |
| G4 | Screen coverage | Design lead | Evidence pending | No |
| G5 | Prototypes | Design lead | Evidence pending | No |
| G6 | Localization | Design lead + language reviewer | Evidence pending | No |
| G7 | Accessibility | Design lead + accessibility reviewer | Evidence pending | No |
| G8 | Platforms and adaptive | Design lead + engineering lead | Evidence pending | No |
| G9 | Developer handoff | Engineering lead + design lead | Evidence pending | No |
| G10 | Product sign-off | Product owner + design lead | Evidence pending | No |

---

## Gate dependency

Implementation of production features on design-dependent screens is blocked until:

- The relevant screen's Figma frame is covered under G4 (screen coverage — Approved).
- The component library (G3) is Approved.
- The developer handoff (G9) for that screen is Approved.
- Overall product sign-off (G10) is Approved.

See PRODUCTION_GATE.md for the full production readiness gate.
