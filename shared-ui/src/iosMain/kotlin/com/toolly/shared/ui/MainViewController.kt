package com.toolly.shared.ui

import androidx.compose.ui.window.ComposeUIViewController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState

@Suppress("FunctionName")
fun MainViewController() = MainViewController(developmentAccessAvailable = false)

@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
) = ComposeUIViewController {
    var state by remember {
        mutableStateOf(
            ToollyUiState.firstLaunch(
                developmentAccessAvailable = developmentAccessAvailable,
            ),
        )
    }
    fun dispatch(event: ToollyUiEvent) {
        state = reduceToollyUiState(state, event)
    }
    ToollyApp(
        state = state,
        actions = object : ToollyUiActions {
            override fun finishSplash() = dispatch(ToollyUiEvent.SplashFinished)
            override fun completeTutorial() = dispatch(ToollyUiEvent.TutorialCompleted)
            override fun showSignIn() = dispatch(ToollyUiEvent.SignInSelected)
            override fun showCreateProfile() = dispatch(ToollyUiEvent.CreateProfileSelected)
            override fun backToWelcome() = dispatch(ToollyUiEvent.BackToWelcome)
            override fun authenticate(method: ToollyAuthenticationMethod) = Unit
            override fun useDevelopmentAccess() = dispatch(ToollyUiEvent.DevelopmentAccessGranted)
            override fun openHome() = select(ToollyDestination.HOME)
            override fun openLibrary() = select(ToollyDestination.LIBRARY)
            override fun openTools() = select(ToollyDestination.TOOLS)
            override fun openProfile() = select(ToollyDestination.PROFILE)
            override fun signOut() = dispatch(ToollyUiEvent.SignedOut)
            override fun scanDocument() = Unit
            override fun openDocument(id: DocumentUiId) = Unit
            override fun discardCapture() = Unit
            override fun saveCapture() = Unit
            override fun navigateBack() = Unit

            private fun select(destination: ToollyDestination) {
                dispatch(ToollyUiEvent.MainDestinationSelected(destination))
            }
        },
    )
}
