package com.toolly.spike.capture.domain

/**
 * Toolly-owned port for document capture.
 *
 * Implementations must hide all SDK-specific types. The port is intentionally free of
 * Android, iOS and third-party SDK imports so it can be tested on the JVM and later moved
 * to a shared KMP contracts module without changes.
 *
 * The initial Android adapter uses Google ML Kit Document Scanner. A CameraX/manual fallback
 * remains behind the same port and is selected when Play Services or ML Kit are unavailable.
 */
interface DocumentScanner {
    /**
     * Launch a capture session and suspend until the user completes, cancels, or an error
     * occurs. On success, ownership of every returned temporary asset transfers to the
     * caller until it is explicitly released. Implementations must clean up assets created
     * by failed, cancelled, or interrupted sessions.
     */
    suspend fun launch(config: ScanConfig): ScanResult
}
