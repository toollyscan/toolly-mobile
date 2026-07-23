# Component Inventory

Complete inventory of reusable UI components required for the Toolly V1 application. Each
component must be designed in Figma with all required states before engineering implementation.

Component states are defined in [COMPONENT_STATE_MATRIX.md](COMPONENT_STATE_MATRIX.md).
Design tokens referenced here are defined in [DESIGN_TOKENS.md](DESIGN_TOKENS.md).

Figma design status for all components is **evidence-pending**; see
[FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

---

## Component naming convention

Components follow the pattern `Category / Name / Variant`. In code, components will be named
by their domain role (e.g., `CaptureButton`, `DocumentCard`) following Clean Architecture
conventions. The Figma names in this document are for design alignment only.

---

## Buttons

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| BTN-001 | Primary Button | Default, Hover, Pressed, Disabled, Loading | Full-width or flexible-width; filled background. Used for primary CTAs. |
| BTN-002 | Secondary Button | Default, Hover, Pressed, Disabled | Outlined; used for secondary actions alongside a primary button. |
| BTN-003 | Text Button | Default, Hover, Pressed, Disabled | No background or border; used for lower-emphasis actions. |
| BTN-004 | Icon Button | Default, Hover, Pressed, Disabled | Icon only; used in toolbars and dense layouts. |
| BTN-005 | Destructive Button | Default, Hover, Pressed, Disabled | Filled red; used only for irreversible destructive actions (e.g., delete account). |
| BTN-006 | FAB — Capture | Default, Hover, Pressed | Floating action button; camera icon; triggers document capture. |
| BTN-007 | FAB — Add | Default, Hover, Pressed | Floating action button; plus icon; used for add-folder or similar. |

---

## Input fields

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| INP-001 | Text Input | Default, Focused, Filled, Error, Disabled | Standard single-line text input with label and helper text. |
| INP-002 | Phone Number Input | Default, Focused, Filled, Error | Pre-populated +91 prefix; 10-digit entry; inline format validation. |
| INP-003 | OTP Input | Default, Active digit, Filled, Error | 6 individual digit boxes; auto-advance on input. |
| INP-004 | Search Input | Default, Focused, Filled, Clear | Search bar with magnifier icon and clear button. |
| INP-005 | Document Name Input | Default, Focused, Filled, Error | Single-line input for document and folder names; character limit indicator. |

---

## Cards

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| CRD-001 | Document Card — Grid | Default, Pressed, Selected | Thumbnail + title + date; used in grid layout. |
| CRD-002 | Document Card — List | Default, Pressed, Selected | Smaller thumbnail + title + page count + date; used in list layout. |
| CRD-003 | Folder Card | Default, Pressed | Folder icon + name + document count. |
| CRD-004 | Subscription Plan Card | Default, Selected, Featured | Plan name, price and billing period; checkmark when selected. |
| CRD-005 | Backup Summary Card | Idle, In Progress, Complete, Error | Shows last-backup timestamp, storage used and status. |

---

## Navigation

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| NAV-001 | Bottom Navigation Bar | With 3 tabs | Library, Scan, Settings. Active and inactive tab states. |
| NAV-002 | Top App Bar | Default, With Back, With Actions | Persistent top bar; title; optional back arrow and action icons. |
| NAV-003 | Breadcrumb | 1-level, 2-level | Folder path display in folder view. |
| NAV-004 | Back Button / Arrow | Default, Pressed | Standard back navigation control. |

---

## Dialogs and sheets

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| DLG-001 | Alert Dialog | Destructive, Informational | Two-button modal for confirmations (e.g., delete). |
| DLG-002 | Bottom Sheet | Short, Tall | Draggable bottom sheet container for options and pickers. |
| DLG-003 | Folder Picker Sheet | Default, With folders, Empty | Bottom sheet listing folders for move-to-folder action. |
| DLG-004 | Export Options Sheet | Free options, Premium options unlocked, Premium locked | Export format selector. |
| DLG-005 | Language Picker | Default | Language selection bottom sheet or full-screen picker. |

---

## Feedback and status

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| FDB-001 | Toast / Snackbar | Success, Error, Info | Non-blocking inline notification at bottom of screen. |
| FDB-002 | Inline Error | Field-level | Error text shown below an input field. |
| FDB-003 | Loading Spinner | Default | Circular indeterminate progress indicator. |
| FDB-004 | Progress Bar | Determinate | Horizontal bar for upload/download progress. |
| FDB-005 | Skeleton Loader | Document card, List item | Placeholder while content is loading. |
| FDB-006 | Empty State | No documents, No results, Error | Illustration, heading and body text; optional CTA. |
| FDB-007 | Banner | Info, Warning | Full-width contextual banner (e.g., backup paused). |

---

## Camera and capture

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| CAM-001 | Camera Viewfinder Overlay | Searching, Detected, Manual | Semi-transparent overlay showing detected document edges. |
| CAM-002 | Capture Button | Ready, Capturing | Large circular shutter button. |
| CAM-003 | Flash Toggle | Off, On, Auto | Icon button for flash mode selection. |
| CAM-004 | Page Counter Badge | With count | Shows current page number during multi-page capture. |
| CAM-005 | Crop Handle | Default, Dragging | Corner handle for manual crop adjustment. |
| CAM-006 | Enhancement Mode Selector | Colour, Greyscale, B&W | Segmented control for image output mode. |

---

## Badges, chips and labels

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| LBL-001 | Premium Badge | Default | "Premium" label; shown on premium-only features. |
| LBL-002 | Page Count Chip | Default | Shows page count on a document card. |
| LBL-003 | Status Chip | Pending, In progress, Complete, Error | Small status indicator for backup and export. |

---

## Controls

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| CTL-001 | Toggle Switch | On, Off, Disabled | Used for backup enable/disable and notification preferences. |
| CTL-002 | Checkbox | Unchecked, Checked, Indeterminate, Disabled | Used in multi-select flows (page selection). |
| CTL-003 | Radio Button | Unselected, Selected, Disabled | Used in export options and language picker. |
| CTL-004 | Slider | Default | Horizontal slider for enhancement intensity (future). |

---

## Miscellaneous

| Component ID | Name | Variants | Description |
|--------------|------|----------|-------------|
| MSC-001 | Section Divider | Default | Horizontal rule between sections in lists and settings. |
| MSC-002 | Avatar / Account Icon | Guest, Authenticated | Small icon representing the user's account state in the top bar. |
| MSC-003 | Toolly Logo | Full, Icon only | Brand mark used on the splash/welcome screen. |
| MSC-004 | OTP Timer | Counting, Expired | Countdown timer on OTP entry screen. |

---

## Component count summary

| Category | Count |
|----------|-------|
| Buttons | 7 |
| Input fields | 5 |
| Cards | 5 |
| Navigation | 4 |
| Dialogs and sheets | 5 |
| Feedback and status | 7 |
| Camera and capture | 6 |
| Badges, chips and labels | 3 |
| Controls | 4 |
| Miscellaneous | 4 |
| **Total** | **50** |
