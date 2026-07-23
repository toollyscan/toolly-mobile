# Figma Information Architecture

Defines the required page structure of the Toolly Figma file.

Figma file reference: `https://figma.com/design/86LhFLYYzUNQ1upWbNfDI6/Toolly-Scan-—-Low-Fidelity-Product-Flows`

---

## Audit status key

| Status | Meaning |
|--------|---------|
| Not started | Work has not begun |
| In progress | Work is under way |
| Evidence pending | Repository specification is complete; live Figma audit has not been performed |
| Blocked | Cannot proceed; reason documented |
| Approved | Evidence reviewed and accepted by designated reviewer |
| Verified | Independently confirmed by a second reviewer |

> **Important:** No gate below may be marked Approved or Verified without direct evidence from the Figma file. The current status of all gates is **Evidence pending** because a live Figma audit has not been performed.

---

## Required Figma pages

### Page 00 — Cover & Status

**Purpose:** Single-page project overview showing design system version, completion status and gate dashboard.

**Required sections:**

- Project title and version
- Completion gate dashboard (G1–G10)
- Owner and reviewer contacts
- Last-updated timestamp

**Required frames:**

- Cover card
- Gate summary table

**Required components:** None (informational only).

**Required states:** Not applicable.

**Prototype dependencies:** None.

**Review owner:** Design lead.

**Completion evidence:** Frame visible in Figma with accurate gate statuses.

**Status:** Evidence pending.

---

### Page 01 — Brand

**Purpose:** Brand identity assets — logo, wordmark, icon, colour palette, brand voice.

**Required sections:**

- Logo variants (full colour, monochrome, white)
- Minimum size and clear-space rules
- Brand colour swatches linked to Design Tokens
- Do / Do-not usage examples

**Required frames:**

- Logo — primary
- Logo — compact
- Brand colours
- Do / Do-not

**Required components:** None (reference page).

**Required states:** Not applicable.

**Prototype dependencies:** None.

**Review owner:** Design lead + repository owner.

**Completion evidence:** All logo variants exported; colour swatches reference token names.

**Status:** Evidence pending.

---

### Page 02 — Foundations

**Purpose:** Design token reference — colour, typography, spacing, radius, elevation and motion.

**Required sections:**

- Colour tokens (semantic and primitive)
- Typography scale
- Spacing scale
- Radius scale
- Elevation / shadow
- Motion easing and duration
- Minimum touch-target reference

**Required frames:**

- Colour tokens
- Type scale
- Spacing scale
- Radius scale
- Elevation
- Motion tokens

**Required components:** Token swatch component, type sample component.

**Required states:** Not applicable.

**Prototype dependencies:** None.

**Review owner:** Design lead.

**Completion evidence:** All tokens from DESIGN_TOKENS.md present; semantic names visible.

**Status:** Evidence pending.

---

### Page 03 — Components & Feedback

**Purpose:** Full component library with all variants and interactive states.

**Required sections:**

- Navigation
- Buttons and icon buttons
- Inputs and OTP fields
- Search and filters
- Cards and thumbnails
- Status, chips and indicators
- Dialogs, sheets and snackbars
- Progress and loading
- Empty and error states

**Required frames:** One main frame per component family; sub-frames per variant group.

**Required components:** All families listed in COMPONENT_INVENTORY.md.

**Required states:** All states listed in COMPONENT_STATE_MATRIX.md.

**Prototype dependencies:** Interactive-component prototype links used in Page 13.

**Review owner:** Design lead.

**Completion evidence:** All component families present; interactive states documented; auto-layout verified.

**Status:** Evidence pending.

---

### Page 04 — Onboarding & Auth

**Purpose:** All authentication and onboarding screens for both phone and tablet.

**Required sections:**

- Splash and welcome
- Login method selection
- Phone-number entry and OTP
- Email/password login
- Google and Apple auth
- Profile completion
- Auth error and recovery

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Launch and authentication; separate phone and tablet variants.

**Required components:** OTP field, auth button, number pad, loading indicator, error banner.

**Required states:** Default, loading, error, offline, recovery.

**Prototype dependencies:** Prototype flows PT-01, PT-02, PT-07 (see USER_FLOW_MATRIX.md).

**Review owner:** Design lead.

