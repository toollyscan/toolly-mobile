package com.toolly.shared.ui

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import com.toolly.domain.model.CapturedPageDraft
import com.toolly.domain.model.TemporaryAssetId as DomainTemporaryAssetId
import com.toolly.domain.usecases.ListDocumentsUseCase
import com.toolly.domain.usecases.SaveCapturedDocumentUseCase
import com.toolly.foundation.OpaqueIdGenerator
import com.toolly.foundation.ToollyClock
import com.toolly.foundation.ToollyResult
import com.toolly.shared.auth.AuthError
import com.toolly.shared.auth.AuthResult
import com.toolly.shared.auth.PhoneVerificationId
import com.toolly.shared.auth.PhoneVerificationResult
import com.toolly.shared.capture.ScanConfig
import com.toolly.shared.capture.ScanResult
import com.toolly.shared.capture.ScannedPage
import com.toolly.shared.model.BackupPreferenceKind
import com.toolly.shared.model.BackupProvider
import com.toolly.shared.model.DocumentListItem
import com.toolly.shared.model.DocumentUiId
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiEvent
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.model.reduceToollyUiState
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.NSLocale
import platform.Foundation.NSUUID
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localizedStringForLanguageCode
import platform.Foundation.timeIntervalSince1970

@Suppress("FunctionName")
fun MainViewController() = MainViewController(
    developmentAccessAvailable = false,
    captureSession = null,
    accountAuthenticatorSession = null,
    documentVaultSession = null,
)

@Suppress("FunctionName")
fun MainViewController(developmentAccessAvailable: Boolean) = MainViewController(
    developmentAccessAvailable = developmentAccessAvailable,
    captureSession = null,
    accountAuthenticatorSession = null,
    documentVaultSession = null,
)

@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
    captureSession: AppleCaptureSession?,
) = MainViewController(
    developmentAccessAvailable = developmentAccessAvailable,
    captureSession = captureSession,
    accountAuthenticatorSession = null,
    documentVaultSession = null,
)

@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
    captureSession: AppleCaptureSession?,
    accountAuthenticatorSession: AppleAccountAuthenticatorSession?,
) = MainViewController(
    developmentAccessAvailable = developmentAccessAvailable,
    captureSession = captureSession,
    accountAuthenticatorSession = accountAuthenticatorSession,
    documentVaultSession = null,
)

/**
 * [captureSession] is the first-party Swift VisionKit implementation of [AppleCaptureSession]
 * (see `AppleCaptureBridge.kt`), [accountAuthenticatorSession] is the first-party Swift Firebase
 * implementation of [AppleAccountAuthenticatorSession] (see `AppleAuthBridge.kt`), and
 * [documentVaultSession] is the first-party Swift local-document-store implementation of
 * [AppleDocumentVaultSession] (see `AppleDocumentVaultBridge.kt`, TLY-014 Phase 2/#82 -- no
 * cryptography behind it yet). All three are nullable so existing/test callers that don't care
 * about capture, real authentication or persistence keep compiling unchanged -- with `null`,
 * `scanDocument()` falls back to its original library-navigation-only behavior, auth actions stay
 * local-only/mock, and `saveCapture()` stays the no-op it always was (matching the pre-existing
 * behavior before any adapter existed).
 */
