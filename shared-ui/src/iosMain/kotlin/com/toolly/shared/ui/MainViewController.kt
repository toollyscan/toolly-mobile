package com.toolly.shared.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.toolly.shared.auth.AuthError
import com.toolly.shared.auth.AuthResult
import com.toolly.shared.auth.PhoneVerificationId
import com.toolly.shared.auth.PhoneVerificationResult
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.model.BackupPreferenceKind
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState
import kotlinx.coroutines.launch
import platform.Foundation.NSUserDefaults

@Suppress("FunctionName")
fun MainViewController() = MainViewController(
    developmentAccessAvailable = false,
    captureSession = null,
    accountAuthenticatorSession = null,
)

@Suppress("FunctionName")
fun MainViewController(developmentAccessAvailable: Boolean) = MainViewController(
    developmentAccessAvailable = developmentAccessAvailable,
    captureSession = null,
    accountAuthenticatorSession = null,
)

@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
    captureSession: AppleCaptureSession?,
) = MainViewController(
    developmentAccessAvailable = developmentAccessAvailable,
    captureSession = captureSession,
    accountAuthenticatorSession = null,
)

/**
 * [captureSession] is the first-party Swift VisionKit implementation of [AppleCaptureSession]
 * (see `AppleCaptureBridge.kt`), and [accountAuthenticatorSession] is the first-party Swift
 * Firebase implementation of [AppleAccountAuthenticatorSession] (see `AppleAuthBridge.kt`), both
 * supplied by the host app. Both are nullable so existing/test callers that don't care about
 * capture or real authentication keep compiling unchanged -- with `null`, `scanDocument()` falls
 * back to its original library-navigation-only behavior, and the auth actions stay local-only/
 * mock (matching the pre-existing behavior before either adapter existed).
 */
@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
    captureSession: AppleCaptureSession?,
    accountAuthenticatorSession: AppleAccountAuthenticatorSession?,
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

    val authenticator = remember(accountAuthenticatorSession) {
        accountAuthenticatorSession?.let { AppleAccountAuthenticator(it) }
    }
    // Host-local, not reducer-shared -- see AndroidToollyApp.kt's identical field for why.
    var pendingPhoneVerificationId by remember { mutableStateOf<PhoneVerificationId?>(null) }

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
            override fun authenticate(method: ToollyAuthenticationMethod) =
                dispatch(ToollyUiEvent.AuthenticationMethodSelected(method))
            override fun useDevelopmentAccess() = dispatch(ToollyUiEvent.DevelopmentAccessGranted)
            override fun openHome() = select(ToollyDestination.HOME)
            override fun openLibrary() = select(ToollyDestination.LIBRARY)
            override fun openSearch() = select(ToollyDestination.SEARCH)
            override fun openProfile() = select(ToollyDestination.PROFILE)
            override fun signOut() {
                authenticator?.let { active -> coroutineScope.launch { active.signOut() } }
                dispatch(ToollyUiEvent.SignedOut)
            }

            /**
             * The Scan action only renders inside the authenticated main shell (Home/Library/
             * Search/Profile), which is itself unreachable while signed out (D-049) -- so this is
             * never invoked pre-authentication. When a real [captureSession] is wired, launches
             * it and, on a successful ordered-page result, enters the shared review screen with
             * the real page count.
             */
            override fun scanDocument() {
                if (state.busy) return
                select(ToollyDestination.LIBRARY)
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
            // When no real [authenticator] is wired (Swift host didn't supply a session), these
            // fall back to the original local-only/mock transitions -- same behavior as before
            // either adapter existed. With a real authenticator, mirrors AndroidToollyApp.kt's
            // wiring exactly, including the account-linking deferral for the phone-verification
            // step that follows creating an email/password account (ADR-0004 point 9).
            override fun submitPhoneNumber(phoneNumber: String) {
                val activeAuthenticator = authenticator
                if (activeAuthenticator == null || state.pendingEmail != null) {
                    dispatch(ToollyUiEvent.PhoneNumberSubmitted(phoneNumber))
                    return
                }
                dispatch(ToollyUiEvent.AuthenticationStarted)
                coroutineScope.launch {
                    when (val result = activeAuthenticator.sendPhoneVerificationCode(phoneNumber)) {
                        is PhoneVerificationResult.CodeSent -> {
                            pendingPhoneVerificationId = result.id
                            dispatch(ToollyUiEvent.PhoneNumberSubmitted(phoneNumber))
                        }
                        is PhoneVerificationResult.Failure ->
                            dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                    }
                }
            }

            override fun verifyOtp(code: String) {
                val activeAuthenticator = authenticator
                if (activeAuthenticator == null || state.pendingEmail != null) {
                    dispatch(ToollyUiEvent.OtpVerified)
                    return
                }
                val verificationId = pendingPhoneVerificationId
                if (verificationId == null) {
                    dispatch(ToollyUiEvent.AuthenticationFailed(AuthError.ExpiredCode))
                    return
                }
                dispatch(ToollyUiEvent.AuthenticationStarted)
                coroutineScope.launch {
                    when (
                        val result = activeAuthenticator.confirmPhoneVerificationCode(verificationId, code)
                    ) {
                        is AuthResult.Success -> {
                            pendingPhoneVerificationId = null
                            dispatch(ToollyUiEvent.OtpVerified)
                        }
                        is AuthResult.Failure -> dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                    }
                }
            }

            override fun completeAuthentication(email: String, password: String) {
                val activeAuthenticator = authenticator
                if (activeAuthenticator == null) {
                    dispatch(ToollyUiEvent.AuthenticationSucceeded)
                    return
                }
                dispatch(ToollyUiEvent.AuthenticationStarted)
                coroutineScope.launch {
                    when (val result = activeAuthenticator.signInWithEmail(email, password)) {
                        is AuthResult.Success -> dispatch(ToollyUiEvent.AuthenticationSucceeded)
                        is AuthResult.Failure -> dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                    }
                }
            }

            override fun selectCreateAccount() = dispatch(ToollyUiEvent.CreateAccountSelected)

            override fun createAccount(email: String, password: String) {
                val activeAuthenticator = authenticator
                if (activeAuthenticator == null) {
                    dispatch(ToollyUiEvent.AccountCreated(email))
                    return
                }
                dispatch(ToollyUiEvent.AuthenticationStarted)
                coroutineScope.launch {
                    when (val result = activeAuthenticator.createAccountWithEmail(email, password)) {
                        is AuthResult.Success -> dispatch(ToollyUiEvent.AccountCreated(email))
                        is AuthResult.Failure -> dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                    }
                }
            }

            override fun finishOnboarding() = dispatch(ToollyUiEvent.AuthenticationSucceeded)
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
