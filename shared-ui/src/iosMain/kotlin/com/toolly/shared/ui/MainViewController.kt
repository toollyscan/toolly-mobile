package com.toolly.shared.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.toolly.shared.model.BackupPreferenceKind
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollySessionState
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState
import platform.Foundation.NSUserDefaults

@Suppress("FunctionName")
fun MainViewController() = MainViewController(developmentAccessAvailable = false)

@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
) = ComposeUIViewController {
    val preferences = remember { NSUserDefaults.standardUserDefaults }
    var state by remember {
        mutableStateOf(
            ToollyUiState.firstLaunch(
                tutorialCompleted = preferences.boolForKey(TUTORIAL_COMPLETED_KEY),
                developmentAccessAvailable = developmentAccessAvailable,
                appleSignInAvailable = true,
            ),
        )
    }
    fun dispatch(event: ToollyUiEvent) {
        state = reduceToollyUiState(state, event)
    }
    fun persistTutorialCompletion(event: ToollyUiEvent) {
        preferences.setBool(true, forKey = TUTORIAL_COMPLETED_KEY)
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
            override fun authenticate(method: ToollyAuthenticationMethod) =
                dispatch(ToollyUiEvent.AuthenticationMethodSelected(method))
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
            override fun navigateBack() = dispatch(ToollyUiEvent.NavigateBack)
            override fun submitPhoneNumber(phoneNumber: String) =
                dispatch(ToollyUiEvent.PhoneNumberSubmitted(phoneNumber))
            override fun verifyOtp() = dispatch(ToollyUiEvent.OtpVerified)
            override fun completeAuthentication() = dispatch(ToollyUiEvent.AuthenticationSucceeded)
            override fun selectCreateAccount() = dispatch(ToollyUiEvent.CreateAccountSelected)
            override fun createAccount(email: String) = dispatch(ToollyUiEvent.AccountCreated(email))
            override fun selectForgotPassword() = dispatch(ToollyUiEvent.ForgotPasswordSelected)
            override fun completeProfile() = dispatch(ToollyUiEvent.ProfileCompleted)
            override fun authStepBack() = dispatch(ToollyUiEvent.AuthStepBackRequested)
            override fun openPrivacyCenter() = dispatch(ToollyUiEvent.PrivacyCenterOpened)
            override fun openBackupSettings() = dispatch(ToollyUiEvent.BackupSettingsOpened)
            override fun setBackupPreference(kind: BackupPreferenceKind, enabled: Boolean) =
                dispatch(ToollyUiEvent.BackupPreferenceToggled(kind, enabled))
            override fun setBackupEnabled(enabled: Boolean) =
                dispatch(ToollyUiEvent.BackupEnabledChanged(enabled))

            private fun select(destination: ToollyDestination) {
                dispatch(ToollyUiEvent.MainDestinationSelected(destination))
            }
        },
    )
}

private const val TUTORIAL_COMPLETED_KEY = "toolly.tutorial.completed"
