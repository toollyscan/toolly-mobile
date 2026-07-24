package com.toolly.spike.capture.domain

/**
 * Toolly-owned routing boundary that retries a provider-neutral fallback only when the
 * primary scanner reports that its service is unavailable.
 */
class FallbackDocumentScanner(
    private val primary: DocumentScanner,
    private val fallback: DocumentScanner,
) : DocumentScanner {

    override suspend fun launch(config: ScanConfig): ScanResult {
        val primaryResult = primary.launch(config)
        return if (
            primaryResult is ScanResult.Failure &&
            primaryResult.error is ScanError.ServiceUnavailable
        ) {
            fallback.launch(config)
        } else {
            primaryResult
        }
    }
}