**Completion evidence:** All screens in SCREEN_INVENTORY.md §Launch and authentication present; phone and tablet variants exist.

**Status:** Evidence pending.

---

### Page 05 — Home & Library

**Purpose:** Home feed, document library, search, filters and selection mode.

**Required sections:**

- Empty and populated home
- Document list and grid
- Search and filter panel
- Selection mode
- Offline and sync status
- Tablet two-pane layout

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Home and library; phone and tablet variants.

**Required components:** Document card, thumbnail, search bar, filter chip, sort sheet, selection bar, offline indicator, sync indicator.

**Required states:** Empty, loading, populated, search active, offline, error.

**Prototype dependencies:** Flows PT-02, PT-04, PT-05.

**Review owner:** Design lead.

**Completion evidence:** All home and library screens present; two-pane tablet layout verified.

**Status:** Evidence pending.

---

### Page 06 — Scan Capture

**Purpose:** Camera capture screens, permission flows and batch capture.

**Required sections:**

- Permission introduction and denial
- Live preview and capture controls
- Auto and manual capture
- Batch capture
- Flash and gallery import
- Capture warnings (blur, glare, low-light, edge failure)
- Processing, failure and recovery
- Post-capture review

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Scan capture; phone and tablet variants; immersive layout documented.

**Required components:** Camera control bar, flash toggle, batch counter, capture button, warning overlay, processing indicator, edge-detect overlay.

**Required states:** Default, auto-detecting, warning active, processing, error, immersive (no bottom navigation).

**Prototype dependencies:** Flows PT-01, PT-03, PT-10.

**Review owner:** Design lead.

**Completion evidence:** All capture screens present; immersive layout shows hidden navigation.

**Status:** Evidence pending.

---

### Page 07 — Crop & Enhance

**Purpose:** Manual crop, perspective correction and image enhancement screens.

**Required sections:**

- Automatic edge result
- Manual crop with corner handles
- Magnified handle adjustment
- Rotation and perspective
- Enhancement modes (auto, colour, greyscale, B&W)
- Processing, reset, undo, confirm
- Tablet large-page workspace

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Crop and enhancement; phone and tablet variants; immersive layout.

**Required components:** Crop handle, rotation dial, enhancement toggle, processing overlay, undo/redo controls.

**Required states:** Default, handle active, processing, confirm, error, immersive.

**Prototype dependencies:** Flows PT-01, PT-03.

**Review owner:** Design lead.

**Completion evidence:** All crop and enhancement screens present; magnified-handle frame included.

**Status:** Evidence pending.

---

### Page 08 — Document Editor & OCR

**Purpose:** Multi-page document editor, OCR and autosave states.

**Required sections:**

- Document preview and page thumbnails
- Page management (reorder, add, delete, rotate, replace)
- Document metadata and rename
- OCR states (pending, success, partial, unavailable)
- Autosave and save failure
- Offline editing and conflict
- Undo destructive action

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Document editor; phone and tablet variants.

**Required components:** Page thumbnail, thumbnail drag handle, add-page button, OCR indicator, autosave indicator, conflict banner, rename dialog.

**Required states:** Default, reordering, OCR pending, OCR success, save failure, offline, conflict.

**Prototype dependencies:** Flow PT-03.

**Review owner:** Design lead.

**Completion evidence:** All editor screens present; tablet wide layout verified.

**Status:** Evidence pending.

---

### Page 09 — Export & Share

**Purpose:** Export format selection, progress and share sheets.

**Required sections:**

- Format and quality selection
- PDF and image export
- Premium export states
- Export progress, success and failure
- Share, print and save-to-device
- Large-document warning

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Export and share.

**Required components:** Format selector, quality slider, progress bar, share sheet, permission error dialog, large-doc warning sheet.

**Required states:** Default, processing, success, error, premium locked, offline.

**Prototype dependencies:** None (linear export flow).

**Review owner:** Design lead.

**Completion evidence:** All export screens present; premium states visually distinct.

**Status:** Evidence pending.

---

### Page 10 — Backup, Sync & Security

**Purpose:** Backup introduction, progress, recovery and trusted-device management.

**Required sections:**

- Backup disabled and introduction
- Backup progress and pause
- Quota and offline states
- Sync conflict
- Trusted-device management
- Recovery phrase and verification
- Restore progress and outcomes

