package com.toolly.domain.usecases

import com.toolly.domain.contracts.DocumentRepository
import com.toolly.domain.contracts.SaveCapturedDocumentCommand
import com.toolly.domain.model.CapturedPageDraft
import com.toolly.domain.model.DocumentDetails
import com.toolly.domain.model.DocumentId
import com.toolly.domain.model.DocumentLifecycle
import com.toolly.domain.model.DocumentPage
import com.toolly.domain.model.DocumentSummary
import com.toolly.domain.model.TemporaryAssetId
import com.toolly.foundation.OpaqueIdGenerator
import com.toolly.foundation.ToollyClock
import com.toolly.foundation.ToollyResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveCapturedDocumentUseCaseTest {
    @Test
    fun `builds stable canonical command and preserves page order`() = runTest {
        val repository = RecordingRepository()
        val ids = sequenceOf(
            "00000000-0000-4000-8000-000000000001",
            "00000000-0000-4000-8000-000000000002",
            "00000000-0000-4000-8000-000000000003",
            "00000000-0000-4000-8000-000000000004",
            "00000000-0000-4000-8000-000000000005",
            "00000000-0000-4000-8000-000000000006",
        ).iterator()
        val useCase = SaveCapturedDocumentUseCase(
            repository = repository,
            clock = ToollyClock { 42L },
            idGenerator = OpaqueIdGenerator { ids.next() },
        )

        val result = useCase(
            listOf(
                CapturedPageDraft(TemporaryAssetId("temp-1"), 0, 100, 200),
                CapturedPageDraft(TemporaryAssetId("temp-2"), 1, 300, 400),
            ),
        )

        assertTrue(result is ToollyResult.Success)
        assertEquals(listOf(0, 1), repository.command!!.pages.map { it.ordinal })
        assertEquals(42L, repository.command!!.createdAtEpochMillis)
    }

    @Test
    fun `rejects duplicate temporary assets before repository write`() = runTest {
        val repository = RecordingRepository()
        val useCase = SaveCapturedDocumentUseCase(
            repository = repository,
            clock = ToollyClock { 42L },
            idGenerator = OpaqueIdGenerator { error("IDs must not be generated") },
        )

        val result = useCase(
            listOf(
                CapturedPageDraft(TemporaryAssetId("same"), 0, null, null),
                CapturedPageDraft(TemporaryAssetId("same"), 1, null, null),
            ),
        )

        assertTrue(result is ToollyResult.Failure)
        assertEquals(null, repository.command)
    }

    private class RecordingRepository : DocumentRepository {
        var command: SaveCapturedDocumentCommand? = null

        override suspend fun listDocuments(): ToollyResult<List<DocumentSummary>> =
            ToollyResult.Success(emptyList())

        override suspend fun getDocument(documentId: DocumentId): ToollyResult<DocumentDetails> =
            error("Not used")

        override suspend fun saveCapturedDocument(
            command: SaveCapturedDocumentCommand,
        ): ToollyResult<DocumentDetails> {
            this.command = command
            val pages = command.pages.map {
                DocumentPage(
                    id = it.pageId,
                    sourceAssetId = it.assetId,
                    ordinal = it.ordinal,
                    widthPixels = it.widthPixels,
                    heightPixels = it.heightPixels,
                )
            }
            return ToollyResult.Success(
                DocumentDetails(
                    summary = DocumentSummary(
                        id = command.documentId,
                        pageCount = pages.size,
                        createdAtEpochMillis = command.createdAtEpochMillis,
                        updatedAtEpochMillis = command.createdAtEpochMillis,
                        lifecycle = DocumentLifecycle.ACTIVE,
                    ),
                    pages = pages,
                ),
            )
        }
    }
}
