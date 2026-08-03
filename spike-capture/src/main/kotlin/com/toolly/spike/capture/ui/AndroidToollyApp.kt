package com.toolly.spike.capture.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollySessionState
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState
import com.toolly.shared.ui.ToollyApp
import com.toolly.spike.capture.BuildConfig

@Composable
internal fun AndroidToollyApp(
    documentsContent: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    var state by remember {
        mutableStateOf(
            ToollyUiState.firstLaunch(
                tutorialCompleted = preferences.getBoolean(TUTORIAL_COMPLETED_KEY, false),
                developmentAccessAvailable = BuildConfig.DEBUG,
                appleSignInAvailable = false,
            ),
        )
    }
    fun dispatch(event: ToollyUiEvent) {
        state = reduceToollyUiState(state, event)
    }
    fun persistTutorialCompletion(event: ToollyUiEvent) {
        preferences.edit().putBoolean(TUTORIAL_COMPLETED_KEY, true).apply()
        dispatch(event)
    }

    ToollyApp(
        state = state,
        actions = object : ToollyUiActions {
            override fun finishSplash() = dispatch(ToollyUiEvent.SplashFinished)
            override fun nextTutorial() = dispatch(ToollyUiEvent.TutorialAdvanced)
            override fun skipTutorial() = persistTutorialCompletion(ToollyUiEvent.TutorialSkipped)
            override fun completeTutorial() = persistTutorialCompletion(ToollyUiEvent.TutorialCompleted)
            override fun showSignIn() = dispatch(ToollyUiEvent.SignInSelected)
            override fun showCreateProfile() = dispatch(ToollyUiEvent.CreateProfileSelected)
            override fun backToWelcome() = dispatch(ToollyUiEvent.BackToWelcome)
            override fun continueLocally(destination: ToollyDestination) {
                dispatch(ToollyUiEvent.LocalSessionStarted(destination))
            }
            override fun authenticate(method: ToollyAuthenticationMethod) = Unit
            override fun useDevelopmentAccess() = dispatch(ToollyUiEvent.DevelopmentAccessGranted)
            override fun openHome() = select(ToollyDestination.HOME)
            override fun openLibrary() = select(ToollyDestination.LIBRARY)
            override fun openSearch() = select(ToollyDestination.SEARCH)
            override fun openProfile() = select(ToollyDestination.PROFILE)
            override fun signOut() = dispatch(ToollyUiEvent.SignedOut)
            override fun scanDocument() {
                if (state.sessionState == ToollySessionState.SIGNED_OUT) {
                    dispatch(ToollyUiEvent.LocalSessionStarted(ToollyDestination.LIBRARY))
                } else {
                    select(ToollyDestination.LIBRARY)
                }
            }
            override fun openDocument(id: DocumentUiId) = Unit
            override fun discardCapture() = Unit
            override fun saveCapture() = Unit
            override fun navigateBack() = Unit

            private fun select(destination: ToollyDestination) {
                dispatch(ToollyUiEvent.MainDestinationSelected(destination))
            }
        },
        documentsContent = documentsContent,
    )
}

private const val PREFERENCES_NAME = "toolly_ui_preferences"
private const val TUTORIAL_COMPLETED_KEY = "tutorial_completed"
