package com.toolly.spike.capture.ui

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.toolly.shared.auth.AuthError
import com.toolly.shared.auth.AuthResult
import com.toolly.shared.auth.PhoneVerificationId
import com.toolly.shared.auth.PhoneVerificationResult
import com.toolly.shared.model.BackupPreferenceKind
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState
import com.toolly.shared.ui.ToollyApp
import com.toolly.spike.capture.BuildConfig
import com.toolly.spike.capture.firebase.FirebaseAccountAuthenticator
import com.toolly.spike.capture.google.GoogleIdTokenProvider
import kotlinx.coroutines.launch

@Composable
internal fun AndroidToollyApp(
    documentsContent: @Composable () -> Unit,
    searchContent: @Composable () -> Unit,
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
    var captureRequested by remember { mutableStateOf(false) }

    // Real ADR-0004 authentication, bound to toollyscan-dev. Constructed once per composition;
    // FirebaseAccountAuthenticator itself needs an Activity for phone-auth's reCAPTCHA fallback.
    val authenticator = remember { FirebaseAccountAuthenticator(context as Activity) }
    val googleIdTokenProvider = remember { GoogleIdTokenProvider(context as Activity) }
    val coroutineScope = rememberCoroutineScope()

    // Host-local, not reducer-shared: the in-flight phone verification handed back by
    // sendPhoneVerificationCode, needed by verifyOtp. Not UI state -- an adapter coordination
    // detail, matching how captureRequested/LocalCaptureLaunchRequest already work below.
    var pendingPhoneVerificationId by remember { mutableStateOf<PhoneVerificationId?>(null) }

    fun dispatch(event: ToollyUiEvent) {
        state = reduceToollyUiState(state, event)
    }
    fun persistTutorialCompletion(event: ToollyUiEvent) {
        preferences.edit().putBoolean(TUTORIAL_COMPLETED_KEY, true).apply()
        dispatch(event)
    }

    CompositionLocalProvider(
        LocalCaptureLaunchRequest provides CaptureLaunchRequest(
            requested = captureRequested,
            consume = { captureRequested = false },
        ),
    ) {
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
                override fun authenticate(method: ToollyAuthenticationMethod) {
                    if (method != ToollyAuthenticationMethod.GOOGLE) {
                        dispatch(ToollyUiEvent.AuthenticationMethodSelected(method))
                        return
                    }
                    // Google's own consent UI (Credential Manager) is a separate platform concern
                    // from exchanging the resulting ID token for a Firebase session -- see
                    // AccountAuthenticator.signInWithGoogle's doc comment. This doesn't need the
                    // AuthenticationMethodSelected event at all (unlike PHONE/EMAIL, there's no
                    // dedicated entry screen to navigate to first).
                    dispatch(ToollyUiEvent.AuthenticationStarted)
                    coroutineScope.launch {
                        when (val tokenResult = googleIdTokenProvider.requestIdToken()) {
                            is GoogleIdTokenProvider.Result.Success -> {
                                when (val result = authenticator.signInWithGoogle(tokenResult.idToken)) {
                                    is AuthResult.Success -> dispatch(ToollyUiEvent.AuthenticationSucceeded)
                                    is AuthResult.Failure ->
                                        dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                                }
                            }
                            // Neither AuthError nor AuthResult has a dedicated "cancelled" case
                            // (unlike ScanResult's own Cancelled/Failure split) -- a user
                            // dismissing the account picker surfaces the same generic error
                            // message as a real failure. Minor UX rough edge, not a correctness
                            // gap; a real fix needs a shared-core change affecting both platforms.
                            GoogleIdTokenProvider.Result.Cancelled ->
                                dispatch(ToollyUiEvent.AuthenticationFailed(AuthError.Unknown))
                            GoogleIdTokenProvider.Result.Failure ->
                                dispatch(ToollyUiEvent.AuthenticationFailed(AuthError.Unknown))
                        }
                    }
                }
                override fun useDevelopmentAccess() = dispatch(ToollyUiEvent.DevelopmentAccessGranted)
                override fun openHome() = select(ToollyDestination.HOME)
                override fun openLibrary() = select(ToollyDestination.LIBRARY)
                override fun openSearch() = select(ToollyDestination.SEARCH)
                override fun openProfile() = select(ToollyDestination.PROFILE)
                override fun signOut() {
                    coroutineScope.launch { authenticator.signOut() }
                    dispatch(ToollyUiEvent.SignedOut)
                }
                // The Scan action only renders inside the authenticated main shell, itself
                // unreachable while signed out (D-049) -- no signed-out branch needed here.
                override fun scanDocument() {
                    select(ToollyDestination.LIBRARY)
                    captureRequested = true
                }
                override fun openDocument(id: DocumentUiId) = Unit
                override fun discardCapture() = Unit
                override fun saveCapture() = Unit
                override fun navigateBack() = dispatch(ToollyUiEvent.NavigateBack)

                override fun submitPhoneNumber(phoneNumber: String) {
                    if (state.pendingEmail != null) {
                        // Mandatory phone-verification step after creating an email/password
                        // account -- this needs to LINK a credential to the just-authenticated
                        // user, not sign in fresh. Real linking is exactly the account-linking
                        // work ADR-0004 point 9 defers pending its own spike, so this step stays
                        // local-only until that lands.
                        dispatch(ToollyUiEvent.PhoneNumberSubmitted(phoneNumber))
                        return
                    }
                    dispatch(ToollyUiEvent.AuthenticationStarted)
                    coroutineScope.launch {
                        when (val result = authenticator.sendPhoneVerificationCode(phoneNumber)) {
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
                    if (state.pendingEmail != null) {
                        // Same account-linking deferral as submitPhoneNumber above.
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
                        when (val result = authenticator.confirmPhoneVerificationCode(verificationId, code)) {
                            is AuthResult.Success -> {
                                pendingPhoneVerificationId = null
                                dispatch(ToollyUiEvent.OtpVerified)
                            }
                            is AuthResult.Failure -> dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                        }
                    }
                }

                override fun completeAuthentication(email: String, password: String) {
                    dispatch(ToollyUiEvent.AuthenticationStarted)
                    coroutineScope.launch {
                        when (val result = authenticator.signInWithEmail(email, password)) {
                            is AuthResult.Success -> dispatch(ToollyUiEvent.AuthenticationSucceeded)
                            is AuthResult.Failure -> dispatch(ToollyUiEvent.AuthenticationFailed(result.error))
                        }
                    }
                }

                override fun selectCreateAccount() = dispatch(ToollyUiEvent.CreateAccountSelected)

                override fun createAccount(email: String, password: String) {
                    dispatch(ToollyUiEvent.AuthenticationStarted)
                    coroutineScope.launch {
                        when (val result = authenticator.createAccountWithEmail(email, password)) {
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
            documentsContent = documentsContent,
            searchContent = searchContent,
        )
    }
}

private const val PREFERENCES_NAME = "toolly_ui_preferences"
private const val TUTORIAL_COMPLETED_KEY = "tutorial_completed"
