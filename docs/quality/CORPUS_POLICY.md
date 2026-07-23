# Benchmark Corpus Policy

## Purpose

Toolly needs representative document conditions without turning the repository or benchmark infrastructure into a personal-data store. The corpus starts synthetic. Any redacted or licensed material requires a recorded provenance and privacy review.

## Allowed sources

| Source | Default status | Conditions |
|--------|----------------|------------|
| Programmatically generated document | Allowed | No real identity or production data; generator/version recorded |
| Manually authored fictional document | Allowed | Names, numbers, addresses and codes are unmistakably fictional |
| Publicly licensed sample | Review required | Licence, source URL, hash and permitted use recorded |
| Irreversibly redacted real document | Exceptional review | Consent, redaction verification, purpose and retention recorded |
| Production/user/support document | Prohibited | Never used for benchmark development or evidence |

Government identity layouts must not reproduce a valid credential, scannable identifier, portrait or machine-readable code. Synthetic examples must carry a visible `SAMPLE — NOT VALID` marker where layout realism could cause confusion.

## Required languages and cohorts

The corpus manifest defines required counts; it does not claim the files exist until each item is registered with a digest.

Required language coverage:

- English;
- Hindi;
- Kannada;
- mixed-language pages where product flows support them.

Required condition cohorts:

- clean printed A4;
- compact card-like layout without valid identity data;
- multi-page document;
- low light;
- blur or motion;
- glare or shadow;
- skew and perspective distortion;
- low contrast;
- dense tables;
- handwriting, reported separately from printed text.

## Item manifest

Each corpus item records:

- stable item ID and corpus version;
- source type and provenance;
- language/script and document cohort;
- synthetic/redacted status;
- content and ground-truth file digests;
- generator and parameters when generated;
- expected page geometry and OCR ground truth when applicable;
- licence and retention decision;
- privacy-review status.

Changing pixels, ground truth, normalization or labels creates a new digest and corpus version. An item ID is never silently reused for different content.

## OCR ground truth

- Ground truth uses Unicode and records normalization form.
- Punctuation, whitespace, case, numerals and ligature handling are explicit.
- Printed and handwritten cohorts are never pooled without separate results.
- Character error rate and word error rate are reported by language and cohort.
- Reviewers inspect a sample of ground truth before accepting a run.

## Geometry ground truth

Corner coordinates use the original image coordinate space and record width, height and orientation. A labelled quadrilateral must be convex, ordered consistently and visually reviewed. Ambiguous or partially occluded pages use an explicit `not_scored` reason instead of invented corners.

## Repository rules

- Corpus binaries are not committed until provenance and privacy checks pass.
- EXIF, thumbnails and embedded metadata are stripped from approved images.
- Archives and artifacts are secret-scanned before upload.
- Filenames contain stable IDs, not names, phone numbers or document titles.
- `benchmarks/corpus/manifest.v1.json` is the machine-readable source of cohort requirements and registered items.

## Consent and deletion

If an exceptional redacted source is approved, consent withdrawal or a deletion request removes it from active corpora and records a tombstone. Historical benchmark decisions using that item are reviewed for reproducibility and replacement; Git history is not treated as a suitable store for revocable material.