@Suppress("FunctionName")
fun MainViewController(
    developmentAccessAvailable: Boolean,
    captureSession: AppleCaptureSession?,
    accountAuthenticatorSession: AppleAccountAuthenticatorSession?,
    documentVaultSession: AppleDocumentVaultSession?,
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

    // clock/idGenerator use Foundation directly (NSDate/NSUUID) rather than a shared-core
    // implementation -- these are two-line platform primitives, not worth an expect/actual.
    val vaultRepository = remember(documentVaultSession) {
        documentVaultSession?.let(::AppleDocumentRepository)
    }
    val saveCapturedDocument = remember(vaultRepository) {
        vaultRepository?.let {
            SaveCapturedDocumentUseCase(
                repository = it,
                clock = ToollyClock { (NSDate().timeIntervalSince1970 * 1000).toLong() },
                idGenerator = OpaqueIdGenerator { NSUUID().UUIDString().lowercase() },
            )
        }
    }
    val listDocuments = remember(vaultRepository) { vaultRepository?.let(::ListDocumentsUseCase) }

    fun dispatch(event: ToollyUiEvent) {
        state = reduceToollyUiState(state, event)
    }
    fun persistTutorialCompletion(event: ToollyUiEvent) {
        preferences.setBool(true, forKey = TUTORIAL_COMPLETED_KEY)
        dispatch(event)
    }
    suspend fun refreshDocuments() {
        val activeListDocuments = listDocuments ?: return
        val result = activeListDocuments()
        if (result is ToollyResult.Success) {
            dispatch(
                ToollyUiEvent.DocumentsLoaded(
                    result.value.map { summary ->
                        DocumentListItem(
                            id = DocumentUiId(summary.id.value),
                            pageCount = summary.pageCount,
                            title = summary.displayName,
                        )
                    },
                ),
            )
        }
    }
    LaunchedEffect(vaultRepository) { refreshDocuments() }

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

            // With no real [documentVaultSession] wired, stays the no-op it always was (issue #48;
            // a real vault is TLY-014/#82, in progress). With one wired, mirrors
            // AndroidToollyApp.kt's save flow: persist, release the now-redundant staged temp
            // files, return to the library and refresh it with the newly-saved document.
            override fun saveCapture() {
                val activeUseCase = saveCapturedDocument ?: return
                if (state.busy) return
                state = state.copy(busy = true)
                coroutineScope.launch {
                    val drafts = capturedPages.map { page ->
                        CapturedPageDraft(
                            temporaryAssetId = DomainTemporaryAssetId(page.assetId.value),
                            ordinal = page.index,
                            widthPixels = null,
                            heightPixels = null,
                        )
                    }
                    when (activeUseCase(drafts)) {
                        is ToollyResult.Success -> {
                            scanner?.release(capturedPages)
                            capturedPages = emptyList()
                            state = state.copy(busy = false)
                            dispatch(ToollyUiEvent.CaptureSaved)
                            refreshDocuments()
                        }
                        // No error-message slot in shared state for the iOS host yet (same gap
                        // scanDocument() already has, above) -- stays on the review screen with
                        // the captured pages intact so the user can retry.
                        is ToollyResult.Failure -> state = state.copy(busy = false)
                    }
                }
            }

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
            override fun selectBackupProvider(provider: BackupProvider) =
                dispatch(ToollyUiEvent.BackupProviderSelected(provider))
            override fun openBackupPolicy() = dispatch(ToollyUiEvent.BackupPolicyOpened)
            override fun setBackupPreference(kind: BackupPreferenceKind, enabled: Boolean) =
                dispatch(ToollyUiEvent.BackupPreferenceToggled(kind, enabled))
            override fun confirmBackupPolicy() = dispatch(ToollyUiEvent.BackupPolicyConfirmed)

            private fun select(destination: ToollyDestination) {
                dispatch(ToollyUiEvent.MainDestinationSelected(destination))
            }
        },
        appLanguageDisplayName = currentAppLanguageDisplayName(),
        // 4.2 Complete profile's photo picker needs a real PHPickerViewController consent-UI flow,
        // which (like Apple sign-in, see AuthScreens.kt's file doc) can't be built/verified without
        // Xcode on this machine -- documented pending, not faked. hasProfilePhoto stays false and
        // the edit badge is a real, wired no-op rather than silently doing nothing unexpectedly.
        hasProfilePhoto = false,
        onPickProfilePhoto = {},
    )
}

private fun currentAppLanguageDisplayName(): String {
    val locale = NSLocale.currentLocale
    return locale.localizedStringForLanguageCode(locale.languageCode)
        ?.replaceFirstChar { it.uppercase() }
        ?: "English"
}

private const val TUTORIAL_COMPLETED_KEY = "toolly.tutorial.completed"
