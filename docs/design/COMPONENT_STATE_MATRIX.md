# Component State Matrix

Documents the required interactive states for every Toolly UI component.

State must never be communicated by colour alone. Each state requires a distinct visual treatment (shape, icon, text, pattern or combination).

---

## State definitions

| State | Description |
|-------|-------------|
| Default | Resting state; no user interaction |
| Pressed | Pointer or touch is actively pressed on the component |
| Focused | Component has keyboard or accessibility focus |
| Hovered | Pointer is positioned over the component (tablet / keyboard navigation) |
| Selected | Component is in a chosen or active state |
| Disabled | Component is not interactive in current context |
| Loading | Asynchronous operation is in progress |
| Success | Operation completed successfully |
| Warning | Operation completed with a non-blocking issue |
| Error | Operation failed or input is invalid |

---

## Navigation components

### BottomNavBar tab item

| State | Visual treatment |
|-------|-----------------|
| Default | Icon + label, standard contrast |
| Pressed | Ripple / press highlight |
| Focused | Visible focus ring around tab item |
| Selected | Active colour, filled icon (not colour-only: also uses filled vs. outline icon) |
| Disabled | Not applicable (navigation is always enabled) |

### NavigationRail item

| State | Visual treatment |
|-------|-----------------|
| Default | Icon + optional label |
| Pressed | Ripple highlight |
| Focused | Visible focus indicator |
| Selected | Active indicator pill; filled icon variant |

### TopAppBar back / action button

| State | Visual treatment |
|-------|-----------------|
| Default | Standard icon |
| Pressed | Ripple |
| Focused | Focus ring |
| Disabled | Reduced opacity + changed icon tint |

---

## Buttons

### PrimaryButton

| State | Visual treatment |
|-------|-----------------|
| Default | Filled background, white label |
| Pressed | Darker fill (overlay) + ripple |
| Focused | Focus ring outside button boundary |
| Hovered | Elevated shadow + slightly lightened fill |
| Disabled | Reduced opacity; not colour-only — also uses disabled label text |
| Loading | Spinner replaces label; button not tappable |
| Destructive | Red fill; warning icon; confirmation required |

### SecondaryButton

| State | Visual treatment |
|-------|-----------------|
| Default | Outlined border, coloured label |
| Pressed | Light background fill + ripple |
| Focused | Focus ring |
| Hovered | Light background fill |
| Disabled | Reduced opacity border and label |

### TextButton

| State | Visual treatment |
|-------|-----------------|
| Default | Text only |
| Pressed | Ripple |
| Focused | Underline or focus ring |
| Disabled | Reduced opacity |

### DestructiveButton

| State | Visual treatment |
|-------|-----------------|
| Default | Destructive colour fill |
| Pressed | Darker fill |
| Focused | Focus ring |
| Disabled | Reduced opacity |
| Loading | Spinner; blocked |

---

## Icon buttons

### IconButton

| State | Visual treatment |
|-------|-----------------|
| Default | Standard icon |
| Pressed | Circular ripple |
| Focused | Visible circular focus ring |
| Hovered | Subtle background fill |
| Disabled | Reduced opacity + tint change (not colour-only — also reduced opacity) |

### ToggleIconButton

| State | Visual treatment |
|-------|-----------------|
| Off | Outline icon |
| On | Filled icon + active background tint |
| Pressed | Ripple |
| Focused | Focus ring |
| Disabled | Reduced opacity |

---

## ScanFab

| State | Visual treatment |
|-------|-----------------|
| Default | Filled background + scan icon |
| Pressed | Ripple + scale-down |
| Focused | Focus ring |
| Hidden | Not rendered during immersive flows |

---

## Inputs

### TextField

| State | Visual treatment |
|-------|-----------------|
| Default | Outlined container, hint text |
| Focused | Highlighted border (colour + stroke weight) |
| Filled | Label floated; value text visible |
| Error | Error border + error icon + helper error text (not colour-only) |
| Disabled | Filled background; reduced contrast; not interactive |
| Read-only | Standard border; no cursor |

### PasswordField

Inherits TextField states plus:

| State | Visual treatment |
|-------|-----------------|
| Visible | Eye icon toggled; plain text |
| Hidden | Eye-off icon; dots |

### OtpField

| State | Visual treatment |
|-------|-----------------|
| Empty | Empty cell with outline |
| Entering | Filled cell per digit; cursor in active cell |
| Complete | All cells filled |
| Error | Error border + error label (not colour-only — also uses error icon) |
| Expired | Error state + expiry label |

---

## Search

### SearchBar / SearchField

| State | Visual treatment |
|-------|-----------------|
| Inactive | Placeholder text, magnifier icon |
| Active | Elevated, cursor visible, back arrow |
| Filled | Query text; clear button |
| Results | Results list shown below |
| No results | Empty-search state |

---

## Filter and sort

### FilterChip

| State | Visual treatment |
|-------|-----------------|
| Default | Outlined, label |
| Selected | Filled background + checkmark (not colour-only) |
| Pressed | Ripple |
| Focused | Focus ring |
| Disabled | Reduced opacity |

