package com.toolly.shared.model

import kotlin.jvm.JvmInline

@JvmInline
value class DocumentUiId(val value: String)

data class DocumentListItem(
    val id: DocumentUiId,
    val pageCount: Int,
)

enum class ToollyDestination {
    LIBRARY,
    CAPTURE_REVIEW,
    DOCUMENT_VIEWER,
}

data class ToollyUiState(
    val destination: ToollyDestination,
    val documents: List<DocumentListItem>,
    val selectedDocumentId: DocumentUiId?,
    val reviewPageCount: Int,
    val busy: Boolean,
) {
    init {
        require(reviewPageCount >= 0)
        require(documents.all { it.pageCount > 0 })
    }

    companion object {
        fun empty(): ToollyUiState = ToollyUiState(
            destination = ToollyDestination.LIBRARY,
            documents = emptyList(),
            selectedDocumentId = null,
            reviewPageCount = 0,
            busy = false,
        )
    }
}

interface ToollyUiActions {
    fun scanDocument()
    fun openDocument(id: DocumentUiId)
    fun discardCapture()
    fun saveCapture()
    fun navigateBack()
}
