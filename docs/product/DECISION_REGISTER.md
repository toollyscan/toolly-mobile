# Decision Register

This register records every significant product, architecture and process decision for Toolly.
Add a row when a decision is made. Update the status column when a decision is reversed or superseded.

| ID | Date | Status | Title | Rationale | Owner |
|----|------|--------|-------|-----------|-------|
| D-001 | 2026-07-23 | Accepted | India-first launch | India is the selected first market; market sizing and workflow differentiation require evidence before launch claims are made. | shivayogih |
| D-002 | 2026-07-23 | Accepted | English, Hindi and Kannada as launch languages | Supports the selected India-first audience and Karnataka launch strategy; translation quality requires native-language review. | shivayogih |
| D-003 | 2026-07-23 | Accepted | Android and iOS dual-platform from day one | Avoid market fragmentation; KMP boundary ADR manages shared code risk. | shivayogih |
| D-004 | 2026-07-23 | Accepted | Encrypted local vault as source of truth | Privacy-first requirement; offline-first requirement; cloud is optional. See ADR-0002. | shivayogih |
| D-005 | 2026-07-23 | Accepted | Firebase as the approved cloud provider | Firebase is approved for initial development and production releases. AWS migration may be evaluated after approximately two years based on cost, scale, reliability and business needs; no AWS implementation is included now. See ADR-0003. | shivayogih |
| D-006 | 2026-07-23 | Accepted | Toolly-owned canonical document and account IDs | Prevents provider lock-in and enables migration without data loss. See ADR-0004. | shivayogih |
| D-007 | 2026-07-23 | Accepted | Domain and trademark clearance are release blockers | toollyscan.com / toollyscan.in must be owned; "Toolly" trademark must be cleared in India before public release. | shivayogih |
| D-008 | 2026-07-23 | Accepted | No commercial scanning or OCR SDK without approved ADR | Licence, binary-size and removal risk must be evaluated before any commercial SDK is added. | shivayogih |
| D-009 | 2026-07-23 | Accepted | Production feature implementation blocked until Production Gate is approved | Ensures architecture and security review before any user data is processed. | shivayogih |
| D-010 | 2026-07-23 | Accepted | Indian data-protection compliance is a launch requirement | DPDP Act 2023 obligations must be mapped before launch. | shivayogih |
| D-011 | 2026-07-23 | Hypothesis | Freemium subscription-first monetization model | Core scanning is free; premium subscription unlocks advanced processing, cloud backup and sync. Final prices require willingness-to-pay research and cost validation before being set. | shivayogih |
| D-012 | 2026-07-23 | Accepted | No watermark on standard free exports | Normal export of user-owned documents must never carry a Toolly watermark. | shivayogih |
| D-013 | 2026-07-23 | Accepted | Subscription expiry must not remove or block access to local documents | Local documents are owned by the user; billing state is advisory and must not gate access to the local vault. | shivayogih |
| D-014 | 2026-07-23 | Accepted | Store billing types must not enter domain entitlement models | Google Play and App Store transaction types are wrapped in Toolly-owned entitlement contracts; billing provider is replaceable. | shivayogih |
| D-015 | 2026-07-23 | Accepted | Entitlement cache must support offline use | Entitlement state must be cacheable with an explicit freshness policy; stale cache must not block local document access. | shivayogih |
| D-016 | 2026-07-23 | Accepted | Backend entitlement verification must be idempotent | Re-submitting the same purchase token must not create duplicate entitlement records; required for reliable retry and migration. | shivayogih |
| D-017 | 2026-07-23 | Hypothesis | Free batch page limit of 10 pages per document | Hypothesis H-001; requires cost analysis and user validation before being finalised. | shivayogih |
| D-018 | 2026-07-23 | Hypothesis | Premium cloud-storage allowance of 5 GB | Hypothesis H-002; requires cloud cost modelling before being finalised. | shivayogih |
| D-019 | 2026-07-23 | Accepted | No production SDK offering during initial app launch | SDK offering is deferred until after initial launch; scope is limited to the consumer application. | shivayogih |
| D-020 | 2026-07-23 | Accepted | Document analytics prohibition | Document images, OCR text, filenames, phone numbers, OTPs, tokens and key material must never enter analytics or logs. | shivayogih |
| D-021 | 2026-07-23 | Accepted | Login before first scan | V1 requires authentication before first capture; guest scanning is excluded unless this decision is explicitly superseded. | shivayogih |
| D-022 | 2026-07-23 | Accepted | V1 authentication methods | V1 supports phone OTP, email/password, Google and Apple Sign In on iOS behind Toolly-owned identity contracts. | shivayogih |
| D-023 | 2026-07-23 | Accepted | Firebase-first, AWS evaluation later | Firebase is implemented now; AWS migration may be evaluated after approximately two years and is not a committed deadline. | shivayogih |
