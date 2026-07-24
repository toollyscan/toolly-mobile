package com.toolly.spike.capture.domain

/**
 * Toolly-owned port for document capture.
 *
 * Implementations must hide all SDK-specific types. The port is intentionally free of
 * Android, iOS and third-party SDK imports so it can be tested on the JVM and later moved
 * to a shared KMP contracts module without changes.
 *
 * The initial Android adapter uses Google ML Kit Document Scanner. A CameraX/manual fallback
 * is provided as a stub and selected when Play Services or ML Kit are unavailable.
 */
interface DocumentScanner {
    /**
     * Launch a capture session and suspend until the user completes, cancels, or an error
     * occurs. Implementations must delete any temporary plaintext artifacts when the
     * [ScanResult] is delivered, including on cancellation and failure.
     */
    suspend fun launch(config: ScanConfig): ScanResult
}
