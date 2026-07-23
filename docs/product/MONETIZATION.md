# Monetization

Toolly monetization strategy baseline.

Final prices must not be invented here. This document establishes the monetization model,
cost structure and a pricing-decision framework. Prices must be validated with real cost data,
competitor benchmarks and willingness-to-pay research before being set.

---

## Model

Toolly uses a **freemium, subscription-first** model.

- A useful free tier retains users and builds trust.
- Premium subscription unlocks advanced processing, cloud backup, sync and priority support.
- There are no intrusive advertisements.
- There are no one-time purchase options in V1.
- No watermark is added to free exports.

---

## Subscription plans

| Plan | Billing | Notes |
|------|---------|-------|
| Monthly | Monthly auto-renewal | Higher unit price; lower commitment barrier. |
| Annual | Annual auto-renewal | Discounted unit price; better retention and LTV. |

Target conversion: annual plans drive higher LTV and lower churn. Introductory offers should
prioritise annual plan trial conversion.

---

## India pricing research requirement

Final prices for India are **not defined here**. Prices must be derived from:

1. Competitor benchmark (see framework below).
2. Willingness-to-pay research with Indian users.
3. Cloud cost per active free and premium user.
4. Google Play and App Store commission (typically 15–30 %).
5. Applicable Indian taxes (GST).
6. Support cost per user segment.
7. Target gross margin.

Indian users are highly price-sensitive. Regional pricing on Google Play and the App Store must
be evaluated. Google Play Pass and Apple Arcade are not applicable to this model.

---

## International regional pricing requirement

International pricing must not be a uniform conversion of INR amounts. Each market requires:

- Local currency display.
- Willingness-to-pay validation.
- Local tax obligations assessed.
- App store regional pricing tiers evaluated.

International pricing is a post-V1 requirement.

---

## Introductory offer rules

- Introductory free trial must be offered to first-time subscribers only.
- Trial duration is a hypothesis requiring conversion-rate validation. **[H-006]**
- Introductory price offers are permitted but must not misrepresent the post-offer price.
- Trial terms must be clearly communicated before the user subscribes.
- Trial must be honoured even if the user cancels before the trial ends.

---

## Free-trial evaluation

A free trial is the recommended acquisition path for premium conversion. The trial period gives
users time to experience OCR, cloud backup and advanced processing.

Key questions requiring evidence:

- What trial length maximises conversion without inflating cloud costs? **[H-006]**
- Does a trial-to-annual funnel outperform trial-to-monthly?
- What is the refund rate for users who forget to cancel?

---

## Store commission considerations

- Google Play Store commission: 15 % for subscriptions after the first year; 30 % in year 1
  (subject to Google Play policies at time of launch; verify current rates).
- Apple App Store commission: 15 % for subscriptions after the first year; 30 % in year 1
  (subject to Apple policies at time of launch; verify current rates).
- Commission directly reduces net revenue per subscriber.
- Commission must be factored into the pricing framework before prices are set.

---

## Tax and invoicing considerations

- GST applies to digital subscriptions sold to Indian users. Rate must be confirmed with a
  qualified tax advisor before launch.
- Stores typically collect and remit tax on behalf of developers in supported markets; confirm
  applicable markets and rates.
- Invoicing and GST compliance requirements for Indian users must be assessed before launch.
- Legal review is a launch gate. Do not make tax claims without legal confirmation.

---

## Cloud cost structure

Cloud cost directly impacts subscription pricing viability. The following cost drivers must be
modelled before prices are finalised.

### Cost per active free user (monthly estimate)

| Item | Notes |
|------|-------|
| Firebase Authentication (OTP) | Per-OTP SMS cost; controlled by rate limits in COST_CONTROLS.md. |
| Firestore reads/writes | Entitlement cache, account metadata. |
| App Check | Per-request. |
| Cloud Functions | Receipt verification, account operations. |
| Storage | None for free users with backup disabled. |

### Cost per active premium user (monthly estimate)

All free-user costs plus:

| Item | Notes |
|------|-------|
| Firebase Storage | Backup storage at **[H-002]** up to 5 GB. |
| Storage egress | Downloads and restores; India data-residency requirement affects region selection. |
| Cloud Functions | Backup operations, sync operations. |
| OCR compute | On-device OCR has no server cost; server OCR (if used) has direct cost. |
| Advanced processing | Server-side processing cost where applicable. |

All cost estimates must be validated with Firebase pricing and the COST_CONTROLS.md budget model
before prices are finalised.

---

## Storage and egress exposure

Storage and egress are the largest variable cost drivers for premium users.

