# Localization Requirements

Defines the localization requirements for Toolly's India-first launch and future international expansion.

---

## Launch locales

| Locale | Language | Region | Status |
|--------|----------|--------|--------|
| en-IN | English | India | Required for launch |
| hi-IN | Hindi | India | Required for launch |
| kn-IN | Kannada | India | Required for launch |

Future locale expansion is designed for but not implemented in V1.

---

## Text expansion allowance

When translating from English, other languages may require significantly more space.

Designers must not create fixed-width containers that cannot accommodate expanded text.

| Language | Expected expansion over English |
|----------|---------------------------------|
| Hindi (hi-IN) | Up to 40% longer in some strings |
| Kannada (kn-IN) | Up to 50% longer in some strings due to script width |

All layouts must be verified with the longest expected string in each launch language.

---

## Multiline labels

- Buttons and chips must support multiline labels at large text sizes.
- Fixed-height containers that clip translated text are not permitted.
- Short-string layouts (e.g., "OK", "Cancel") must be verified to still render correctly in Hindi and Kannada at the longest translated equivalent.
- Navigation tab labels must wrap or abbreviate gracefully; do not truncate without an accessible full-label alternative.

---

## Pluralization

- All count-based strings must use locale-aware pluralization.
- English uses singular/plural; Hindi and Kannada have additional plural categories.
- Do not hardcode "s" suffix for plurals: use the platform pluralization API.
- Examples requiring pluralization: "1 page / 2 pages", "1 document / 5 documents", "1 device / 3 devices".

---

## Numbers

| Format | en-IN | hi-IN | kn-IN |
|--------|-------|-------|-------|
| Grouping | 1,00,000 (Indian numbering) | Same | Same |
| Decimal separator | . | . | . |

Toolly must use locale-aware number formatting for all displayed numbers.

---

## Currency

| Format | en-IN | hi-IN | kn-IN |
|--------|-------|-------|-------|
| Symbol | ₹ | ₹ | ₹ |
| Position | Before amount | Before amount | Before amount |
| Example | ₹149/month | ₹149/माह | ₹149/ತಿಂಗಳಿಗೆ |

Subscription prices must always display the correct currency symbol and billing period in the user's locale.

---

## Date and time

- Use locale-aware date and time formatting via platform APIs.
- Display dates in the user's locale convention (en-IN typically uses DD/MM/YYYY).
- Relative timestamps ("2 hours ago", "Yesterday") must be locale-aware.
- Time zones must not be assumed; use the device's configured time zone.

---

## File size

- Display file sizes in locale-appropriate format.
- Use standard SI or IEC prefixes consistently (KB, MB, GB).
- All size displays must use platform locale-aware formatting.

---

## Phone numbers

- Phone number formatting must follow the Indian convention for en-IN, hi-IN and kn-IN.
- The India country code (+91) must be pre-populated in the phone entry field.
- Phone numbers must not be stored in logs or analytics (see SECURITY_BASELINE.md).
- Phone number display must mask all but the last four digits where shown in the UI.

---

## OTP content

- OTP screens must clearly label the field: "Enter the code sent to [masked number]".
- OTP labels and hints must be translated for all launch locales.
- Countdown timers must use locale-aware number formatting.
- "Resend code" must be translated and respect accessibility requirements.

---

## Font coverage

The following scripts must render correctly on all target devices and OS versions:

| Script | Required for |
|--------|-------------|
| Latin | English (en-IN) |
| Devanagari | Hindi (hi-IN) |
| Kannada | Kannada (kn-IN) |

Requirements:

- The system font must be used for all body, heading and UI text.
- Do not use custom fonts for body text in V1; custom display fonts (brand use only) must fully cover all launch scripts.
- Verify Devanagari and Kannada rendering on Android API 26 + and iOS 15 + representative devices.
- Confirm that digit glyphs in Devanagari and Kannada render correctly in numeric fields (OTP, page counter, file size).
- Do not place user-facing text inside raster images or vector assets; use text layers.

---

## Truncation rules

- Truncate with an ellipsis (…) only when no alternative layout is possible.
- Document names in cards may truncate at one or two lines; full name accessible via tooltip or long-press.
- Navigation labels must not truncate; shorten via translation if needed.
- Error messages must never truncate; use scrollable containers if necessary.
- Subscription plan names must not truncate in the comparison screen.

---

## Bidirectional layout readiness

V1 does not include a bidirectional (RTL) locale. However, the layout system must be written using start/end semantics (not left/right) to allow future RTL locale support without layout rewrites.

Requirements:

- Use `start` and `end` padding and margin instead of `left` and `right`.
- Use `textDirection` or layout-mirroring APIs for directional icons (back arrow, forward arrow).
- Icon assets that have inherent directionality must be mirrored appropriately in RTL.
- This applies to both Compose layouts (Android) and SwiftUI layouts (iOS).

---

## Screenshot review requirements

Before each launch locale is approved, screenshots must be reviewed for:

- [ ] All strings are translated (no English placeholder visible).
- [ ] No text is clipped, truncated or overflowing any container.
- [ ] Numbers, dates, currency and phone numbers are formatted correctly for the locale.
- [ ] Font rendering is correct for the script.
- [ ] No raster assets contain user-facing text.
- [ ] Pluralization is correct for each locale.
- [ ] Direction of directional icons is correct.

**Required screenshots per locale:**

One screenshot for each screen in SCREEN_INVENTORY.md on at least one representative Android phone, one Android tablet, one iPhone and one iPad.

---

## Localization in the Figma design

- All Figma frames must include representative localized text for at least en-IN and hi-IN.
- Kannada frames must be present for screens that include text-heavy content.
- Long-string variants must be shown for the component state matrix to verify expansion behaviour.
- Figma localization review is part of gate G6 (see FIGMA_COMPLETION_GATE.md).

---

## Audit status

Localization specification: **Complete** (this document).

Figma localization review (G6): **Evidence pending** — Hindi and Kannada frames not verified.

Implementation localization audit: **Not started** — pending Phase 2 implementation.
