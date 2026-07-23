# Benchmark Metric Contracts

The machine-readable registry is `benchmarks/contracts/metrics.v1.json`. Metric IDs, units and direction are stable within a registry version.

## General rules

- Latency uses monotonic time and reports individual samples in milliseconds.
- Memory reports mebibytes, not ambiguous `MB`.
- File and model size use bytes.
- Ratios use the closed range 0–1; percentages are presentation only.
- Each aggregate includes sample count, median, p90, p95, minimum and maximum where meaningful.
- Accuracy aggregates remain separated by language, script and cohort.
- Aborted, timed-out and failed samples record status and reason; they are not converted to zero or discarded.

## Camera

| Metric ID | Unit | Better | Definition |
|-----------|------|--------|------------|
| `camera.first_frame` | ms | lower | User capture intent to first valid preview frame |
| `camera.capture_to_buffer` | ms | lower | Capture request accepted to immutable image buffer available |
| `camera.buffer_transfer` | ms | lower | Native buffer available to Toolly processing boundary without including processing |
| `camera.capture_success` | ratio | higher | Successful valid captures divided by attempts |
| `camera.peak_memory` | MiB | lower | Peak process memory attributable to the run window |

## Geometry and enhancement

| Metric ID | Unit | Better | Definition |
|-----------|------|--------|------------|
| `geometry.pipeline_latency` | ms | lower | Input buffer accepted to validated quadrilateral/result |
| `geometry.quad_iou` | ratio | higher | Intersection-over-union against labelled page polygon |
| `geometry.corner_error` | ratio | lower | Mean corner distance normalized by image diagonal |
| `geometry.detection_success` | ratio | higher | Correct scored detections divided by eligible cases |
| `enhancement.pipeline_latency` | ms | lower | Recipe input to immutable enhanced output |
| `enhancement.peak_memory` | MiB | lower | Peak process memory during the enhancement window |

Visual-quality methods must be defined per candidate. A generic image score does not replace human inspection for readability, clipping or colour distortion.

## PDF and export

| Metric ID | Unit | Better | Definition |
|-----------|------|--------|------------|
| `pdf.generate_latency` | ms | lower | Valid page inputs to closed, readable PDF |
| `pdf.output_size` | bytes | contextual | Final file length |
| `pdf.peak_memory` | MiB | lower | Peak process memory during generation |
| `pdf.validation_success` | ratio | higher | Outputs passing structure, page-count and reopen validation |
| `export.temporary_plaintext_lifetime` | ms | lower | Time a reviewed plaintext temporary artifact exists |

## OCR

| Metric ID | Unit | Better | Definition |
|-----------|------|--------|------------|
| `ocr.character_error_rate` | ratio | lower | Unicode edit distance divided by ground-truth character count |
| `ocr.word_error_rate` | ratio | lower | Token edit distance divided by ground-truth token count |
| `ocr.latency` | ms | lower | Immutable page input to final recognized result |
| `ocr.peak_memory` | MiB | lower | Peak process memory during recognition |
| `ocr.artifact_size` | bytes | lower | Incremental packaged model/application size attributed to the candidate |
| `ocr.offline_success` | ratio | higher | Eligible cases completed with network disabled |

Printed and handwriting results are always reported separately.

## Encrypted vault

| Metric ID | Unit | Better | Definition |
|-----------|------|--------|------------|
| `vault.open_latency` | ms | lower | Locked process state to usable authorized vault |
| `vault.write_latency` | ms | lower | Transaction start to durable committed asset/metadata/outbox state |
| `vault.read_latency` | ms | lower | Read request to authenticated immutable plaintext buffer |
| `vault.peak_memory` | MiB | lower | Peak process memory during operation |
| `vault.storage_overhead` | ratio | lower | Additional encrypted representation bytes divided by plaintext bytes |
| `vault.tamper_detection` | ratio | higher | Tampered cases rejected without plaintext publication |
| `vault.recovery_success` | ratio | higher | Eligible interrupted/corrupt scenarios recovered to the specified safe state |

Throughput cannot approve an algorithm suite. Security invariants and qualified review remain independent gates.

## Planning targets

Values previously listed in `BENCHMARK_PLAN.md` are planning hypotheses. A target becomes an approved release threshold only when it has:

- a metric ID and protocol version;
- product rationale and user-impact basis;
- representative physical-device baseline;
- variance and failure-rate analysis;
- accountable owner and review date.
