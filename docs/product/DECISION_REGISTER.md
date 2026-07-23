# Decision Register

This register records every significant product, architecture and process decision for Toolly.
Add a row when a decision is made. Update the status column when a decision is reversed or superseded.

| ID | Date | Status | Title | Rationale | Owner |
|----|------|--------|-------|-----------|-------|
| D-001 | 2025-07-01 | Accepted | India-first launch | Largest addressable market for document-scanning utility apps; UPI and Aadhaar workflows are a differentiator. | shivayogih |
| D-002 | 2025-07-01 | Accepted | English, Hindi and Kannada as launch languages | Covers the primary developer market (Bengaluru) and national language requirements. | shivayogih |
| D-003 | 2025-07-01 | Accepted | Android and iOS dual-platform from day one | Avoid market fragmentation; KMP boundary ADR manages shared code risk. | shivayogih |
| D-004 | 2025-07-01 | Accepted | Encrypted local vault as source of truth | Privacy-first requirement; offline-first requirement; cloud is optional. See ADR-0002. | shivayogih |
| D-005 | 2025-07-01 | Accepted | Firebase as initial cloud provider | Time-to-market; well-understood; migration plan documented. See ADR-0003. | shivayogih |
| D-006 | 2025-07-01 | Accepted | Toolly-owned canonical document and account IDs | Prevents provider lock-in and enables migration without data loss. See ADR-0004. | shivayogih |
| D-007 | 2025-07-01 | Accepted | Domain and trademark clearance are release blockers | toollyscan.com / toollyscan.in must be owned; "Toolly" trademark must be cleared in India before public release. | shivayogih |
| D-008 | 2025-07-01 | Accepted | No commercial scanning or OCR SDK without approved ADR | Licence, binary-size and removal risk must be evaluated before any commercial SDK is added. | shivayogih |
| D-009 | 2025-07-01 | Accepted | Production feature implementation blocked until Production Gate is approved | Ensures architecture and security review before any user data is processed. | shivayogih |
| D-010 | 2025-07-01 | Accepted | Indian data-protection compliance is a launch requirement | DPDP Act 2023 obligations must be mapped before launch. | shivayogih |
