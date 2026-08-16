package com.toolly.shared.edit

/**
 * A hand-drawn signature, as a list of strokes -- each stroke a list of touch points normalized
 * to the signature pad's own 0..1 coordinate space, mirroring [CropRegion]'s existing
 * [NormalizedPoint]-based, resolution-independent pattern. Nothing platform-specific crosses this
 * boundary: the capture UI is pure Compose Multiplatform drawing (`SignaturePadScreen`), so this
 * works identically on Android and iOS, and only the final rasterization onto an export page is
 * platform-specific.
 *
 * This is a simple visual stamp, not a legally-binding e-signature: no identity verification, no
 * audit trail, no cryptographic seal. See `AndroidDocumentExporter`'s use of this type for where
 * it actually gets drawn.
 */
typealias SignatureStrokes = List<List<NormalizedPoint>>
