package com.toolly.domain.usecases

import com.toolly.domain.contracts.DocumentRepository
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.model.AssetId
import com.toolly.domain.model.DocumentCategory
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentLifecycle
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.PageId
import com.toolly.foundation.ToollyClock
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RenameDocumentUseCaseTest {
    @Test
    fun trimsTheNameAndStampsTheClocksTimeBeforeWriting() = runImmediate {
        val repository = RecordingRepository()
        val useCase = RenameDocumentUseCase(repository, clock = ToollyClock { 99L })

        val result = useCase(DocumentId(SAMPLE_ID), "  Electricity bill  ")

        assertTrue(result is ToollyResult.Success)
        assertEquals("Electricity bill", repository.lastDisplayName)
        assertEquals(99L, repository.lastUpdatedAt)
    }

    @Test
    fun blankNameClearsTheTitleInsteadOfStoringWhitespace() = runImmediate {
        val repository = RecordingRepository()
        val useCase = RenameDocumentUseCase(repository, clock = ToollyClock { 1L })

        useCase(DocumentId(SAMPLE_ID), "   ")

        assertNull(repository.lastDisplayName)
        assertTrue(repository.renameCalled)
    }

    @Test
    fun nullClearsTheTitle() = runImmediate {
        val repository = RecordingRepository()
        val useCase = RenameDocumentUseCase(repository, clock = ToollyClock { 1L })

        useCase(DocumentId(SAMPLE_ID), null)

        assertNull(repository.lastDisplayName)
        assertTrue(repository.renameCalled)
    }

    @Test
    fun rejectsANameOverTheSupportedLengthWithoutCallingTheRepository() = runImmediate {
        val repository = RecordingRepository()
        val useCase = RenameDocumentUseCase(repository, clock = ToollyClock { 1L })

        val result = useCase(DocumentId(SAMPLE_ID), "x".repeat(121))

        assertTrue(result is ToollyResult.Failure)
        assertEquals(ToollyErrorCode.VALIDATION, (result as ToollyResult.Failure).error.code)
        assertTrue(!repository.renameCalled)
    }

    private class RecordingRepository : DocumentRepository {
        var renameCalled = false
        var lastDisplayName: String? = null
        var lastUpdatedAt: Long? = null

        override suspend fun listDocuments(): ToollyResult<List<DocumentSummary>> =
            ToollyResult.Success(emptyList())

        override suspend fun getDocument(documentId: DocumentId): ToollyResult<DocumentDetails> =
            error("Not used")

        override suspend fun saveCapturedDocument(
            command: SaveCapturedDocumentCommand,
        ): ToollyResult<DocumentDetails> = error("Not used")

        override suspend fun renameDocument(
            documentId: DocumentId,
            displayName: String?,
            updatedAtEpochMillis: Long,
        ): ToollyResult<DocumentDetails> {
            renameCalled = true
            lastDisplayName = displayName
            lastUpdatedAt = updatedAtEpochMillis
            return ToollyResult.Success(
                DocumentDetails(
                    summary = DocumentSummary(
                        id = documentId,
                        pageCount = 1,
                        createdAtEpochMillis = 0L,
                        updatedAtEpochMillis = updatedAtEpochMillis,
                        lifecycle = DocumentLifecycle.ACTIVE,
                        displayName = displayName,
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

        override suspend fun tagDocument(
            documentId: DocumentId,
            category: DocumentCategory?,
            updatedAtEpochMillis: Long,
        ): ToollyResult<DocumentDetails> = error("Not used")
    }

    private companion object {
        const val SAMPLE_ID = "00000000-0000-4000-8000-000000000001"
        const val SAMPLE_PAGE_ID = "00000000-0000-4000-8000-000000000002"
        const val SAMPLE_ASSET_ID = "00000000-0000-4000-8000-000000000003"
    }
}
