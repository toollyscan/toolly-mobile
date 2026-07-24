package com.toolly.shared.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiState

@Suppress("FunctionName")
fun MainViewController() = ComposeUIViewController {
    ToollyApp(
        state = ToollyUiState.empty(),
        actions = NoOpToollyUiActions,
    )
}

private object NoOpToollyUiActions : ToollyUiActions {
    override fun scanDocument() = Unit
    override fun openDocument(id: DocumentUiId) = Unit
    override fun discardCapture() = Unit
    override fun saveCapture() = Unit
    override fun navigateBack() = Unit
}
