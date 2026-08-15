package com.toolly.domain.contracts

import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentCategory
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.OperationId
import com.toolly.domain.model.PageId
import com.toolly.domain.model.TemporaryAssetId
import com.toolly.foundation.ToollyResult

/**
 * Source-of-truth boundary for local Toolly documents.
 *
 * Implementations own staging, publication, recovery and encryption. Callers never receive
 * database rows, filesystem paths, provider tasks or Android types.
 */
interface DocumentRepository {
    suspend fun listDocuments(): ToollyResult<List<DocumentSummary>>

    suspend fun getDocument(documentId: DocumentId): ToollyResult<DocumentDetails>

    suspend fun saveCapturedDocument(command: SaveCapturedDocumentCommand): ToollyResult<DocumentDetails>

    /** Sets or clears (`displayName = null`) the document's user-facing title. */
    suspend fun renameDocument(
        documentId: DocumentId,
        displayName: String?,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails>

    /** Sets or clears (`category = null`) the document's Library filter category. */
    suspend fun tagDocument(
        documentId: DocumentId,
        category: DocumentCategory?,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails>
}

data class SaveCapturedDocumentCommand(
    val operationId: OperationId,
    val documentId: DocumentId,
    val createdAtEpochMillis: Long,
    val pages: List<SaveCapturedPage>,
)

data class SaveCapturedPage(
    val pageId: PageId,
    val assetId: AssetId,
    val temporaryAssetId: TemporaryAssetId,
    val ordinal: Int,
    val widthPixels: Int?,
    val heightPixels: Int?,
)