---

## Cards

### DocumentCard

| State | Visual treatment |
|-------|-----------------|
| Default | Thumbnail + metadata |
| Loading | Skeleton placeholder |
| Selected | Checkbox or checkmark overlay; border highlight |
| Pressed | Ripple |
| Focused | Focus ring |
| Error | Error icon on thumbnail; error text (not colour-only) |
| Offline | Offline chip on card |

### PageThumbnail

| State | Visual treatment |
|-------|-----------------|
| Default | Thumbnail image |
| Selected | Highlighted border + checkmark |
| Drag active | Elevated shadow; ghost image |
| Drop target | Insert indicator |
| Error | Error icon overlay (not colour-only) |

---

## Status chips

### StatusChip

| State | Visual treatment |
|-------|-----------------|
| Success | Icon + label (not colour-only) |
| Warning | Warning icon + label |
| Error | Error icon + label |
| Info | Info icon + label |
| Syncing | Animated spinner + label |
| Offline | Offline icon + label |
| Premium | Lock or star icon + label |

---

## Banners

### InfoBanner / ActionBanner

| State | Visual treatment |
|-------|-----------------|
| Info | Info icon + text |
| Warning | Warning icon + text (not colour-only) |
| Error | Error icon + text |
| Offline | Offline icon + text |
| Dismissed | Not rendered |

---

## Dialogs

### AlertDialog

| State | Visual treatment |
|-------|-----------------|
| Default | Standard confirmation |
| Destructive | Destructive button variant; warning text |
| Loading | Spinner; buttons disabled |

### InputDialog

| State | Visual treatment |
|-------|-----------------|
| Default | Empty input |
| Error | Error below input; confirm disabled |

---

## Progress indicators

### LinearProgressBar

| State | Visual treatment |
|-------|-----------------|
| Indeterminate | Animated sweep |
| Determinate | Fill width matches percentage + label |
| Complete | Full fill + success indicator |
| Error | Error icon + error colour tint + text (not colour-only) |

### CircularProgressIndicator

| State | Visual treatment |
|-------|-----------------|
| Indeterminate | Rotating arc |
| Determinate | Arc fills to percentage |

---

## Capture controls

### CaptureButton

| State | Visual treatment |
|-------|-----------------|
| Default | Circular shutter |
| Processing | Spinner replaces shutter |

### FlashToggle

| State | Visual treatment |
|-------|-----------------|
| Off | Crossed lightning icon |
| Auto | Lightning + A label |
| On | Lightning icon, lit state |
| Torch | Torch icon, lit state |

### CaptureWarningOverlay

| State | Visual treatment |
|-------|-----------------|
| Blur | Warning icon + text "Blurry — hold steady" |
| Glare | Warning icon + text "Glare detected — adjust angle" |
| Low light | Warning icon + text "Low light — add light source" |
| Edge failure | Warning icon + text "Edge not found — position document" |

---

## Crop handles

### CropHandle

| State | Visual treatment |
|-------|-----------------|
| Default | Square or circle handle |
| Active | Enlarged handle + magnified inset shown |

---

## Enhancement controls

### EnhancementModeSelector

| State | Visual treatment |
|-------|-----------------|
| Default | Outline icons |
| Selected | Filled/active icon + label underline (not colour-only) |

---

## Editor controls

### OcrStatusIndicator

| State | Visual treatment |
|-------|-----------------|
| Pending | Spinner + "Recognising text" label |
| Success | Check icon + "Text recognised" label |
| Partial failure | Warning icon + "Recognition incomplete" label (not colour-only) |
| Unavailable | Lock icon + "Premium required" label |

### AutosaveIndicator

| State | Visual treatment |
|-------|-----------------|
| Saved | Check icon + "Saved" label |
| Saving | Spinner + "Saving…" label |
| Unsaved changes | Dot indicator + "Unsaved changes" label |
| Error | Error icon + "Save failed — tap to retry" (not colour-only) |

---

## Subscription indicators

### SubscriptionStatusChip

| State | Visual treatment |
|-------|-----------------|
| Free | Standard chip |
| Premium active | Star icon + "Premium" label |
| Trial | Clock icon + "Trial" + days remaining label |
| Grace period | Warning icon + "Payment issue" label |
| Expired | Alert icon + "Expired" label (not colour-only) |
| Cancelled | Info icon + "Cancelled — active until [date]" |
| Revoked | Error icon + "Revoked" label |

---

## General principles for state design

1. Every interactive component must have a visible focus indicator that meets WCAG 2.1 AA contrast requirements.
2. State must never be communicated by colour alone: add icon, shape, text or pattern.
3. Disabled components must have a minimum contrast ratio of 3:1 against the background (WCAG 2.1 AA non-text).
4. Touch targets must be at least 48 × 48 dp regardless of the visible component size.
5. Loading states must be announced to screen readers (TalkBack / VoiceOver).
6. Error states must be announced to screen readers with a descriptive message.
