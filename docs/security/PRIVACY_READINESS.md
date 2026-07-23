# India Privacy Readiness

This is an engineering readiness map, not legal advice. Qualified Indian privacy counsel must
confirm applicability, notices, lawful processing, cross-border transfers, children-related
requirements, grievance handling, breach notifications and commencement dates before launch.

## Regulatory status snapshot

The Digital Personal Data Protection Act, 2023 is published on India Code. The Digital Personal
Data Protection Rules, 2025 were notified with phased commencement. As of 2026-07-23, the
repository must not assume every substantive obligation has the same effective date. Counsel must
revalidate the current Act, Rules, notifications and corrigenda at each release gate.

## Engineering control map

| Area | Toolly control | Evidence | Status |
|------|----------------|----------|--------|
| Clear notice | Layered notice before identity/backup/telemetry processing | UX and legal review | Pending |
| Consent/choice | Explicit backup enable; withdrawal path; no preselection | Flow tests | Pending |
| Purpose limitation | Data inventory and field-onboarding gate | Register review | Defined |
| Minimisation | Local-first, ciphertext-only backup, telemetry denylist | Network/storage tests | Defined |
| Accuracy/correction | Profile/provider correction path | Integration tests | Pending |
| Access/export | Account processing summary and document export | End-to-end tests | Pending |
| Erasure | Idempotent local/provider deletion orchestration | Processor confirmations | Defined; unimplemented |
| Security safeguards | Threat model, key/auth/incident controls | Independent assessment | Pending |
| Breach response | Incident runbook and counsel-led notification decision | Tabletop | Defined; untested |
| Grievance | In-app/web contact and tracked handling | Legal/operations evidence | Pending |
| Children | Age/audience decision and child-data controls | Product/legal decision | Blocked |
| Processor governance | Firebase/service register and terms review | Signed assessment | Pending |
| Cross-border processing | Per-service location/transfer review | Counsel review | Pending |
| Retention | Versioned purpose-based schedule | Legal approval and deletion tests | Pending |

## Required user controls

- see what account/backup/telemetry processing is enabled;
- disable optional backup and choose cloud-copy handling;
- export owned documents without premium entitlement;
- request correction/deletion through accessible UI;
- contact grievance/support without disclosing document content;
- receive clear security and deletion status notifications;
- use English, Hindi and Kannada notices after qualified translation review.

## Firebase location correction

Do not use “Firebase India residency” as a single checkbox. Firebase Authentication is documented
as US-only, while selected data services can have configurable locations. The release gate is a
service-by-service processing and transfer decision, not a blanket India-region claim.

## Children and family documents

Toolly may scan documents relating to children even when the account holder is an adult. Product
and legal review must distinguish account user age from document subject, minimise inference, and
avoid analytics/advertising use. No age assurance or parental-consent implementation is approved
by this document.

## Legal review packet

Provide counsel:

- product/data flow and classifications;
- Firebase and store processing inventory/terms;
- notices, consent and withdrawal UX;
- retention schedule and deletion/export design;
- security safeguards and incident runbook;
- supported countries/languages and audience;
- support/grievance process;
- children-related scope;
- cross-border/service locations;
- store privacy disclosures and vendor list.

## Primary references

- India Code, Digital Personal Data Protection Act, 2023:
  <https://www.indiacode.nic.in/handle/123456789/22037>
- Digital Personal Data Protection Rules, 2025, Gazette notification G.S.R. 846(E), plus applicable
  commencement notifications and corrigenda.

Reference status checked 2026-07-23. Legal counsel must verify operative text and dates.
