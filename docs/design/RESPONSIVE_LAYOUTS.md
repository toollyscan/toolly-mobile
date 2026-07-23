# Responsive Layouts

This document defines the layout specifications for all supported form factors in the Toolly
V1 application. All layout breakpoints must be designed in Figma before engineering
implementation.

Figma design status for responsive variants is **evidence-pending**; see
[FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

Design audit items DA-007 (Android tablet) and DA-008 (iPad) are open. Tablet layouts are
required before general availability but do not gate the phone launch.

---

## Supported form factors

| Form factor | Platform | Breakpoint | V1 priority |
|-------------|----------|-----------|-------------|
| Android phone (compact) | Android | < 600 dp width | Primary — V1 launch |
| Android phone (medium) | Android | 600–840 dp width | V1 launch |
| Android tablet | Android | ≥ 840 dp width | Required before GA (DA-007) |
| iPhone (compact) | iOS | < 390 pt width | V1 launch |
| iPhone (regular) | iOS | ≥ 390 pt width | V1 launch |
| iPad | iOS | ≥ 744 pt width | Required before GA (DA-008) |

---

## Grid system

Toolly uses a 4 dp / pt base grid with the following column configurations:

| Breakpoint | Columns | Margin | Gutter | Max content width |
|-----------|---------|--------|--------|------------------|
| Compact (< 600 dp) | 4 | 16 dp | 16 dp | Full width |
| Medium (600–840 dp) | 8 | 24 dp | 16 dp | Full width |
| Expanded (≥ 840 dp) | 12 | 24 dp | 24 dp | 1,280 dp |

---

## Document library — LIB-001

### Compact (phone)

- Single-column list layout by default.
- Optional: 2-column grid if the user prefers grid view.
- Bottom navigation bar visible.
- FAB (capture) fixed at bottom-right.

### Medium (large phone / narrow tablet)

- 2-column grid for documents.
- Bottom navigation bar visible.
- FAB visible.

### Expanded (tablet)

- Navigation drawer on the left (persistent at ≥ 840 dp).
- Document list / grid in the main content pane.
- Optional: two-pane layout with document list on the left and document preview on the right.
- FAB visible in the list pane.
- Bottom navigation replaced by navigation rail or navigation drawer per platform guidelines.

---

## Document detail — LIB-003

### Document detail — compact (phone)

- Full-screen page viewer.
- Action bar with share, export and more-options icons.

### Document detail — expanded (tablet)

- Page thumbnail strip on the left or bottom.
- Full-page preview in the main pane.
- Action buttons in the side panel or toolbar.

---

## Camera viewfinder — CAP-001

### Camera viewfinder — phone

- Full-screen camera viewfinder.
- Capture button centred at the bottom.
- Flash and close icons at the top.

### Camera viewfinder — tablet

- Full-screen viewfinder (camera is the primary focus; layout adapts to landscape).
- Capture controls positioned to be reachable on a tablet form factor.
- Edge-detection overlay scales to full viewfinder dimensions.

---

## Settings — SET-001

### Settings — compact (phone)

- Single-column settings list.

### Settings — expanded (tablet)

- Two-pane: settings list on the left, detail pane on the right.

---

## Orientation support

| Screen | Portrait | Landscape |
|--------|----------|-----------|
| Document library | Required | Required |
| Camera viewfinder | Required | Required (primary capture mode may be landscape) |
| Document detail | Required | Required |
| Authentication screens | Required | Required |
| Settings | Required | Optional |

Landscape layout must be designed for the camera viewfinder and document library at minimum.
Other screens may use portrait constraints in landscape without a dedicated redesign for V1.

---

## Safe areas and system UI

- All layouts must respect platform safe areas (notch, home indicator, navigation bar).
- Bottom navigation must not be obscured by the Android gesture navigation bar or iOS home
  indicator.
- On Android, layouts must account for the status bar, navigation bar and gesture navigation.
- On iOS, layouts must account for the Dynamic Island / notch and home indicator.

---

## Typography scale at breakpoints

Text sizes do not change between breakpoints. Dynamic type (user-adjustable text size) is
supported at all breakpoints. See [ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md)
for Dynamic Type requirements.

---

## Evidence status

| Breakpoint | Figma frame | Status |
|-----------|------------|--------|
| Compact phone — all flows | — | Pending |
| Medium phone — library, capture | — | Pending |
| Android tablet — library, capture, settings | — | Pending (DA-007) |
| iPad — library, capture, settings | — | Pending (DA-008) |
| Landscape — camera | — | Pending |
