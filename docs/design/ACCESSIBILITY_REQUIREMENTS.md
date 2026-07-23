# Accessibility Requirements

Defines the accessibility requirements for Toolly across Android (TalkBack) and iOS (VoiceOver).

Accessibility must be included in the design acceptance criteria. It must not be deferred to implementation.

---

## Target standard

WCAG 2.1 Level AA is the minimum target for all screens and components.

This must be met before the first beta release. Audit evidence is required (see DA-005 in DESIGN_AUDIT.md).

---

## Screen reader support

### TalkBack (Android)

- Every interactive element must have a meaningful content description or semantic label.
- Decorative images must be marked as not important for accessibility.
- Reading order must match the visual left-to-right, top-to-bottom order unless an alternate logical order is specified.
- Focus must not be trapped in components other than dialogs and bottom sheets (which must be escapable).
- Custom components (crop handles, OTP field cells, camera controls) must expose their role, state and value to the accessibility API.

### VoiceOver (iOS)

- Every interactive element must have an accessibility label, hint and trait.
- Traits must accurately represent the element role (button, image, header, text field, etc.).
- Custom actions must be provided for components where drag-and-drop is the only interaction (page reorder, crop handles).
- Groups must use accessibility containers where appropriate to reduce swipe count.

---

## Semantic labels

Requirements per component family:

| Component | Required label content |
|-----------|----------------------|
| ScanFab | "Scan document" |
| CaptureButton | "Capture page" |
| FlashToggle | "Flash: [current mode]" |
| BatchCounter | "[N] pages captured" |
| DocumentCard | "[Document name], [page count] pages, [date]" |
| PageThumbnail | "Page [N] of [total]" |
| StatusChip | "[Status label]" — must not read only a colour name |
| OcrStatusIndicator | "[OCR status description]" |
| CropHandle | "Crop handle [corner name]; drag to adjust" |
| OfflineIndicator | "You are offline. Some features are unavailable." |
| SyncIndicator | "Backup [status]" |
| PremiumLockBadge | "[Feature name], premium required" |
| SubscriptionStatusChip | "Subscription: [status]" |
| RecoveryPhraseDisplay | "Recovery phrase. Do not share these words." (phrase words must not be read aloud automatically — require user activation) |
| DestructiveButton | "[Action] — this cannot be undone" |

---

## Focus order

- Focus order must follow the natural reading order for the current locale.
- In dialogs and bottom sheets, focus must enter the dialog on open and return to the triggering element on close.
- On screen navigation (back button, dismiss), focus must return to the last sensible focus point.
- In list and grid views, focus must resume at the previously focused item after returning from a detail screen.

---

## Focus restoration

| Scenario | Required behaviour |
|----------|--------------------|
| Dialog closed | Focus returns to element that opened the dialog |
| Bottom sheet dismissed | Focus returns to triggering element |
| Navigation back | Focus restores to list item or button that triggered navigation |
| Snackbar dismissed | Focus returns to content; snackbar action is reachable before dismissal |

---

## Minimum touch targets

- All interactive elements must have a touch target of at least 48 × 48 dp.
- This applies to the invisible tap area, not just the visible component.
- CropHandle corners must have at least a 48 × 48 dp tap area even if the visual handle is smaller.
- Icon buttons in compact toolbars must pad to reach the minimum target size.

---

## Contrast

| Element | Minimum contrast ratio |
|---------|----------------------|
| Body text on background | 4.5:1 |
| Large text (≥ 18 pt regular or ≥ 14 pt bold) | 3:1 |
| UI components and icons (non-decorative) | 3:1 |
| Focus indicators | 3:1 against adjacent colours |
| Disabled components | May fall below AA; must still be distinguishable (≥ 3:1 against background) |

---

## Text scaling and Dynamic Type

- All UI must remain functional at system text size × 2.0 (200% scale).
- Text must not be clipped or overlapped at any accessibility text size.
- Minimum font sizes must not be specified in absolute dp when they conflict with system text scaling.
- Dynamic Type on iOS: all text styles must use system-defined styles (not fixed point sizes) where possible.

