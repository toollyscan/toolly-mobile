package com.toolly.domain.model

const val DOCUMENT_SCHEMA_VERSION: Int = 1
const val MAX_DOCUMENT_PAGES: Int = 20
const val MAX_DISPLAY_NAME_LENGTH: Int = 120

enum class DocumentLifecycle {
    ACTIVE,
    DELETION_PENDING,
    CORRUPT,
    UNKNOWN,
}

/**
 * User-assigned grouping for a document. Deliberately small and closed (no free-form tags) so
 * Library's filter chips stay a fixed, predictable set rather than an open-ended taxonomy.
 */
enum class DocumentCategory {
    RECEIPT,
    IDENTIFICATION,
    OTHER,
}

enum class AssetKind {
    SOURCE_IMAGE,
    PROCESSED_IMAGE,
    PDF,
    THUMBNAIL,
    UNKNOWN,
}

data class DocumentSummary(
    val id: DocumentId,
    val pageCount: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lifecycle: DocumentLifecycle,
    val displayName: String? = null,
    val category: DocumentCategory? = null,
    val schemaVersion: Int = DOCUMENT_SCHEMA_VERSION,
) {
    init {
        require(pageCount in 1..MAX_DOCUMENT_PAGES)
        require(createdAtEpochMillis >= 0)
        require(updatedAtEpochMillis >= createdAtEpochMillis)
        require(schemaVersion > 0)
        require(displayName == null || displayName.isNotBlank())
        require(displayName == null || displayName.length <= MAX_DISPLAY_NAME_LENGTH)
    }
}

data class DocumentPage(
    val id: PageId,
    val sourceAssetId: AssetId,
    val ordinal: Int,
    val widthPixels: Int?,
    val heightPixels: Int?,
) {
    init {
        require(ordinal >= 0)
        require(widthPixels == null || widthPixels > 0)
        require(heightPixels == null || heightPixels > 0)
    }
}

data class DocumentDetails(
    val summary: DocumentSummary,
    val pages: List<DocumentPage>,
) {
    init {
        require(pages.isNotEmpty())
        require(pages.size == summary.pageCount)
        require(pages.map { it.ordinal } == pages.indices.toList())
        require(pages.map { it.id }.distinct().size == pages.size)
        require(pages.map { it.sourceAssetId }.distinct().size == pages.size)
    }
}

data class CapturedPageDraft(
    val temporaryAssetId: TemporaryAssetId,
    val ordinal: Int,
    val widthPixels: Int?,
    val heightPixels: Int?,
) {
    init {
        require(ordinal >= 0)
        require(widthPixels == null || widthPixels > 0)
        require(heightPixels == null || heightPixels > 0)
    }
}
