# Accessibility Requirements

Toolly must meet a minimum WCAG 2.1 Level AA standard on all supported platforms. This
document records the specific accessibility requirements and maps them to design and
engineering responsibilities.

Design audit item DA-005 (accessibility compliance) is open; an accessibility audit report
is required before the first beta release. See also
[COMPONENT_STATE_MATRIX.md](COMPONENT_STATE_MATRIX.md) for per-component accessibility states.

---

## Standards and targets

| Standard | Target level | Scope |
|----------|-------------|-------|
| WCAG 2.1 | Level AA | All screens and components |
| Android accessibility | TalkBack | All interactive elements |
| iOS accessibility | VoiceOver | All interactive elements |
| Android text sizing | Dynamic type / font scaling | All text; up to 200 % scaling |
| iOS text sizing | Dynamic Type | All text; all content size categories |

---

## Colour contrast

| Context | Minimum ratio | Notes |
|---------|--------------|-------|
| Normal text (< 18 sp / 14 sp bold) | 4.5 : 1 | WCAG 1.4.3 |
| Large text (≥ 18 sp / 14 sp bold) | 3 : 1 | WCAG 1.4.3 |
| Non-text UI components and states | 3 : 1 | WCAG 1.4.11 |
| Disabled components | No requirement | Must still be visually distinguishable |

Colour contrast must be verified against all colour tokens in all states. See
[DESIGN_TOKENS.md](DESIGN_TOKENS.md) for the token set.

---

## Touch target sizes

| Platform | Minimum touch target | Notes |
|----------|---------------------|-------|
| Android | 48 × 48 dp | Google Material Design requirement |
| iOS | 44 × 44 pt | Apple HIG minimum; target 48 × 48 pt |

All buttons, icons, toggles, checkboxes, and interactive list items must meet the minimum
touch target. Visual size may be smaller than the touch target (padding extends the tap area).

---

## Screen reader support

### TalkBack (Android)

- Every interactive element must have a `contentDescription` that describes its purpose.
- Images and icons that convey information must have a non-empty content description.
- Decorative images must have an empty content description (`contentDescription = ""`).
- Screen reading order must follow the logical reading order (left-to-right, top-to-bottom).
- Focus must not be trapped inside components (e.g., modals must be dismissible with
  TalkBack).
- Custom actions must be exposed for swipe-based TalkBack gestures where applicable.

### VoiceOver (iOS)

- Every interactive element must have an accessibility label that describes its purpose.
- Accessibility traits must be set correctly: buttons, images, headers, links.
- Focus order must match the logical layout order.
- Modals and sheets must set correct `accessibilityViewIsModal = true`.
- Custom accessibility actions must be registered where native gestures are used.

---

## Screen reader: specific component requirements

| Component | TalkBack label | VoiceOver label | Notes |
|-----------|---------------|-----------------|-------|
| CAM-002 Capture Button | "Capture page" | "Capture page, button" | |
| BTN-006 FAB — Capture | "Scan new document" | "Scan new document, button" | |
| LBL-001 Premium Badge | "Premium feature" | "Premium feature" | Must not be labelled just as an image. |
| CAM-003 Flash Toggle | "Flash off", "Flash on", "Flash auto" | Same | Must announce current state on activation. |
| CTL-001 Toggle Switch | "Cloud backup, switch, off" | "Cloud backup, off" | Must announce state change. |
| INP-003 OTP Input | "Enter digit 1 of 6", … | Same | Each digit box labelled by position. |
| CAM-001 Viewfinder Overlay | "Document detected" / "Searching for document" | Same | Must be announced as status changes. |
| NAV-001 Bottom Navigation | Tab name + ", selected" or ", tab" | Same | Selected tab state must be announced. |

---

## Keyboard and switch access

- All interactive elements must be reachable via keyboard navigation (tab order).
- Logical tab order must match the visual order.
- Focus indicators (visible focus ring) must be present on all focused elements.
- Focus ring must meet 3 : 1 contrast ratio against adjacent colours (WCAG 2.4.7).
- Modal dialogs must trap focus within the dialog while open.
- Dialogs and sheets must be dismissible via keyboard (Escape key equivalent).

---

## Dynamic Type and font scaling

- All text elements must respond correctly to system text-size settings.
- Text must not be clipped or truncated at 200 % scaling without an expansion mechanism
  (e.g., scroll, wrap).
- Layouts must not break at any text scale category (iOS) or font scale (Android up to 2×).
- Hindi and Kannada strings are typically longer than English equivalents; layouts must be
  tested with maximum-length translated strings at large text sizes.
  See [LOCALIZATION_REQUIREMENTS.md](LOCALIZATION_REQUIREMENTS.md).
- Line heights must accommodate Devanagari and Kannada script ascenders and descenders.

---

## Colour-independent state communication

- State must not be communicated by colour alone (WCAG 1.4.1).
- Error states must use an icon or text label in addition to a colour change.
- Success states must use an icon or text label in addition to a colour change.
- The selected state of navigation tabs must use a text label or icon change in addition to
  colour.

---

## Motion and animation

- All animated transitions must respect the system reduced-motion setting
  (Android: `ANIMATOR_DURATION_SCALE = 0`; iOS: `UIAccessibility.isReduceMotionEnabled`).
- When reduced motion is on, animated transitions must be replaced by instant transitions or
  cross-fades.
- The edge-detection overlay animation (CAM-001) must respect reduced motion.

---

## Form error handling

- Error messages must be descriptive, not generic (e.g., "Enter a valid 10-digit Indian
  mobile number" not "Invalid input").
- Errors must be announced by screen readers immediately on validation failure.
- Error messages must be associated with the field that caused the error.

---

## Evidence required

| Item | Status |
|------|--------|
| Colour contrast verification for all tokens | Pending (DA-005) |
| TalkBack audit on representative Android devices | Pending (DA-005) |
| VoiceOver audit on representative iPhone | Pending (DA-005) |
| Dynamic Type / font scaling test at 200 % | Pending (DA-005) |
| Hindi and Kannada rendering on low-cost Android devices | Pending (DA-005) |
| Accessibility audit report | Pending — required before first beta release |
