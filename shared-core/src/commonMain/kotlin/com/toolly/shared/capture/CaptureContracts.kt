package com.toolly.shared.capture

import kotlin.jvm.JvmInline

/**
 * Provider-neutral port for one document-capture session.
 *
 * Implementations own every platform or SDK integration. Android/iOS types, paths, URIs,
 * provider handles, document bytes and arbitrary provider exceptions must not cross this boundary.
 */
interface DocumentScanner {
    /**
     * Suspends until capture succeeds, is cancelled, or fails.
     *
     * A successful result transfers temporary-asset ownership to the caller. Implementations must
     * release assets created by failed, cancelled or interrupted sessions.
     */
    suspend fun launch(config: ScanConfig): ScanResult
}

/**
 * Configuration for a single capture session.
 *
 * This value contains no user data, paths, tokens or credentials.
 */
data class ScanConfig(
    val maxPages: Int = 10,
    val galleryImportEnabled: Boolean = false,
) {
    init {
        require(maxPages in MIN_PAGES..MAX_PAGES) {
            "maxPages must be between $MIN_PAGES and $MAX_PAGES"
        }
    }

    companion object {
        const val MIN_PAGES = 1
        const val MAX_PAGES = 50
    }
}

/** Terminal outcome from [DocumentScanner.launch]. */
sealed class ScanResult {
    /** One or more ordered pages were captured. */
    data class Success(val pages: List<ScannedPage>) : ScanResult()

    /** The user explicitly cancelled. No pages transfer to the caller. */
    data object Cancelled : ScanResult()

    /** Capture failed with an allowlisted, non-sensitive error. */
    data class Failure(val error: ScanError) : ScanResult()
}

/**
 * One ordered captured page.
 *
 * [assetId] is not a path, URI, filename or provider handle. Only a platform adapter may resolve
 * it, and the caller must release it after display or promotion into the encrypted vault.
 */
data class ScannedPage(
    val index: Int,
    val assetId: TemporaryAssetId,
)

/** Opaque Toolly identifier for one temporary capture asset. */
@JvmInline
value class TemporaryAssetId(val value: String) {
    init {
        require(value.matches(VALID_ID)) { "Invalid temporary asset identifier" }
    }

    companion object {
        private val VALID_ID = Regex("[a-f0-9]{32}")
    }
}

/**
 * Allowlisted capture errors.
 *
 * These values never contain document pixels, OCR text, paths, filenames, identifiers, provider
 * exception messages or other user data.
 */
sealed class ScanError {
    data object ServiceUnavailable : ScanError()
    data object PermissionDenied : ScanError()
    data object Busy : ScanError()
    data object InvalidResult : ScanError()
    data object StorageFailure : ScanError()
    data object LifecycleEnded : ScanError()

    data class PartialCapture(
        val capturedPages: List<ScannedPage>,
        val reason: PartialCaptureReason,
    ) : ScanError()
}

/** Allowlisted reasons for a validated partial capture. */
enum class PartialCaptureReason {
    SOURCE_READ_FAILED,
    STORAGE_WRITE_FAILED,
    SESSION_INTERRUPTED,
}
