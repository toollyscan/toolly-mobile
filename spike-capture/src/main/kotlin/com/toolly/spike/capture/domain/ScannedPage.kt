package com.toolly.spike.capture.domain

/**
 * A single captured page returned by [DocumentScanner].
 *
 * [imageUri] is an opaque string URI referencing a temporary plaintext JPEG in
 * app-private storage. Callers must not log, share or persist it. The owning adapter
 * deletes the underlying file when the capture session ends.
 */
data class ScannedPage(
    /** Zero-based page index within the capture session. */
    val index: Int,
    /** Opaque URI string; backed by a temporary file in app-private storage. */
    val imageUri: String,
)
