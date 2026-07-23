# India Launch Scope

Toolly India-first launch specification.

This document does not constitute legal advice. All items marked **[Legal gate]** must be
reviewed by qualified legal counsel before launch. Do not make legal claims based on this
document.

---

## India-first rationale

India is Toolly's selected first market. Market size, willingness to pay and workflow differentiation remain evidence requirements rather than approved claims. English, Hindi and Kannada support the selected India-first audience and Karnataka launch strategy. See DECISION_REGISTER D-001 and D-002.

---

## User segments

| Segment | Description |
|---------|-------------|
| Students | Scanning mark sheets, certificates, ID cards and assignment submissions. |
| Small-business owners | Invoices, GST documents, receipts, contracts. |
| Government document users | Aadhaar, PAN, ration card, driving licence, passport. |
| Professionals | Legal documents, medical records, property papers. |
| Home users | Family documents, financial papers, insurance. |

---

## Launch languages

| Language | Script | Status |
|---------|--------|--------|
| English | Latin | Launch requirement |
| Hindi | Devanagari | Launch requirement |
| Kannada | Kannada script | Launch requirement |

### Requirements

- All UI strings must be translated and reviewed by a native speaker for each language.
- Hindi and Kannada fonts must be rendered correctly on all representative Android devices
  (see DA-005).
- Font selection must support correct rendering on low-cost devices with older Android versions.
- Dynamic type (text size accessibility) must work correctly with Hindi and Kannada strings.
- String length variance between languages must be accounted for in UI layouts.

---

## Phone-number authentication expectations

- OTP via SMS to Indian mobile numbers.
- Indian mobile number format: +91 followed by a 10-digit number.
- Number must be validated against the +91 prefix and 10-digit format before OTP dispatch.
- Toolly-owned application databases must not duplicate plaintext phone numbers without an approved purpose. Firebase Authentication processes provider identity data according to its configured service contract and privacy terms.
- OTP abuse controls (rate limiting, lockout) must be in place before launch.
  See COST_CONTROLS.md.
- Firebase Authentication is the OTP provider; it must remain behind a
  provider-neutral interface.
- Before phone authentication, the notice and consent flow must explain that the phone number is sent to and stored by Google for spam and abuse prevention, subject to the approved Firebase configuration and privacy notice.

---

## Indian phone-number formatting

- Input field must accept 10-digit numbers with or without the +91 prefix.
- Display format in UI: `+91 XXXXX XXXXX` (5+5 grouping).
- Phone numbers must never be written to application logs or analytics; any Toolly-owned persistence requires an approved purpose, retention period and security review.
- International number support (non-+91) is a post-V1 requirement.

---

## Indian currency display

- Subscription prices must be displayed in INR (₹).
- Amounts below ₹1,000 displayed without separator: `₹499`.
- Amounts ₹1,000 and above displayed with Indian number system separators: `₹1,999`.
- Currency formatting must be implemented in the presentation layer; domain layer must not
  contain hardcoded INR amounts.
- Final prices are not defined here; see MONETIZATION.md for the pricing framework.
- Prices must be confirmed with legal counsel for GST display requirements. **[Legal gate]**

---

## Date and time formatting

- Default locale date format for India: `DD/MM/YYYY`.
- Time format: 12-hour with AM/PM for consumer contexts; 24-hour as alternative.
- All date and time values must use the device locale for formatting.
- UTC timestamps must be used for storage and sync; local formatting is a presentation concern.

---

## Subscription store requirements

| Platform | Store | Notes |
|---------|-------|-------|
| Android | Google Play Store | In-app subscriptions via Google Play Billing. |
| iOS | Apple App Store | In-app subscriptions via StoreKit. |

- Billing SDK must not enter domain entitlement models.
- Google Play and Apple transaction types must be wrapped in Toolly-owned entitlement contracts.
- Subscription products must be created in Google Play Console and App Store Connect before launch.
- Store listing must be reviewed and approved before launch.
- Pricing tiers must be set per MONETIZATION.md pricing framework.
- Regional pricing (India) must be confirmed in each store console.
- Tax settings (GST) must be configured in each store console. **[Legal gate]**

---

## Customer-support expectations

- Support channel: in-app link to email support at minimum.
- Support language: English at launch; Hindi and Kannada support is aspirational.
- Response SLA: defined before launch.
- Grievance contact requirement: see below.

---

## Privacy notice requirements

- A privacy notice must be published before launch. **[Legal gate]**
- The privacy notice must describe:
  - What data is collected.
  - How data is used.
  - Which providers process each data category, the applicable processing locations and any reviewed cross-border transfer.
  - How users can access, correct and delete their data.
  - Grievance officer contact details.
