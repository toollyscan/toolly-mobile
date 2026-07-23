# Figma Audit Report

This document records the current audit state of the Toolly Figma design file. It is updated
as design work progresses.

**Important:** This report is evidence-pending. The Figma file has not yet been created or
verified. All design work is pending. No Figma node IDs are recorded; these must be extracted
from the live Figma file when it exists.

---

## Audit scope

This audit covers:

- Screen completeness against [SCREEN_INVENTORY.md](SCREEN_INVENTORY.md)
- Component completeness against [COMPONENT_INVENTORY.md](COMPONENT_INVENTORY.md)
- Design system completeness against [DESIGN_TOKENS.md](DESIGN_TOKENS.md)
- Information architecture completeness against
  [FIGMA_INFORMATION_ARCHITECTURE.md](FIGMA_INFORMATION_ARCHITECTURE.md)
- Figma Completion Gate status per [FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md)

---

## Audit status

| Audit item | Status | Last reviewed | Notes |
|-----------|--------|--------------|-------|
| Figma file exists | Not started | — | File not yet created. |
| Figma file URL recorded | Not started | — | |
| Design system page complete | Not started | — | |
| Components page complete | Not started | — | |
| Authentication screens (8) | Not started | — | |
| Document capture screens (8) | Not started | — | |
| Document library screens (10) | Not started | — | |
| Export and sharing screens (4) | Not started | — | |
| Subscription and paywall screens (8) | Not started | — | |
| Settings screens (7) | Not started | — | |
| Cloud backup screens (7) | Not started | — | |
| Responsive variants — Android tablet | Not started | — | DA-007 |
| Responsive variants — iPad | Not started | — | DA-008 |
| Localisation variants — Hindi | Not started | — | |
| Localisation variants — Kannada | Not started | — | |
| Accessibility annotations | Not started | — | |
| Prototype connections for all 14 flows | Not started | — | |
| Colour contrast verified | Not started | — | |
| Figma Completion Gate reviewed | Not started | — | |

---

## Per-screen status

| Screen ID | Screen name | Figma frame | Gate passed | Handoff issued | Implemented |
|-----------|-------------|-------------|-------------|----------------|-------------|
| AUTH-001 | Welcome / Splash | — | No | No | No |
| AUTH-002 | Phone Number Entry | — | No | No | No |
| AUTH-003 | OTP Entry | — | No | No | No |
| AUTH-004 | OTP Rate Limited | — | No | No | No |
| AUTH-005 | Account Created | — | No | No | No |
| AUTH-006 | Guest Mode Confirmation | — | No | No | No |
| AUTH-007 | Recovery Codes — Display | — | No | No | No |
| AUTH-008 | Recovery Codes — Acknowledge | — | No | No | No |
| CAP-001 | Camera Viewfinder | — | No | No | No |
| CAP-002 | Edge Detection Active | — | No | No | No |
| CAP-003 | Manual Crop | — | No | No | No |
| CAP-004 | Enhancement Preview | — | No | No | No |
| CAP-005 | Multi-page Review | — | No | No | No |
| CAP-006 | Save Document | — | No | No | No |
| CAP-007 | Camera Permission Error | — | No | No | No |
| CAP-008 | Low Storage Warning | — | No | No | No |
| LIB-001 | Document Library | — | No | No | No |
| LIB-002 | Folder View | — | No | No | No |
| LIB-003 | Document Detail | — | No | No | No |
| LIB-004 | Page Reorder | — | No | No | No |
| LIB-005 | Document Rename | — | No | No | No |
| LIB-006 | Move to Folder | — | No | No | No |
| LIB-007 | Delete Confirmation | — | No | No | No |
| LIB-008 | Search Results | — | No | No | No |
| LIB-009 | Search Empty State | — | No | No | No |
| LIB-010 | Library Empty State | — | No | No | No |
| EXP-001 | Export Options | — | No | No | No |
| EXP-002 | Export Progress | — | No | No | No |
| EXP-003 | Share Sheet Trigger | — | No | No | No |
| EXP-004 | Export Error | — | No | No | No |
| SUB-001 | Paywall — Feature Gate | — | No | No | No |
| SUB-002 | Subscription Options | — | No | No | No |
| SUB-003 | Purchase In Progress | — | No | No | No |
| SUB-004 | Purchase Success | — | No | No | No |
| SUB-005 | Purchase Error | — | No | No | No |
| SUB-006 | Subscription Management | — | No | No | No |
| SUB-007 | Restore Purchase | — | No | No | No |
| SUB-008 | Expiry Grace Notice | — | No | No | No |
| SET-001 | Settings Root | — | No | No | No |
| SET-002 | Account Settings | — | No | No | No |
| SET-003 | Language Settings | — | No | No | No |
| SET-004 | Cloud Backup Settings | — | No | No | No |
| SET-005 | Privacy and Data | — | No | No | No |
| SET-006 | Delete Account | — | No | No | No |
| SET-007 | About | — | No | No | No |
| BCK-001 | Backup Setup / Opt-in | — | No | No | No |
| BCK-002 | Backup In Progress | — | No | No | No |
| BCK-003 | Backup Complete | — | No | No | No |
| BCK-004 | Restore In Progress | — | No | No | No |
| BCK-005 | Restore Complete | — | No | No | No |
| BCK-006 | Backup Error | — | No | No | No |
| BCK-007 | Backup Paused — Expired | — | No | No | No |

---

## Audit summary

| Category | Total screens | Figma frame exists | Gate passed | Implemented |
|----------|--------------|-------------------|-------------|-------------|
| Authentication | 8 | 0 | 0 | 0 |
| Document capture | 8 | 0 | 0 | 0 |
| Document library | 10 | 0 | 0 | 0 |
| Export and sharing | 4 | 0 | 0 | 0 |
| Subscription and paywall | 8 | 0 | 0 | 0 |
| Settings | 7 | 0 | 0 | 0 |
| Cloud backup | 7 | 0 | 0 | 0 |
| **Total** | **52** | **0** | **0** | **0** |

---

## Audit history

| Date | Auditor | Notes |
|------|---------|-------|
| 2026-07-23 | copilot | Initial audit record created. Design work not yet started. |

---

## How to update this report

When Figma work progresses:

1. Update the "Figma frame" column with the Figma frame link (not the node ID).
2. Update the "Gate passed" column to "Yes" when the
   [FIGMA_COMPLETION_GATE.md](FIGMA_COMPLETION_GATE.md) is passed for that screen.
3. Update the audit summary table totals.
4. Add a row to the audit history with the date, auditor and a brief note.
5. Commit the update with a `docs:` conventional commit message referencing this file.
