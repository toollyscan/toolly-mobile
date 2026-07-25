package com.toolly.spike.capture.export

import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri

/**
 * Builds read-only Android share intents for plaintext files the user explicitly exported.
 *
 * File URIs and empty payloads are rejected. Toolly never shares encrypted-vault or app-cache paths.
 */
internal object AndroidShareIntentFactory {
    fun create(
        documentUris: List<Uri>,
        mimeType: String,
    ): Intent {
        require(documentUris.isNotEmpty())
        require(mimeType.isNotBlank())
        require(documentUris.all { it.scheme == ContentResolver.SCHEME_CONTENT })

        val clipData = ClipData.newRawUri(null, documentUris.first()).also { clip ->
            documentUris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
        }
        return if (documentUris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, documentUris.single())
                this.clipData = clipData
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(documentUris))
                this.clipData = clipData
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
}