| Scenario | Risk |
|----------|------|
| User stores very large documents | Backup storage cost grows without bound without a quota. |
| User restores on multiple devices | Egress cost per restore; limits required. |
| User deletes account | Egress cost for data export; must be supported. |
| Abuse: automated bulk upload | Storage and egress abuse; requires abuse controls. |

Storage quota **[H-002]** must be enforced. Egress limits may be required.

---

## OCR and processing cost exposure

| Scenario | Risk |
|----------|------|
| On-device OCR | No server cost; device battery and thermal cost only. |
| Server-side OCR | Cost per page; must be rate-limited. |
| Server-side advanced processing | Cost per operation; must be rate-limited. |
| Abuse: bulk OCR via API | Cost amplification; requires per-user rate limits. |

Server-side OCR and processing must not be enabled without rate limits and abuse controls in
place. See COST_CONTROLS.md.

---

## Abuse scenarios

| Scenario | Control |
|----------|---------|
| Trial cycling (cancel and re-subscribe for perpetual trial) | Platform prevents trial re-use for same Apple ID / Google account. |
| Bulk document upload to inflate storage | Storage quota; upload rate limits. |
| Automated OCR via reverse-engineered API | App Check; per-user rate limits. |
| Refund abuse | Platform refund policy; refund rate monitoring. |
| Subscription sharing | Platform subscription terms; device limits. |

---

## Cancellation and refund considerations

- Cancellation stops auto-renewal; access continues to end of paid period.
- Refunds are handled by the platform store; Toolly does not process refunds directly.
- Refund events from the platform must be handled by the subscription lifecycle
  (see SUBSCRIPTION_LIFECYCLE.md).
- Local documents must not be deleted on refund.
- Refund rate must be monitored as a health metric.

---

## Pricing experiment guardrails

- No pricing change may be made without updating DECISION_REGISTER.md.
- A/B pricing tests must not show different prices to users in the same market on the same
  platform without store policy compliance.
- Introductory price changes must give existing subscribers adequate notice per store policy.
- Price increases for existing subscribers require platform-compliant notice and consent.

---

## Ethical monetization rules

1. Do not use dark patterns to prevent cancellation.
2. Do not hide the subscription price before the user commits.
3. Do not use scare messaging about document loss on expiry; local documents are never deleted.
4. Do not require a premium subscription to export documents the user already owns.
5. Do not send unsolicited marketing emails without opt-in.
6. Do not use aggressive push notifications to upsell.
7. Subscription terms must be clearly presented before purchase.
8. Trial terms (including auto-renewal date and price) must be clearly presented before trial start.
9. Cancellation must be as easy as subscription.
10. DPDP Act 2023 consent requirements must be met before launch.

---

## Pricing decision framework

The following framework must be completed before final prices are set.

| Factor | Required evidence | Status |
|--------|------------------|--------|
| Competitor benchmark | Survey top 5 competitor apps in India; record monthly and annual prices | Pending |
| Willingness-to-pay research | User interviews and survey with target segments in India | Pending |
| Cloud cost per free user | Firebase cost model at projected free-user volume | Pending |
| Cloud cost per premium user | Firebase cost model at projected premium-user volume including storage | Pending |
| Store commission | Confirm current Google Play and App Store rates | Pending |
| Tax (GST) | Confirm applicable GST rate with qualified tax advisor | Pending — legal gate |
| Support cost per user | Estimate based on support tooling and staff | Pending |
| Target gross margin | Board-approved margin target | Pending |
| Monthly vs. annual conversion | Estimate from trial data and comparable apps | Pending |
| Retention and churn model | Estimate from comparable apps; validate with cohort data post-launch | Pending |
| Refund rate estimate | Estimate from comparable apps; validate post-launch | Pending |

---

## Monetization risk register

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|-----------|
| Cloud cost exceeds subscription revenue | Medium | High | Cost model required before pricing; quota controls in COST_CONTROLS.md. |
| Low conversion rate in price-sensitive India market | High | High | Willingness-to-pay research required; competitive pricing. |
| Store commission reduces margin below target | Medium | Medium | Factor into pricing framework; evaluate alternative billing where permitted. |
| Trial abuse / cancellation before charge | Medium | Low | Platform prevents trial re-use; monitor refund rate. |
| Refund rate exceeds platform thresholds | Low | High | Monitor refund rate; improve onboarding and trial experience. |
| Indian tax compliance gap | Low | High | Legal gate before launch; confirm GST requirements. |
| Price increase triggers churn | Low | Medium | Grandfathering and notice periods per store policy. |
| Entitlement system offline failure | Low | High | Offline cache; must not block local document access. |
