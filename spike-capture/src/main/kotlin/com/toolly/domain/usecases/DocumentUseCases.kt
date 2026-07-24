package com.toolly.domain.usecases

import com.toolly.domain.contracts.DocumentRepository
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.contracts.SaveCapturedPage
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.CapturedPageDraft
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.MAX_DOCUMENT_PAGES
import com.toolly.domain.model.OperationId
import com.toolly.domain.model.PageId
import com.toolly.foundation.OpaqueIdGenerator
import com.toolly.foundation.ToollyClock
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult

class SaveCapturedDocumentUseCase(
    private val repository: DocumentRepository,
    private val clock: ToollyClock,
    private val idGenerator: OpaqueIdGenerator,
) {
    suspend operator fun invoke(pages: List<CapturedPageDraft>): ToollyResult<DocumentDetails> {
        val validationError = validate(pages)
        if (validationError != null) {
            return ToollyResult.Failure(
                ToollyError(ToollyErrorCode.VALIDATION, validationError),
            )
        }

        val now = clock.nowEpochMillis()
        val command = SaveCapturedDocumentCommand(
            operationId = OperationId(idGenerator.newId()),
            documentId = DocumentId(idGenerator.newId()),
            createdAtEpochMillis = now,
            pages = pages.map { page ->
                SaveCapturedPage(
                    pageId = PageId(idGenerator.newId()),
                    assetId = AssetId(idGenerator.newId()),
                    temporaryAssetId = page.temporaryAssetId,
                    ordinal = page.ordinal,
                    widthPixels = page.widthPixels,
                    heightPixels = page.heightPixels,
                )
            },
        )
        return repository.saveCapturedDocument(command)
    }

    private fun validate(pages: List<CapturedPageDraft>): String? = when {
        pages.isEmpty() -> "At least one captured page is required"
        pages.size > MAX_DOCUMENT_PAGES -> "Document exceeds the supported page limit"
        pages.map { it.ordinal } != pages.indices.toList() ->
            "Captured page order must be contiguous"
        pages.map { it.temporaryAssetId }.distinct().size != pages.size ->
            "Captured pages must reference unique temporary assets"
        else -> null
    }
}

class ListDocumentsUseCase(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(): ToollyResult<List<DocumentSummary>> =
        repository.listDocuments()
}

class OpenDocumentUseCase(
    private val repository: DocumentRepository,
) {
    suspend operator fun invoke(documentId: DocumentId): ToollyResult<DocumentDetails> =
        repository.getDocument(documentId)
}