**Required frames:** One frame per screen listed in SCREEN_INVENTORY.md §Backup, sync and security.

**Required components:** Backup toggle, progress indicator, device trust card, recovery phrase display, restore progress bar.

**Required states:** Disabled, enabled, progress, paused, error, quota exceeded, offline, conflict.

**Prototype dependencies:** Flows PT-06, PT-07.

**Review owner:** Design lead.

**Completion evidence:** All backup and recovery screens present; "support cannot decrypt" explanation included.

**Status:** Evidence pending.

---

### Page 11 — Subscription, Profile & Settings

**Purpose:** Subscription paywall, purchase flow and profile/settings screens.

**Required sections:**

- Free and premium comparison
- Purchase flow (monthly, annual, trial)
- Purchase states (pending, success, failure, restore)
- Lifecycle states (expired, cancelled, grace, revoked)
- Profile and edit
- All settings screens listed in SCREEN_INVENTORY.md §Profile and settings

**Required frames:** One frame per screen in SCREEN_INVENTORY.md §Subscription and §Profile and settings.

**Required components:** Plan card, plan comparison table, purchase button, subscription status chip, settings row, danger-zone row.

**Required states:** Default, trial eligible, pending, success, error, grace period, expired, offline entitlement.

**Prototype dependencies:** Flow PT-08.

**Review owner:** Design lead.

**Completion evidence:** All subscription lifecycle states present; account deletion requires confirmation; no preselected paid option.

**Status:** Evidence pending.

---

### Page 12 — States & System

**Purpose:** System-level patterns — loading, empty, error, offline and permission states.

**Required sections:**

- Loading skeletons per major screen
- Empty-state illustrations
- Error states (network, storage, auth, corruption)
- Permission-denied screens
- System dialogs and alerts
- Destructive-action confirmation patterns

**Required frames:** One frame set per state pattern.

**Required components:** Skeleton, empty-state, error card, permission card, system dialog.

**Required states:** Loading, empty, error, permission denied, offline.

**Prototype dependencies:** Flows PT-05, PT-10.

**Review owner:** Design lead.

**Completion evidence:** All state patterns present; no colour-only indication.

**Status:** Evidence pending.

---

### Page 13 — Prototypes

**Purpose:** Linked interactive prototypes for all flows in USER_FLOW_MATRIX.md.

**Required sections:**

- One prototype connection set per user flow (PT-01 through PT-10)
- Phone and tablet variants

**Required frames:** Starting frames for each prototype flow.

**Required components:** None (uses frames from Pages 04–12).

**Required states:** Happy path, alternate path and key error/offline states.

**Prototype dependencies:** All flows in USER_FLOW_MATRIX.md.

**Review owner:** Design lead.

**Completion evidence:** All prototype flows navigable in Figma; phone and tablet variants linked.

**Status:** Evidence pending.

---

### Page 14 — Developer Handoff

**Purpose:** Production-ready annotated frames with token mappings, accessibility semantics and handoff notes.

**Required sections:**

- Annotated screen set for each major workflow
- Token mapping tables
- Component-to-design-system name mapping
- Accessibility annotation layer
- Motion specification
- Asset export set

**Required frames:** Annotated variants of approved screens.

**Required components:** Annotation overlay component.

**Required states:** As per source component.

**Prototype dependencies:** None.

**Review owner:** Engineering lead + design lead.

**Completion evidence:** Annotations present; token names match DESIGN_TOKENS.md; component names match COMPONENT_INVENTORY.md.

**Status:** Evidence pending.

---

### Page 99 — Archive

**Purpose:** Superseded frames, rejected directions and historical reference.

**Required sections:**

- Archived low-fidelity flows from original Figma file
- Rejected design directions with rationale notes

**Required frames:** None mandated; historical frames moved here when superseded.

**Required components:** Not applicable.

**Required states:** Not applicable.

**Prototype dependencies:** None.

**Review owner:** Design lead.

**Completion evidence:** Original low-fidelity flows preserved and clearly labelled as archived.

**Status:** Evidence pending.

---

## Live Figma audit status

Repository specification: **Complete** (this document).

Live Figma audit: **Blocked** — the Figma file has not been accessed during this repository work. Visual completion has not been verified. Product implementation remains gated on Figma completion gate G1–G10 (see FIGMA_COMPLETION_GATE.md).
