package com.toolly.shared.ui

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
import com.toolly.foundation.ToollyError
import com.toolly.foundation.ToollyErrorCode
import com.toolly.foundation.ToollyResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Objective-C-compatible boundary implemented by the first-party Swift host's local document
 * store (TLY-014 Phase 2, #82). Mirrors [AppleCaptureSession]/[AppleAccountAuthenticatorSession]'s
 * shape exactly: callback-based rather than suspend, and only primitives/flat DTOs cross the
 * boundary -- never shared-core's canonical [DocumentSummary]/[DocumentDetails]/[DocumentPage] or
 * their value-class IDs directly (ADR-0012: "shared code owns the port... platform code owns the
 * objects"). The flat DTOs below exist only because a document has enough fields that flat
 * per-callback parameters (the auth bridge's style) would be unreadable; they carry primitives
 * only, so they cross the same way a `List<String>` already does elsewhere in this file.
 *
 * Phase 2 has no cryptography behind it yet (see #82) -- the Swift implementation of this
 * interface is expected to be a plain, unencrypted local store for now, matching the "prove the
 * port boundary and plumbing compile end to end first" scope. It is not production-approved.
 */
interface AppleDocumentVaultSession {
    fun listDocuments(callback: AppleDocumentListCallback)

    fun getDocument(documentId: String, callback: AppleDocumentCallback)

    fun saveCapturedDocument(
        operationId: String,
        documentId: String,
        createdAtEpochMillis: Long,
        pages: List<AppleCapturedPageInput>,
        callback: AppleDocumentCallback,
    )

    fun renameDocument(
        documentId: String,
        displayName: String?,
        updatedAtEpochMillis: Long,
        callback: AppleDocumentCallback,
    )

    fun tagDocument(
        documentId: String,
        category: String?,
        updatedAtEpochMillis: Long,
        callback: AppleDocumentCallback,
    )
}

/** Terminal callback for [AppleDocumentVaultSession.listDocuments]. */
interface AppleDocumentListCallback {
    fun onSuccess(documents: List<AppleDocumentSummaryDto>)
    fun onFailure(errorCode: String)
}

/** Terminal callback shared by every single-document operation. */
interface AppleDocumentCallback {
    fun onSuccess(
        documentId: String,
        pageCount: Int,
        createdAtEpochMillis: Long,
        updatedAtEpochMillis: Long,
        lifecycle: String,
        displayName: String?,
        category: String?,
        pages: List<AppleDocumentPageDto>,
    )
    fun onFailure(errorCode: String)
}

/** Flat DTO for one library row -- see [AppleDocumentVaultSession]'s doc for why this exists. */
data class AppleDocumentSummaryDto(
    val documentId: String,
    val pageCount: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val lifecycle: String,
    val displayName: String?,
    val category: String?,
)

/** Flat DTO for one already-committed page. */
data class AppleDocumentPageDto(
    val pageId: String,
    val assetId: String,
    val ordinal: Int,
    val widthPixels: Int?,
    val heightPixels: Int?,
)

/**
 * One page of a capture about to be saved. IDs are already minted (by
 * [com.toolly.domain.usecases.SaveCapturedDocumentUseCase] on the Kotlin side) by the time this
 * crosses to Swift -- the session only persists what it's given, it never generates identifiers.
 */
data class AppleCapturedPageInput(
    val pageId: String,
    val assetId: String,
    val temporaryAssetId: String,
    val ordinal: Int,
    val widthPixels: Int?,
    val heightPixels: Int?,
)

/**
 * Provider-neutral [DocumentRepository] adapter around the Swift-owned local document store.
 * Converts every canonical Toolly type at the boundary and never lets a malformed DTO from Swift
 * throw past this class -- an unparseable id/enum/page degrades to [ToollyErrorCode.CORRUPT]
 * rather than crashing, matching [AppleDocumentScanner]'s `toValidatedPages` defensiveness.
 */
internal class AppleDocumentRepository(
    private val session: AppleDocumentVaultSession,
) : DocumentRepository {

    override suspend fun listDocuments(): ToollyResult<List<DocumentSummary>> =
        suspendCoroutine { continuation ->
            session.listDocuments(
                object : AppleDocumentListCallback {
                    override fun onSuccess(documents: List<AppleDocumentSummaryDto>) {
                        continuation.resume(
                            runCatching { documents.map { it.toDomain() } }
                                .fold(
                                    onSuccess = { ToollyResult.Success(it) },
                                    onFailure = { corruptFailure() },
                                ),
                        )
                    }

                    override fun onFailure(errorCode: String) {
                        continuation.resume(ToollyResult.Failure(errorCode.toToollyError()))
                    }
                },
            )
        }

    override suspend fun getDocument(documentId: DocumentId): ToollyResult<DocumentDetails> =
        awaitDocument { callback -> session.getDocument(documentId.value, callback) }

    override suspend fun saveCapturedDocument(
        command: SaveCapturedDocumentCommand,
    ): ToollyResult<DocumentDetails> = awaitDocument { callback ->
        session.saveCapturedDocument(
            operationId = command.operationId.value,
            documentId = command.documentId.value,
            createdAtEpochMillis = command.createdAtEpochMillis,
            pages = command.pages.map { page ->
                AppleCapturedPageInput(
                    pageId = page.pageId.value,
                    assetId = page.assetId.value,
                    temporaryAssetId = page.temporaryAssetId.value,
                    ordinal = page.ordinal,
                    widthPixels = page.widthPixels,
                    heightPixels = page.heightPixels,
                )
            },
            callback = callback,
        )
    }

    override suspend fun renameDocument(
        documentId: DocumentId,
        displayName: String?,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails> = awaitDocument { callback ->
        session.renameDocument(documentId.value, displayName, updatedAtEpochMillis, callback)
    }

    override suspend fun tagDocument(
        documentId: DocumentId,
        category: DocumentCategory?,
        updatedAtEpochMillis: Long,
    ): ToollyResult<DocumentDetails> = awaitDocument { callback ->
        session.tagDocument(documentId.value, category?.toWireValue(), updatedAtEpochMillis, callback)
    }

    private suspend fun awaitDocument(
        launch: (AppleDocumentCallback) -> Unit,
    ): ToollyResult<DocumentDetails> = suspendCoroutine { continuation ->
        launch(
            object : AppleDocumentCallback {
                override fun onSuccess(
                    documentId: String,
                    pageCount: Int,
                    createdAtEpochMillis: Long,
                    updatedAtEpochMillis: Long,
                    lifecycle: String,
                    displayName: String?,
                    category: String?,
                    pages: List<AppleDocumentPageDto>,
                ) {
                    val result = runCatching {
                        DocumentDetails(
                            summary = DocumentSummary(
                                id = DocumentId(documentId),
                                pageCount = pageCount,
                                createdAtEpochMillis = createdAtEpochMillis,
                                updatedAtEpochMillis = updatedAtEpochMillis,
                                lifecycle = lifecycle.toDocumentLifecycle(),
                                displayName = displayName,
                                category = category?.toDocumentCategory(),
                            ),
                            pages = pages.map { it.toDomain() },
                        )
                    }.fold(
                        onSuccess = { ToollyResult.Success(it) },
                        onFailure = { corruptFailure() },
                    )
                    continuation.resume(result)
                }

                override fun onFailure(errorCode: String) {
                    continuation.resume(ToollyResult.Failure(errorCode.toToollyError()))
                }
            },
        )
    }

    private fun <T> corruptFailure(): ToollyResult<T> = ToollyResult.Failure(
        ToollyError(ToollyErrorCode.CORRUPT, ToollyErrorCode.CORRUPT.name),
    )
}

private fun AppleDocumentSummaryDto.toDomain(): DocumentSummary = DocumentSummary(
    id = DocumentId(documentId),
    pageCount = pageCount,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
    lifecycle = lifecycle.toDocumentLifecycle(),
    displayName = displayName,
    category = category?.toDocumentCategory(),
)

private fun AppleDocumentPageDto.toDomain(): DocumentPage = DocumentPage(
    id = PageId(pageId),
    sourceAssetId = AssetId(assetId),
    ordinal = ordinal,
    widthPixels = widthPixels,
    heightPixels = heightPixels,
)

private fun String.toDocumentLifecycle(): DocumentLifecycle =
    DocumentLifecycle.entries.firstOrNull { it.name == this } ?: DocumentLifecycle.UNKNOWN

private fun String.toDocumentCategory(): DocumentCategory? =
    DocumentCategory.entries.firstOrNull { it.name == this }

private fun DocumentCategory.toWireValue(): String = name

/**
 * Allowlisted, lowercase-snake-case error codes the Swift session reports failures with, matching
 * [AppleAccountAuthenticatorSession]'s `toAuthError()` convention exactly (not [ToollyErrorCode]'s
 * own enum `.name`, which is uppercase) so both bridges read the same way from the Swift side. An
 * unrecognized code degrades to [ToollyErrorCode.UNKNOWN] rather than throwing.
 */
private fun String.toToollyError(): ToollyError {
    val code = when (this) {
        "validation" -> ToollyErrorCode.VALIDATION
        "unavailable" -> ToollyErrorCode.UNAVAILABLE
        "unauthorized" -> ToollyErrorCode.UNAUTHORIZED
        "conflict" -> ToollyErrorCode.CONFLICT
        "quota" -> ToollyErrorCode.QUOTA
        "corrupt" -> ToollyErrorCode.CORRUPT
        "retryable" -> ToollyErrorCode.RETRYABLE
        "permanent" -> ToollyErrorCode.PERMANENT
        else -> ToollyErrorCode.UNKNOWN
    }
    return ToollyError(code, code.name)
}