---

## Reduced motion

- All animations must respect the system "Reduce Motion" preference.
- When reduced motion is enabled, transitions must use opacity or cross-fade instead of translation or scale animations.
- Parallax effects and auto-playing animations must be disabled when reduced motion is active.
- Loading spinners may continue to rotate (informational animation); decorative animations must stop.

---

## Error announcements

- Inline field errors must be announced by the screen reader when they appear, not only when the user focuses the field.
- Form submission errors must be announced at the top of the form, not silently updated.
- Export, backup and restore failure states must trigger accessibility announcements.
- Snackbar error messages must be announced.

---

## Loading announcements

- Entering a loading state must trigger an accessibility announcement (e.g., "Loading documents" or "Exporting PDF").
- When loading completes, the result must be announced (e.g., "Documents loaded" or "Export complete").
- Progress bars must expose determinate progress values to the accessibility API.

---

## Non-colour status

- No component may rely exclusively on colour to communicate state (see COMPONENT_STATE_MATRIX.md).
- Every state difference must also use icon, text, shape or pattern.
- Applies to: error states, success states, warning states, sync states, subscription states, offline states.

---

## Accessible crop controls

The crop and enhancement screen presents a unique accessibility challenge because it relies on drag interactions.

Requirements:

- Each CropHandle corner must have an accessible label ("Top-left corner", etc.).
- An accessible alternative to dragging must be provided: up/down/left/right adjustment via accessibility actions or visible increment/decrement controls.
- Rotation must be adjustable via a slider with accessible value announcement or via increment/decrement controls.
- The current crop bounds must be readable by the screen reader as numeric values (e.g., "Crop: 20 pixels from top, 40 pixels from left").
- The magnified handle inset must be accessible when the handle is active.

---

## Accessible page reordering

The document editor page reorder interaction relies on drag-and-drop.

Requirements:

- Each page thumbnail must expose a custom accessibility action: "Move up" and "Move down".
- The current position must be announced: "Page 2 of 5".
- After reorder, focus must remain on the moved page and the new position must be announced.
- Drag-and-drop may be used as the primary visual interaction; keyboard/accessibility alternative must also be available.

---

## Accessible destructive-action confirmation

- Destructive actions (delete page, delete document, account deletion, remove device) must present a two-step confirmation.
- The destructive button must be labeled with the specific action ("Delete page", not just "Delete").
- The confirmation dialog must describe the consequence in plain language.
- The destructive button must not be the default focused element in the dialog.
- Screen readers must announce the consequence text before the buttons.

---

## Screen-reader-friendly subscription comparison

- Plan comparison tables must be navigable by screen reader as a logical list.
- Feature names and free/premium status must be read as a unit: "[Feature name]: [free or premium]".
- Premium pricing must include currency symbol and billing period: "₹[price] per month" or "₹[price] per year".
- No interactive element inside the comparison table may rely on position alone to communicate which plan it applies to.

---

## Accessibility inclusion in design acceptance criteria

The following must appear in the acceptance criteria for every screen:

- [ ] All interactive elements have semantic labels.
- [ ] Focus order is logical.
- [ ] Focus restoration is correct.
- [ ] Touch targets are ≥ 48 × 48 dp.
- [ ] Contrast meets WCAG 2.1 AA.
- [ ] State is not communicated by colour alone.
- [ ] Error and loading states trigger accessibility announcements.
- [ ] Text scales to 200% without clipping or overlap.
- [ ] Custom drag interactions have an accessible alternative.
- [ ] Destructive confirmations are clearly labeled.

---

## Audit status

Accessibility design requirements: **Specified** (this document).

Accessibility implementation audit: **Not started** — pending Phase 2 and Phase 3 implementation.

Accessibility compliance evidence: **Not available** — required before first beta release (DA-005).
