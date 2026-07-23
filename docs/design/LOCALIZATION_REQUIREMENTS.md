# Localisation Requirements

This document defines the localisation requirements for Toolly V1. The application must launch
with support for English, Hindi and Kannada. Requirements are derived from
[INDIA_LAUNCH_SCOPE.md](../product/INDIA_LAUNCH_SCOPE.md) and
[DECISION_REGISTER.md](../product/DECISION_REGISTER.md) (D-002).

---

## Launch languages

| Language | Script | Locale code | RTL | Status |
|---------|--------|-------------|-----|--------|
| English | Latin | `en-IN` | No | Launch requirement |
| Hindi | Devanagari | `hi-IN` | No | Launch requirement |
| Kannada | Kannada script | `kn-IN` | No | Launch requirement |

RTL layout support is a post-V1 goal and is not required at launch.

---

## String externalisation

- All user-visible strings must be externalised to platform-standard resource files.
  - Android: `res/values/strings.xml`, `res/values-hi/strings.xml`, `res/values-kn/strings.xml`
  - iOS: `Localizable.strings`, `Localizable.stringsdict` for plurals
- No hardcoded UI strings are permitted in application code.
- String keys must be descriptive (e.g., `otp_entry_resend_button` not `str_42`).

---

## Translation requirements

- All strings must be translated by a native speaker for each language.
- Machine translation without native-speaker review is not acceptable.
- Translations must be reviewed before each release.

---

## String length variance

Hindi and Kannada strings are often longer than their English equivalents. Layouts must
accommodate string length variance without truncation or overflow.

| Language | Expected overhead vs English |
|---------|------------------------------|
| Hindi | +20 % to +50 % (sentence-level) |
| Kannada | +30 % to +60 % (sentence-level) |

**Design requirement:** All Figma layouts must be tested with the longest expected translated
string. See [FIGMA_INFORMATION_ARCHITECTURE.md](FIGMA_INFORMATION_ARCHITECTURE.md) — the
Localisation Variants Figma page must contain key screens with Hindi and Kannada strings.

---

## Font requirements

| Language | Recommended font | Android | iOS |
|---------|-----------------|---------|-----|
| English | System (Roboto / SF Pro) | System | System |
| Hindi | Noto Sans Devanagari | Include as asset or use system Devanagari font | Include asset |
| Kannada | Noto Sans Kannada | Include as asset or use system Kannada font | Include asset |

- Font selection must support correct rendering on low-cost Android devices with older
  Android versions (see INDIA_LAUNCH_SCOPE.md).
- Line height must accommodate ascenders and descenders in Devanagari and Kannada scripts.
  Devanagari text requires additional line height compared to Latin text.
- Dynamic Type / font scaling must work correctly for Hindi and Kannada strings.

---

## Number and currency formatting

- All number formatting must use locale-aware APIs; no hardcoded format strings.
- Indian number system (lakhs and crores) must be applied for the `en-IN` locale.
- INR amounts must use the `₹` symbol; formatted per INDIA_LAUNCH_SCOPE.md:
  - Below ₹1,000: no separator (`₹499`).
  - ₹1,000 and above: Indian number system separators (`₹1,999`).
- Prices must be store-driven; no hardcoded INR values in the app binary.

---

## Date and time formatting

- All date formatting must use locale APIs with the device locale.
- Default format for India: `DD/MM/YYYY`.
- Time format: 12-hour with AM/PM for `en-IN`; follows device preference.
- UTC timestamps must be used for storage; local formatting is a presentation-layer concern.

---

## Phone-number formatting

- Input accepts 10-digit numbers with or without +91 prefix.
- Display format: `+91 XXXXX XXXXX` (5+5 grouping).
- Input field label and placeholder must be translated for all three languages.

---

## Plural strings

- Android: use `<plurals>` with `quantity` attributes (`one`, `other`; add `few`, `many`
  as required for Hindi and Kannada).
- iOS: use `stringsdict` for plural rules.
- Hindi and Kannada plural rules differ from English; a translator must review plural forms.

Examples:

- "1 page" / "2 pages" / "10 pages"
- "1 document" / "5 documents"

---

## Accessibility and localisation interaction

- Accessibility labels (TalkBack / VoiceOver) must also be localised.
- Screen reader announcements must be in the user's selected language.
- See [ACCESSIBILITY_REQUIREMENTS.md](ACCESSIBILITY_REQUIREMENTS.md).

---

## Localisation testing checklist

| Test | Status |
|------|--------|
| All UI strings externalised (no hardcoded strings) | Pending |
| Hindi translation reviewed by native speaker | Pending |
| Kannada translation reviewed by native speaker | Pending |
| Hindi font rendering on API 24+ (Android) | Pending |
| Kannada font rendering on API 24+ (Android) | Pending |
| Hindi text at 200 % Dynamic Type scaling (iOS) | Pending |
| Kannada text at 200 % Dynamic Type scaling (iOS) | Pending |
| Layouts tested with longest Hindi string per screen | Pending |
| Layouts tested with longest Kannada string per screen | Pending |
| INR currency formatting correct in en-IN locale | Pending |
| Date format DD/MM/YYYY in en-IN locale | Pending |
| Phone-number display format +91 XXXXX XXXXX | Pending |
| Plural strings correct in Hindi | Pending |
| Plural strings correct in Kannada | Pending |
| Accessibility labels localised in all three languages | Pending |

---

## Post-V1 localisation

RTL layout support and additional languages are post-V1. The app must be implemented in a way
that does not block future RTL or additional language support:

- All layouts must use start/end rather than left/right padding.
- Text alignment must use start/end not left/right.
- Icons that convey directionality (arrows, back button) must use mirrored variants.
