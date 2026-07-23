# Benchmark Device Matrix

## Selection model

Toolly uses capability tiers rather than permanently naming one phone as representative of India. Market share, OS distribution and available hardware change. Exact devices are recorded in each run manifest and the active execution matrix is approved before TLY-006G.

## Required tiers

### Android

| Tier | Required characteristics | Purpose |
|------|--------------------------|---------|
| A-low | Supported API near the minimum, 3–4 GiB RAM, 64-bit, constrained CPU/GPU/storage | Lower-bound latency, memory and thermal behavior |
| A-mid | Current broadly deployed Android release, 6–8 GiB RAM | Main product baseline |
| A-high | Recent flagship CPU/GPU, 8+ GiB RAM | Upper-bound and accelerated-path behavior |
| A-tablet | Supported tablet, minimum 6 GiB RAM | Large-page review, rotation and memory behavior |

Android API 26 remains the current minimum hypothesis. Actual support requires build, security-provider and device-distribution evidence before approval.

### Apple

| Tier | Required characteristics | Purpose |
|------|--------------------------|---------|
| I-low | Oldest supported iPhone class and smallest supported screen | Lower-bound and layout behavior |
| I-mid | Main supported iPhone class | Product baseline |
| I-high | Recent Pro-class hardware | Upper-bound and accelerated-path behavior |
| I-tablet | Base iPad plus large-screen class when behavior differs | Tablet and large-document behavior |

The minimum iOS version is selected during platform scaffolding and recorded before measurements.

## Physical-device requirement

Architecture or release decisions require physical devices for each applicable tier. Emulator/simulator runs may validate determinism, failure injection and CI regressions but are marked `supplemental`.

## Environment fields

Every run records:

- manufacturer and model;
- stable internal device ID that is not a hardware serial;
- platform, OS/build and security patch where applicable;
- CPU architecture and available RAM;
- app/build variant and Git commit;
- locale, language and display scale;
- battery state, charging state and power mode;
- free storage;
- initial and final thermal state;
- camera characteristics for capture runs;
- emulator/simulator/physical classification.

Hardware serial numbers, advertising IDs, personal device names and account identifiers are prohibited.

## Run conditions

- Reboot or stabilization rules are protocol-specific and recorded.
- Background workload and network state are controlled or documented.
- Performance runs use release-like builds; debug-only numbers are supplemental.
- Warm-up and cooldown are fixed by protocol.
- Thermal throttling is recorded, not hidden by rerunning until a favourable result appears.
- At least three independent run sessions are required for a decision candidate unless a protocol justifies another count.

## Matrix review

Before execution, the product and engineering owners approve:

- exact devices and ownership/access;
- OS versions and upgrade policy;
- required language/locale combinations;
- excluded tiers with rationale and expiry;
- replacement rules for unavailable or failed hardware.

Missing tiers remain blocked. One high-end device cannot stand in for the low or mid tier.