- Privacy notice must be accessible from the app and from the store listing.
- Privacy notice must be reviewed by qualified legal counsel. **[Legal gate]**
- Privacy notice and consent flows must be reviewed against the DPDP Act 2023, DPDP Rules 2025 and provisions effective on the launch date. **[Legal gate]**

---

## Grievance contact requirement

The exact grievance contact, role, response process and statutory terminology must be confirmed against provisions effective at launch.

- An accountable grievance contact and process must be operational before launch. **[Legal gate]**
- Grievance officer contact details (name and contact address) must appear in:
  - The in-app privacy notice or settings.
  - The store listing privacy policy link.
- Grievance mechanism must allow users to raise data-related complaints.
- Response to grievances must be provided within the period required by law. **[Legal gate]**

---

## Data deletion and export workflows

- Users must be able to request deletion of their account and all associated data.
- Deletion timing must follow the approved purpose/retention schedule and legal obligations effective at launch. **[Legal gate]**
- Users must be able to export their data in a portable format.
- Local vault data: user deletes via app; Toolly does not remote-wipe local data.
- Cloud backup data: deleted from cloud on account deletion after the retention period.
- Deletion workflow must be accessible from within the app without contacting support.
- Deletion confirmation must be clear and non-deceptive.

---

## Low-cost Android device support

- Minimum Android version: API 26. The supported-device strategy must still be validated against representative India device data.
- Target minimum RAM: **[H]** 1 GB. India's low-cost Android market includes many devices
  with 1–2 GB of RAM; this hypothesis must be validated against Play Console device
  distribution data for India before the minimum is finalised.
- Camera requirements: must work with devices that have a rear camera but limited autofocus.
- Storage: app size must be optimised for devices with limited internal storage.
- Offline-first design is mandatory; not all low-cost devices have reliable data connectivity.
- Performance benchmarks must be run on representative low-cost devices. See BENCHMARK_PLAN.md.

---

## Network-constrained and offline behaviour

- Core capture, organisation and export must work with no network connection.
- App must handle network switches (WiFi to mobile data) gracefully during backup.
- Slow-network conditions (2G/3G) must not cause the app to appear frozen during OTP dispatch.
- OTP timeout and retry UI must be clearly communicated.
- Background sync must use a work-queue pattern; failures must not block foreground use.
- Upload resumability is required for cloud backup on slow or interrupted connections.

---

## Tablet usage

- Android tablet layout is required before GA. See DA-007.
- iPad layout is required before GA. See DA-008.
- Tablets are used for document review; larger screen layouts should use the additional space
  for document preview and multi-column organisation.
- Tablet layout does not gate the phone launch but must be complete before GA.

---

## Internationalisation requirements for future expansion

The India launch must be implemented in a way that does not block future international expansion:

- All localised strings must be externalised (no hardcoded UI strings in code).
- Currency and number formatting must use locale APIs; no hardcoded INR formatting in domain code.
- Date and time formatting must use locale APIs.
- Phone-number input must be designed for extension to international formats (not India-only).
- Backend APIs must accept locale and currency codes as parameters, not hardcode India values.
- Entitlement prices must be store-driven; no hardcoded prices in the app binary.

---

## India launch-readiness checklist

| Item | Status |
|------|--------|
| English, Hindi and Kannada UI strings complete and reviewed | Pending |
| Hindi and Kannada font rendering verified on representative devices | Pending |
| Indian phone-number formatting implemented | Pending |
| OTP rate limiting and lockout controls in place | Pending |
| DPDP Act 2023 obligations mapped | Pending — **[Legal gate]** |
| Privacy notice published and reviewed by legal counsel | Pending — **[Legal gate]** |
| Grievance officer designated | Pending — **[Legal gate]** |
| GST display requirements confirmed | Pending — **[Legal gate]** |
| Google Play Store subscription products created | Pending |
| Apple App Store subscription products created | Pending |
| India pricing validated per MONETIZATION.md framework | Pending |
| Domain clearance (toollyscan.com / toollyscan.in) | Pending — D-007 |
| Trademark clearance ("Toolly" in India) | Pending — D-007 |
| Firebase service-by-service processing locations, transfers and retention reviewed; Authentication is not represented as India-resident | Pending — **[Legal gate]** |
| Low-cost Android device benchmark complete | Pending |
| Accessibility audit (TalkBack, VoiceOver, WCAG 2.1 AA) | Pending |
| Performance benchmarks on representative devices meet targets | Pending |
| Secret scan passes | Passing |
| Markdown lint passes | Passing |
| All Production Gate items approved | Pending |
