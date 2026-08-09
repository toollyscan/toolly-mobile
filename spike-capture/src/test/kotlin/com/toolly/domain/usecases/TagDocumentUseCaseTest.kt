package com.toolly.domain.usecases

import com.toolly.domain.contracts.DocumentRepository
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentCategory
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentLifecycle
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.PageId
import com.toolly.foundation.ToollyClock
import com.toolly.foundation.ToollyResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagDocumentUseCaseTest {
    @Test
    fun `forwards the chosen category and the clock's time`() = runTest {
        val repository = RecordingRepository()
        val useCase = TagDocumentUseCase(repository, clock = ToollyClock { 55L })

        useCase(DocumentId(SAMPLE_ID), DocumentCategory.RECEIPT)

        assertEquals(DocumentCategory.RECEIPT, repository.lastCategory)
        assertEquals(55L, repository.lastUpdatedAt)
    }

    @Test
    fun `null clears the category`() = runTest {
        val repository = RecordingRepository()
        val useCase = TagDocumentUseCase(repository, clock = ToollyClock { 1L })

        useCase(DocumentId(SAMPLE_ID), null)

        assertNull(repository.lastCategory)
    }

    private class RecordingRepository : DocumentRepository {
        var lastCategory: DocumentCategory? = null
        var lastUpdatedAt: Long? = null

        override suspend fun listDocuments(): ToollyResult<List<DocumentSummary>> =
            ToollyResult.Success(emptyList())

        override suspend fun getDocument(documentId: DocumentId): ToollyResult<DocumentDetails> =
            error("Not used")

        override suspend fun saveCapturedDocument(
            command: com.toolly.domain.contracts.SaveCapturedDocumentCommand,
        ): ToollyResult<DocumentDetails> = error("Not used")

        override suspend fun renameDocument(
            documentId: DocumentId,
            displayName: String?,
            updatedAtEpochMillis: Long,
        ): ToollyResult<DocumentDetails> = error("Not used")

        override suspend fun tagDocument(
            documentId: DocumentId,
            category: DocumentCategory?,
            updatedAtEpochMillis: Long,
        ): ToollyResult<DocumentDetails> {
            lastCategory = category
            lastUpdatedAt = updatedAtEpochMillis
            return ToollyResult.Success(
                DocumentDetails(
                    summary = DocumentSummary(
                        id = documentId,
                        pageCount = 1,
                        createdAtEpochMillis = 0L,
                        updatedAtEpochMillis = updatedAtEpochMillis,
                        lifecycle = DocumentLifecycle.ACTIVE,
                        category = category,
                    ),
                    pages = listOf(
                        DocumentPage(
                            id = PageId(SAMPLE_PAGE_ID),
                            sourceAssetId = AssetId(SAMPLE_ASSET_ID),
                            ordinal = 0,
                            widthPixels = null,
                            heightPixels = null,
                        ),
                    ),
                ),
            )
        }
    }

    private companion object {
        const val SAMPLE_ID = "00000000-0000-4000-8000-000000000004"
        const val SAMPLE_PAGE_ID = "00000000-0000-4000-8000-000000000005"
        const val SAMPLE_ASSET_ID = "00000000-0000-4000-8000-000000000006"
    }
}
