# Figma Information Architecture

This document defines the expected structure of the Toolly Figma file and serves as the
specification that the Figma designer must implement. Actual Figma implementation is
evidence-pending; see [FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

Figma node IDs are not recorded here. Node IDs are assigned by Figma when frames are created
and must be extracted from the live file; they must not be invented.

---

## Purpose

This document:

1. Defines the required page and frame hierarchy for the Toolly Figma file.
2. Provides the canonical screen naming convention used in [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md).
3. Acts as the checklist the Figma designer works against.
4. Acts as the completion gate input for [FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md).

---

## Figma file structure

The Toolly Figma file must be organised into the following pages. Each page contains the
frames described below.

### Page: Cover

| Frame | Description |
|-------|-------------|
| Cover | Project name, version, last-updated date and contributors. |

---

### Page: Design System

This page contains all foundational design tokens, typography, colour swatches and spacing
guides. See [DESIGN_TOKENS.md](DESIGN_TOKENS.md) for the token specification.

| Frame | Description |
|-------|-------------|
| Colour Palette | All brand, semantic and neutral colour tokens. |
| Typography | All text styles: display, heading, body, caption, label. |
| Spacing | Spacing scale visualisation (4 px base grid). |
| Elevation / Shadow | Shadow tokens for cards and modals. |
| Iconography | Icon library reference grid. |
| Illustration Placeholders | Named placeholder frames for any custom illustrations. |

---

### Page: Components

This page contains all reusable UI components at all defined states. See
[COMPONENT_INVENTORY.md](COMPONENT_INVENTORY.md) and
[COMPONENT_STATE_MATRIX.md](COMPONENT_STATE_MATRIX.md).

| Frame | Description |
|-------|-------------|
| Buttons | Primary, secondary, text, icon and destructive variants in all states. |
| Input Fields | Text, phone-number, OTP, search and password variants in all states. |
| Cards | Document card, folder card, list-item variants. |
| Navigation | Bottom navigation bar, top app bar, back arrow. |
| Dialogs and Sheets | Alert dialog, confirmation dialog, bottom sheet. |
| Feedback | Toast/snackbar, inline error, loading spinner, skeleton. |
| Badges and Pills | Premium badge, label chip, tag. |
| Toggles and Switches | Toggle switch, checkbox, radio button. |
| Sliders | Horizontal slider (enhancement controls). |
| FAB | Floating action button: capture, add. |
| Camera Overlay | Viewfinder, edge-detection overlay, capture button. |

---

### Page: Flows — Authentication

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| AUTH-001 | AUTH | Onboarding splash / welcome |
| AUTH-002 | AUTH | Phone-number entry |
| AUTH-003 | AUTH | OTP entry |
| AUTH-004 | AUTH | OTP resend / rate-limited state |
| AUTH-005 | AUTH | Account creation success |
| AUTH-006 | AUTH | Guest mode confirmation |
| AUTH-007 | AUTH | Recovery codes — display |
| AUTH-008 | AUTH | Recovery codes — acknowledge |

See [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md) for full screen descriptions.

---

### Page: Flows — Document Capture

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| CAP-001 | CAP | Camera viewfinder |
| CAP-002 | CAP | Edge detection active |
| CAP-003 | CAP | Manual crop |
| CAP-004 | CAP | Enhancement preview |
| CAP-005 | CAP | Multi-page review |
| CAP-006 | CAP | Save / name document |
| CAP-007 | CAP | Capture error (no camera permission) |
| CAP-008 | CAP | Capture error (low storage) |

---

### Page: Flows — Document Library

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| LIB-001 | LIB | Document library — home |
| LIB-002 | LIB | Folder view |
| LIB-003 | LIB | Document detail / page viewer |
| LIB-004 | LIB | Page reorder |
| LIB-005 | LIB | Document rename |
| LIB-006 | LIB | Move to folder |
| LIB-007 | LIB | Delete confirmation |
| LIB-008 | LIB | Search — results |
| LIB-009 | LIB | Search — empty state |
| LIB-010 | LIB | Library — empty state (no documents) |

---

### Page: Flows — Export and Share

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| EXP-001 | EXP | Export options sheet |
| EXP-002 | EXP | PDF export progress |
| EXP-003 | EXP | Share sheet trigger |
| EXP-004 | EXP | Export error |

---

### Page: Flows — Subscription and Paywall

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| SUB-001 | SUB | Paywall — feature gate |
| SUB-002 | SUB | Subscription options (monthly / annual) |
| SUB-003 | SUB | Purchase in progress |
| SUB-004 | SUB | Purchase success |
| SUB-005 | SUB | Purchase error / retry |
| SUB-006 | SUB | Subscription management |
| SUB-007 | SUB | Restore purchase |
| SUB-008 | SUB | Expiry — grace period notice |

---

### Page: Flows — Settings

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| SET-001 | SET | Settings — root |
| SET-002 | SET | Account settings |
| SET-003 | SET | Language settings |
| SET-004 | SET | Cloud backup toggle |
| SET-005 | SET | Privacy and data |
| SET-006 | SET | Delete account — confirmation |
| SET-007 | SET | About / app version |

---

### Page: Flows — Cloud Backup

| Frame | ID prefix | Description |
|-------|-----------|-------------|
| BCK-001 | BCK | Backup — setup / opt-in |
| BCK-002 | BCK | Backup — in progress |
| BCK-003 | BCK | Backup — complete |
| BCK-004 | BCK | Restore — in progress |
| BCK-005 | BCK | Restore — complete |
| BCK-006 | BCK | Backup — error / retry |
| BCK-007 | BCK | Backup — paused (subscription expired) |

---

### Page: Responsive Variants

Contains tablet and large-screen adaptations. See [RESPONSIVE_LAYOUTS.md](RESPONSIVE_LAYOUTS.md).

| Frame | Description |
|-------|-------------|
| Android Tablet — Library | LIB-001 at 600 dp width |
| Android Tablet — Capture | CAP-001 at 600 dp width |
| iPad — Library | LIB-001 at 768 pt width |
| iPad — Capture | CAP-001 at 768 pt width |

---

### Page: Accessibility Annotations

Contains accessibility annotations for TalkBack and VoiceOver. See
[ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md).

---

### Page: Localisation Variants

Contains key screens in Hindi and Kannada for string-length and rendering validation. See
[LOCALIZATION_REQUIREMENTS.md](LOCALIZATION_REQUIREMENTS.md).

---

## Naming conventions

| Convention | Format | Example |
|------------|--------|---------|
| Screen frame | `{PREFIX}-{NNN} Screen Name` | `AUTH-002 Phone Number Entry` |
| Component frame | `Component / Variant / State` | `Button / Primary / Default` |
| Token colour | `color/{category}/{role}` | `color/brand/primary` |
| Token spacing | `spacing/{n}` | `spacing/4` |

---

## Completion status

Figma file implementation is **evidence-pending**. See [FIGMA_AUDIT_REPORT.md](FIGMA_AUDIT_REPORT.md).

The Figma Completion Gate at [FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md) must be
passed before engineering implements any screen.
