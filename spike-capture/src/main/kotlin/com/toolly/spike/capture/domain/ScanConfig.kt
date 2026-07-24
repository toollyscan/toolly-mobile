package com.toolly.spike.capture.domain

/**
 * Configuration for a single capture session.
 *
 * All fields have safe defaults that satisfy the spike's synthetic-document requirements.
 * No user PII, production-document paths, tokens or credentials are carried here.
 */
data class ScanConfig(
    /** Maximum number of pages per capture session. Bounded to prevent unbounded memory use. */
    val maxPages: Int = 10,
    /** Whether the scanner UI should offer gallery import in addition to live camera. */
    val galleryImportEnabled: Boolean = false,
)
