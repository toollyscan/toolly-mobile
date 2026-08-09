package com.toolly.shared.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.model.BackupPreferenceKind
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollySessionState
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState
import kotlinx.coroutines.launch
import platform.Foundation.NSUserDefaults

@Suppress("FunctionName")
fun MainViewController() = MainViewController(developmentAccessAvailable = false, captureSession = null)

@Suppress("FunctionName")
fun MainViewController(developmentAccessAvailable: Boolean) =
    MainViewController(developmentAccessAvailable = developmentAccessAvailable, captureSession = null)

/**
 * [captureSession] is the first-party Swift VisionKit implementation of [AppleCaptureSession]
 * (see `AppleCaptureBridge.kt`), supplied by the host app. It is nullable so existing/test
 * callers that don't care about capture keep compiling unchanged -- with `null`, `scanDocument()`
 * falls back to its original library-navigation-only behavior.
 */
@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
    captureSession: AppleCaptureSession?,
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
    val scanner = remember(captureSession) { captureSession?.let { AppleDocumentScanner(it) } }
    var capturedPages by remember { mutableStateOf<List<ScannedPage>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

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

            /**
             * Ensures a local session same as before, then -- when a real [captureSession] is
             * wired -- launches it and, on a successful ordered-page result, enters the shared
             * review screen with the real page count. With no session wired (`scanner == null`,
             * e.g. simulator builds without a host-supplied session), this is unchanged from the
             * library-navigation-only behavior that shipped before Apple capture existed.
             */
            override fun scanDocument() {
                if (state.busy) return
                if (state.sessionState == ToollySessionState.SIGNED_OUT) {
                    dispatch(ToollyUiEvent.LocalSessionStarted(ToollyDestination.LIBRARY))
                } else {
                    select(ToollyDestination.LIBRARY)
                }
                val activeScanner = scanner ?: return
                state = state.copy(busy = true)
                coroutineScope.launch {
                    when (val result = activeScanner.launch(ScanConfig())) {
                        is ScanResult.Success -> {
                            capturedPages = result.pages
                            state = state.copy(busy = false)
                            dispatch(ToollyUiEvent.CaptureReviewStarted(result.pages.size))
                        }
                        // Cancellation and failure both just return to where the user already is;
                        // there is no error-message slot in shared state for the iOS host yet.
                        ScanResult.Cancelled, is ScanResult.Failure -> {
                            state = state.copy(busy = false)
                        }
                    }
                }
            }

            override fun openDocument(id: DocumentUiId) = Unit

            override fun discardCapture() {
                scanner?.release(capturedPages)
                capturedPages = emptyList()
                dispatch(ToollyUiEvent.CaptureDiscarded)
            }

            // No iOS equivalent of EncryptedDocumentRepository exists yet, so there is nowhere to
            // persist a capture to. Deliberately left unimplemented rather than faking a save --
            // see issue #48 for scope and the follow-up this needs (a real iOS vault).
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
