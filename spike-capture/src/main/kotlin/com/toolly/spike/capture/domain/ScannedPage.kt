package com.toolly.spike.capture.domain

/**
 * A single captured page returned by [DocumentScanner].
 *
 * [assetId] is a Toolly-owned identifier for a temporary plaintext JPEG in app-private
 * storage. It is not a path, URI, filename, or provider handle. Callers must release the
 * asset when it is no longer displayed or promoted into the encrypted vault.
 */
data class ScannedPage(
    /** Zero-based page index within the capture session. */
    val index: Int,
    /** Provider-neutral identifier resolved only inside the Android adapter layer. */
    val assetId: TemporaryAssetId,
)

@JvmInline
value class TemporaryAssetId(val value: String) {
    init {
        require(value.matches(VALID_ID)) { "Invalid temporary asset identifier" }
    }

    companion object {
        private val VALID_ID = Regex("[a-f0-9]{32}")
    }
}
