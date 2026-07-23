# Component State Matrix

This document defines the required visual states for every component in the
[COMPONENT_INVENTORY.md](COMPONENT_INVENTORY.md). Each state must be designed in Figma
before engineering implementation.

Figma design status for all component states is **evidence-pending**; see
[FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

---

## State definitions

| State | Description |
|-------|-------------|
| Default | Resting state; no interaction in progress. |
| Hover | Pointer hovering (relevant on iPad with pointer device). |
| Pressed | User's finger or pointer is actively pressing. |
| Focused | Keyboard focus (accessibility: Tab key / switch access). |
| Filled | Input field contains a value. |
| Disabled | Component is not interactive; greyed out. |
| Loading | Action triggered; awaiting response. |
| Error | Invalid input or failed action. |
| Success | Action completed successfully. |
| Selected | Item is selected in a list or group. |
| Indeterminate | Partial selection (e.g., checkbox in multi-select). |
| Empty | Container has no content. |

---

## Buttons

| Component | Default | Hover | Pressed | Disabled | Loading | Notes |
|-----------|---------|-------|---------|----------|---------|-------|
| BTN-001 Primary Button | ✓ | ✓ | ✓ | ✓ | ✓ | Loading state shows spinner inside button; label hidden. |
| BTN-002 Secondary Button | ✓ | ✓ | ✓ | ✓ | — | |
| BTN-003 Text Button | ✓ | ✓ | ✓ | ✓ | — | |
| BTN-004 Icon Button | ✓ | ✓ | ✓ | ✓ | — | |
| BTN-005 Destructive Button | ✓ | ✓ | ✓ | ✓ | ✓ | Loading state required for delete-account confirmation. |
| BTN-006 FAB — Capture | ✓ | ✓ | ✓ | — | — | No disabled state; capture is always available. |
| BTN-007 FAB — Add | ✓ | ✓ | ✓ | — | — | |

---

## Input fields

| Component | Default | Focused | Filled | Error | Disabled | Notes |
|-----------|---------|---------|--------|-------|----------|-------|
| INP-001 Text Input | ✓ | ✓ | ✓ | ✓ | ✓ | Error state shows red border and inline error message. |
| INP-002 Phone Number Input | ✓ | ✓ | ✓ | ✓ | — | +91 prefix always visible; error if invalid format. |
| INP-003 OTP Input | ✓ | ✓ (per digit) | ✓ | ✓ | — | Active-digit state: cursor in current box. Error: all boxes red. |
| INP-004 Search Input | ✓ | ✓ | ✓ | — | — | Clear button appears only in Filled state. |
| INP-005 Document Name Input | ✓ | ✓ | ✓ | ✓ | — | Error if name is empty or exceeds character limit. |

---

## Cards

| Component | Default | Pressed | Selected | Empty | Notes |
|-----------|---------|---------|----------|-------|-------|
| CRD-001 Document Card — Grid | ✓ | ✓ | ✓ | — | Selected state: highlighted border or checkbox overlay. |
| CRD-002 Document Card — List | ✓ | ✓ | ✓ | — | |
| CRD-003 Folder Card | ✓ | ✓ | — | — | No selection state; folders are not multi-selectable in V1. |
| CRD-004 Subscription Plan Card | ✓ | ✓ | ✓ | — | Selected state: filled border and checkmark. |
| CRD-005 Backup Summary Card | Idle | In Progress | — | — | States: Idle, In Progress, Complete, Error. |

---

## Navigation

| Component | Default | Active tab | Inactive tab | Pressed | Notes |
|-----------|---------|------------|--------------|---------|-------|
| NAV-001 Bottom Navigation Bar | — | ✓ | ✓ | ✓ | Active: filled icon + label. Inactive: outline icon. |
| NAV-002 Top App Bar | ✓ | — | — | ✓ (actions) | |
| NAV-003 Breadcrumb | ✓ | — | — | ✓ (tappable segments) | |
| NAV-004 Back Button | ✓ | — | — | ✓ | |

---

## Dialogs and sheets

| Component | Default | Expanded | Collapsed | Notes |
|-----------|---------|----------|-----------|-------|
| DLG-001 Alert Dialog | ✓ | — | — | Two button states: confirm (may be destructive) and cancel. |
| DLG-002 Bottom Sheet | — | ✓ | ✓ | Handle indicator; drag-to-dismiss. |
| DLG-003 Folder Picker Sheet | Default | — | — | Empty state when no folders exist. |
| DLG-004 Export Options Sheet | Free | Premium unlocked | Premium locked | Locked premium options are shown but visually dimmed with a lock icon. |
| DLG-005 Language Picker | Default | — | — | Selected language is highlighted. |

---

## Feedback and status

| Component | Default | Success | Error | Info / Warning | Loading | Empty | Notes |
|-----------|---------|---------|-------|----------------|---------|-------|-------|
| FDB-001 Toast / Snackbar | — | ✓ | ✓ | ✓ | — | — | Auto-dismiss after 4 s. |
| FDB-002 Inline Error | ✓ | — | — | — | — | — | |
| FDB-003 Loading Spinner | ✓ | — | — | — | — | — | |
| FDB-004 Progress Bar | — | — | — | — | ✓ | — | Shows percentage where available. |
| FDB-005 Skeleton Loader | ✓ | — | — | — | — | — | Animated shimmer. |
| FDB-006 Empty State | — | — | ✓ | ✓ | — | ✓ | Three variants: no documents, no results, error. |
| FDB-007 Banner | — | — | ✓ | ✓ | — | — | Warning banner: backup paused, subscription expired. |

---

## Camera and capture

| Component | Default | Active / Detecting | Detected | Manual | Capturing | Notes |
|-----------|---------|-------------------|----------|--------|-----------|-------|
| CAM-001 Camera Viewfinder Overlay | ✓ (searching) | ✓ | ✓ | ✓ | — | Searching: animated corner marks. Detected: green outline. Manual: draggable overlay. |
| CAM-002 Capture Button | ✓ (Ready) | — | — | — | ✓ | Capturing: brief scale animation. |
| CAM-003 Flash Toggle | Off | On | Auto | — | — | Cycles through three modes. |
| CAM-004 Page Counter Badge | ✓ | — | — | — | — | Count increments after each captured page. |
| CAM-005 Crop Handle | ✓ | Dragging | — | — | — | |
| CAM-006 Enhancement Mode Selector | Colour (default) | Greyscale | B&W | — | — | |

---

## Badges, chips and labels

| Component | Default | Notes |
|-----------|---------|-------|
| LBL-001 Premium Badge | ✓ | May appear as a lock icon variant for locked premium features. |
| LBL-002 Page Count Chip | ✓ | |
| LBL-003 Status Chip | Pending, In progress, Complete, Error | Four required state variants. |

---

## Controls

| Component | Unchecked / Off | Checked / On | Indeterminate | Disabled | Notes |
|-----------|-----------------|--------------|---------------|----------|-------|
| CTL-001 Toggle Switch | ✓ (Off) | ✓ (On) | — | ✓ | |
| CTL-002 Checkbox | ✓ | ✓ | ✓ | ✓ | |
| CTL-003 Radio Button | ✓ | ✓ | — | ✓ | |
| CTL-004 Slider | ✓ | — | — | ✓ | Dragging state required. |

---

## Accessibility state requirements

Every interactive component must additionally support:

| State | Required by |
|-------|------------|
| Keyboard focus ring | WCAG 2.1 SC 2.4.7 |
| High-contrast mode variant | DA-005 |
| Minimum touch target 48 × 48 dp / pt | Google Material / Apple HIG |
| Content description / accessibility label | TalkBack, VoiceOver |

See [ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md) for full requirements.
