package com.toolly.shared.model

import com.toolly.shared.auth.AuthError
import kotlin.jvm.JvmInline

@JvmInline
value class DocumentUiId(val value: String)

data class DocumentListItem(
    val id: DocumentUiId,
    val pageCount: Int,
    val title: String? = null,
)

/**
 * Title-only substring match against [documents], mirroring the real Android search
 * implementation's behavior exactly (`SearchDocumentsScreen`'s `displayName?.contains(query,
 * ignoreCase = true)`): a blank [query] returns no results (the caller shows a "type to search"
 * prompt instead, per wireframe `4.2 Search`'s empty-query state), never "all documents". Search
 * is deliberately title-only -- recognized-text (OCR) matching shown in the wireframe is a Phase 5+
 * capability that does not exist yet (see `docs/product/ENTITLEMENTS.md`), so this never claims to
 * match on content it hasn't actually read. Untitled documents ([DocumentListItem.title] null)
 * can't match a non-blank query and are correctly excluded.
 */
fun filterDocumentsByTitle(documents: List<DocumentListItem>, query: String): List<DocumentListItem> =
    if (query.isBlank()) {
        emptyList()
    } else {
        documents.filter { it.title?.contains(query, ignoreCase = true) == true }
    }

/**
 * [DEVELOPMENT] is the debug-only local authentication adapter (D-047) -- release-disabled,
 * never a substitute for real authentication. There is no guest/local-without-account session:
 * D-049 reaffirms D-021/ADR-0004 -- sign-in is required before the first scan.
 */
enum class ToollySessionState {
    SIGNED_OUT,
    DEVELOPMENT,
    AUTHENTICATED,
}

enum class ToollyAuthenticationMethod {
    PHONE,
    EMAIL,
    GOOGLE,
    APPLE,
}

enum class ToollyDestination {
    SPLASH,
    TUTORIAL,
    WELCOME,
    SIGN_IN,
    CREATE_PROFILE,
    PHONE_ENTRY,
    OTP_VERIFICATION,
    EMAIL_SIGN_IN,
    CREATE_ACCOUNT,
    RESET_PASSWORD,
    PROFILE_COMPLETION,
    SESSION_ROUTING,
    HOME,
    LIBRARY,
    SEARCH,
    PROFILE,
    CAPTURE_REVIEW,
    DOCUMENT_VIEWER,
    PRIVACY_CENTER,
    BACKUP_CHOICE,
}

enum class BackupPreferenceKind { WIFI_ONLY, WHILE_CHARGING, INCLUDE_ORIGINALS, END_TO_END_ENCRYPTION }

/**
 * Local, presentation-only backup preferences (wireframe `6.2/6.4 Backup choice`). Nothing here is
 * persisted or sent anywhere -- Phase 5 (optional cloud backup) is blocked until Phase 4
 * (authentication) is complete and its own service-processing approvals land (ROADMAP.md). Toggling
 * these only changes what the Backup Choice screen displays.
 */
data class BackupPreferences(
    val enabled: Boolean = false,
    val wifiOnly: Boolean = true,
    val whileCharging: Boolean = true,
    val includeOriginals: Boolean = false,
    val endToEndEncryption: Boolean = false,
)

sealed interface ToollyUiEvent {
    data object SplashFinished : ToollyUiEvent
    data object TutorialAdvanced : ToollyUiEvent
    data object TutorialSkipped : ToollyUiEvent
    data object TutorialCompleted : ToollyUiEvent
    data object SignInSelected : ToollyUiEvent
    data object CreateProfileSelected : ToollyUiEvent
    data object BackToWelcome : ToollyUiEvent
    data object AuthenticationSucceeded : ToollyUiEvent
    data object DevelopmentAccessGranted : ToollyUiEvent
    data class MainDestinationSelected(val destination: ToollyDestination) : ToollyUiEvent
    data object SignedOut : ToollyUiEvent

    /** [ToollyAuthenticationMethod.GOOGLE] / [ToollyAuthenticationMethod.APPLE] are no-ops until a real provider adapter lands behind the authentication port. */
    data class AuthenticationMethodSelected(val method: ToollyAuthenticationMethod) : ToollyUiEvent
    data class PhoneNumberSubmitted(val phoneNumber: String) : ToollyUiEvent
    data object OtpVerified : ToollyUiEvent
    data object CreateAccountSelected : ToollyUiEvent
    data class AccountCreated(val email: String) : ToollyUiEvent
    data object ForgotPasswordSelected : ToollyUiEvent
    data object ProfileCompleted : ToollyUiEvent
    data object AuthStepBackRequested : ToollyUiEvent

    /** Dispatched right before a host starts a real, async authentication call. */
    data object AuthenticationStarted : ToollyUiEvent

    /** A real authentication call failed with an allowlisted [AuthError] (ADR-0004 point 8). */
    data class AuthenticationFailed(val error: AuthError) : ToollyUiEvent

    data object PrivacyCenterOpened : ToollyUiEvent
    data object BackupSettingsOpened : ToollyUiEvent
    data class BackupPreferenceToggled(val kind: BackupPreferenceKind, val enabled: Boolean) : ToollyUiEvent
    data class BackupEnabledChanged(val enabled: Boolean) : ToollyUiEvent
    data object NavigateBack : ToollyUiEvent

    /** A real capture just finished with [pageCount] pages; enters the review screen. */
    data class CaptureReviewStarted(val pageCount: Int) : ToollyUiEvent
    data object CaptureDiscarded : ToollyUiEvent
}

data class ToollyUiState(
    val destination: ToollyDestination,
    val tutorialCompleted: Boolean,
    val tutorialPageIndex: Int,
    val sessionState: ToollySessionState,
    val developmentAccessAvailable: Boolean,
    val appleSignInAvailable: Boolean,
    val documents: List<DocumentListItem>,
    val selectedDocumentId: DocumentUiId?,
    val reviewPageCount: Int,
    val busy: Boolean,
    // Optional-account auth sub-flow (2.x/3.x/4.x wireframes). None of this is persisted or sent
    // anywhere yet -- real Firebase Authentication stays behind the authentication port until its
    // Phase 4 gate is approved (see README architecture principles + issue #52).
    val authOrigin: ToollyDestination? = null,
    val pendingPhoneNumber: String? = null,
    val pendingEmail: String? = null,
    val backupPreferences: BackupPreferences = BackupPreferences(),
    // Real, async authentication call in flight/failed (ADR-0004). Distinct from [busy], which
    // tracks capture sessions -- these can be true/set independently of each other.
    val authBusy: Boolean = false,
    val authError: AuthError? = null,
) {
    init {
        require(tutorialPageIndex in 0 until TUTORIAL_PAGE_COUNT)
        require(reviewPageCount >= 0)
        require(documents.all { it.pageCount > 0 })
        require(
            sessionState != ToollySessionState.SIGNED_OUT ||
                destination !in productDestinations,
        )
        require(authOrigin == null || authOrigin in setOf(ToollyDestination.SIGN_IN, ToollyDestination.CREATE_PROFILE))
        require(destination != ToollyDestination.OTP_VERIFICATION || pendingPhoneNumber != null)
    }

    companion object {
        const val TUTORIAL_PAGE_COUNT = 3
        const val OTP_LENGTH = 6

        private val productDestinations = setOf(
            ToollyDestination.HOME,
            ToollyDestination.LIBRARY,
            ToollyDestination.SEARCH,
            ToollyDestination.PROFILE,
            ToollyDestination.CAPTURE_REVIEW,
            ToollyDestination.DOCUMENT_VIEWER,
            ToollyDestination.PRIVACY_CENTER,
            ToollyDestination.BACKUP_CHOICE,
        )

        fun firstLaunch(
            tutorialCompleted: Boolean = false,
            developmentAccessAvailable: Boolean = false,
            appleSignInAvailable: Boolean = false,
        ): ToollyUiState = ToollyUiState(
            destination = ToollyDestination.SPLASH,
            tutorialCompleted = tutorialCompleted,
            tutorialPageIndex = 0,
            sessionState = ToollySessionState.SIGNED_OUT,
            developmentAccessAvailable = developmentAccessAvailable,
            appleSignInAvailable = appleSignInAvailable,
            documents = emptyList(),
            selectedDocumentId = null,
            reviewPageCount = 0,
            busy = false,
        )

        fun returningSignedOut(
            developmentAccessAvailable: Boolean = false,
            appleSignInAvailable: Boolean = false,
        ): ToollyUiState = firstLaunch(
            tutorialCompleted = true,
            developmentAccessAvailable = developmentAccessAvailable,
            appleSignInAvailable = appleSignInAvailable,
        )

        fun empty(): ToollyUiState = firstLaunch()
    }
}

private val accountEntryDestinations = setOf(ToollyDestination.SIGN_IN, ToollyDestination.CREATE_PROFILE)

fun reduceToollyUiState(
    state: ToollyUiState,
    event: ToollyUiEvent,
): ToollyUiState = when (event) {
    ToollyUiEvent.SplashFinished -> {
        if (state.destination != ToollyDestination.SPLASH) {
            state
        } else {
            state.copy(
                destination = when {
                    state.sessionState != ToollySessionState.SIGNED_OUT -> ToollyDestination.HOME
                    state.tutorialCompleted -> ToollyDestination.WELCOME
                    else -> ToollyDestination.TUTORIAL
                },
            )
        }
    }

    ToollyUiEvent.TutorialAdvanced -> {
        if (
            state.destination != ToollyDestination.TUTORIAL ||
            state.tutorialPageIndex >= ToollyUiState.TUTORIAL_PAGE_COUNT - 1
        ) {
            state
        } else {
            state.copy(tutorialPageIndex = state.tutorialPageIndex + 1)
        }
    }

    ToollyUiEvent.TutorialSkipped,
    ToollyUiEvent.TutorialCompleted -> {
        if (state.destination != ToollyDestination.TUTORIAL) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.WELCOME,
                tutorialCompleted = true,
            )
        }
    }

    ToollyUiEvent.SignInSelected -> {
        if (state.destination != ToollyDestination.WELCOME) state
        else state.copy(destination = ToollyDestination.SIGN_IN)
    }

    ToollyUiEvent.CreateProfileSelected -> {
        if (
            state.destination !in setOf(
                ToollyDestination.WELCOME,
                ToollyDestination.SIGN_IN,
            )
        ) state
        else state.copy(destination = ToollyDestination.CREATE_PROFILE)
    }

    ToollyUiEvent.BackToWelcome -> {
        if (
            state.destination !in setOf(
                ToollyDestination.SIGN_IN,
                ToollyDestination.CREATE_PROFILE,
            )
        ) state
        else state.copy(
            destination = ToollyDestination.WELCOME,
            authOrigin = null,
            pendingPhoneNumber = null,
            pendingEmail = null,
            authBusy = false,
            authError = null,
        )
    }

    ToollyUiEvent.AuthenticationSucceeded -> {
        if (
            state.destination !in setOf(
                ToollyDestination.SIGN_IN,
                ToollyDestination.CREATE_PROFILE,
                ToollyDestination.EMAIL_SIGN_IN,
                ToollyDestination.SESSION_ROUTING,
            )
        ) state
        else state.copy(
            destination = ToollyDestination.HOME,
            sessionState = ToollySessionState.AUTHENTICATED,
            authOrigin = null,
            pendingPhoneNumber = null,
            pendingEmail = null,
            authBusy = false,
            authError = null,
        )
    }

    ToollyUiEvent.AuthenticationStarted -> {
        if (
            state.destination !in setOf(
                // SIGN_IN/CREATE_PROFILE: Google's SecondaryButton lives on both (AccountScreen),
                // so a Google sign-in attempt can start from either without a PHONE/EMAIL detour.
                ToollyDestination.SIGN_IN,
                ToollyDestination.CREATE_PROFILE,
                ToollyDestination.PHONE_ENTRY,
                ToollyDestination.OTP_VERIFICATION,
                ToollyDestination.EMAIL_SIGN_IN,
                ToollyDestination.CREATE_ACCOUNT,
            )
        ) state
        else state.copy(authBusy = true, authError = null)
    }

    is ToollyUiEvent.AuthenticationFailed -> {
        if (!state.authBusy) state else state.copy(authBusy = false, authError = event.error)
    }

    ToollyUiEvent.DevelopmentAccessGranted -> {
        if (
            !state.developmentAccessAvailable ||
            state.destination !in setOf(
                ToollyDestination.WELCOME,
                ToollyDestination.SIGN_IN,
                ToollyDestination.CREATE_PROFILE,
            )
        ) state
        else state.copy(
            destination = ToollyDestination.HOME,
            sessionState = ToollySessionState.DEVELOPMENT,
        )
    }

    is ToollyUiEvent.MainDestinationSelected -> {
        if (
            state.sessionState == ToollySessionState.SIGNED_OUT ||
            event.destination !in setOf(
                ToollyDestination.HOME,
                ToollyDestination.LIBRARY,
                ToollyDestination.SEARCH,
                ToollyDestination.PROFILE,
            )
        ) {
            state
        } else {
            state.copy(destination = event.destination)
        }
    }

    ToollyUiEvent.SignedOut -> {
        if (state.sessionState == ToollySessionState.SIGNED_OUT) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.WELCOME,
                sessionState = ToollySessionState.SIGNED_OUT,
                selectedDocumentId = null,
                reviewPageCount = 0,
                busy = false,
                authOrigin = null,
                pendingPhoneNumber = null,
                pendingEmail = null,
                authBusy = false,
                authError = null,
            )
        }
    }

    is ToollyUiEvent.AuthenticationMethodSelected -> {
        if (state.destination !in accountEntryDestinations) {
            state
        } else {
            when (event.method) {
                ToollyAuthenticationMethod.PHONE -> state.copy(
                    destination = ToollyDestination.PHONE_ENTRY,
                    authOrigin = state.destination,
                    authError = null,
                )
                ToollyAuthenticationMethod.EMAIL -> state.copy(
                    destination = ToollyDestination.EMAIL_SIGN_IN,
                    authOrigin = state.destination,
                    authError = null,
                )
                // Google/Apple require a real provider SDK behind the authentication port; not
                // implemented in this phase, so the request is a deliberate no-op.
                ToollyAuthenticationMethod.GOOGLE, ToollyAuthenticationMethod.APPLE -> state
            }
        }
    }

    is ToollyUiEvent.PhoneNumberSubmitted -> {
        if (state.destination != ToollyDestination.PHONE_ENTRY || event.phoneNumber.isBlank()) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.OTP_VERIFICATION,
                pendingPhoneNumber = event.phoneNumber,
                authBusy = false,
                authError = null,
            )
        }
    }

    ToollyUiEvent.OtpVerified -> {
        if (state.destination != ToollyDestination.OTP_VERIFICATION) {
            state
        } else {
            state.copy(destination = ToollyDestination.PROFILE_COMPLETION, authBusy = false, authError = null)
        }
    }

    ToollyUiEvent.CreateAccountSelected -> {
        if (state.destination != ToollyDestination.EMAIL_SIGN_IN) {
            state
        } else {
            state.copy(destination = ToollyDestination.CREATE_ACCOUNT, authError = null)
        }
    }

    is ToollyUiEvent.AccountCreated -> {
        if (state.destination != ToollyDestination.CREATE_ACCOUNT || event.email.isBlank()) {
            state
        } else {
            // New accounts (any provider) verify a phone as a security step before profile
            // completion -- matches wireframe 4.1 "One last security step."
            state.copy(
                destination = ToollyDestination.PHONE_ENTRY,
                pendingEmail = event.email,
                authBusy = false,
                authError = null,
            )
        }
    }

    ToollyUiEvent.ForgotPasswordSelected -> {
        if (state.destination != ToollyDestination.EMAIL_SIGN_IN) {
            state
        } else {
            state.copy(destination = ToollyDestination.RESET_PASSWORD, authError = null)
        }
    }

    ToollyUiEvent.ProfileCompleted -> {
        if (state.destination != ToollyDestination.PROFILE_COMPLETION) {
            state
        } else {
            state.copy(destination = ToollyDestination.SESSION_ROUTING)
        }
    }

    ToollyUiEvent.AuthStepBackRequested -> when (state.destination) {
        ToollyDestination.PHONE_ENTRY -> state.copy(
            destination = if (state.pendingEmail != null) {
                ToollyDestination.EMAIL_SIGN_IN
            } else {
                state.authOrigin ?: ToollyDestination.WELCOME
            },
            authBusy = false,
            authError = null,
        )
        ToollyDestination.OTP_VERIFICATION -> state.copy(
            destination = ToollyDestination.PHONE_ENTRY,
            pendingPhoneNumber = null,
            authBusy = false,
            authError = null,
        )
        ToollyDestination.EMAIL_SIGN_IN -> state.copy(
            destination = state.authOrigin ?: ToollyDestination.WELCOME,
            authOrigin = null,
            pendingEmail = null,
            authBusy = false,
            authError = null,
        )
        ToollyDestination.CREATE_ACCOUNT,
        ToollyDestination.RESET_PASSWORD -> state.copy(
            destination = ToollyDestination.EMAIL_SIGN_IN,
            authBusy = false,
            authError = null,
        )
        else -> state
    }

    ToollyUiEvent.PrivacyCenterOpened -> {
        if (state.destination != ToollyDestination.PROFILE) {
            state
        } else {
            state.copy(destination = ToollyDestination.PRIVACY_CENTER)
        }
    }

    ToollyUiEvent.BackupSettingsOpened -> {
        if (state.destination != ToollyDestination.PRIVACY_CENTER) {
            state
        } else {
            state.copy(destination = ToollyDestination.BACKUP_CHOICE)
        }
    }

    is ToollyUiEvent.BackupPreferenceToggled -> {
        if (state.destination != ToollyDestination.BACKUP_CHOICE) {
            state
        } else {
            state.copy(
                backupPreferences = when (event.kind) {
                    BackupPreferenceKind.WIFI_ONLY ->
                        state.backupPreferences.copy(wifiOnly = event.enabled)
                    BackupPreferenceKind.WHILE_CHARGING ->
                        state.backupPreferences.copy(whileCharging = event.enabled)
                    BackupPreferenceKind.INCLUDE_ORIGINALS ->
                        state.backupPreferences.copy(includeOriginals = event.enabled)
                    BackupPreferenceKind.END_TO_END_ENCRYPTION ->
                        state.backupPreferences.copy(endToEndEncryption = event.enabled)
                },
            )
        }
    }

    is ToollyUiEvent.BackupEnabledChanged -> {
        if (state.destination != ToollyDestination.BACKUP_CHOICE) {
            state
        } else {
            state.copy(backupPreferences = state.backupPreferences.copy(enabled = event.enabled))
        }
    }

    ToollyUiEvent.NavigateBack -> when (state.destination) {
        ToollyDestination.DOCUMENT_VIEWER -> state.copy(destination = ToollyDestination.LIBRARY)
        ToollyDestination.PRIVACY_CENTER -> state.copy(destination = ToollyDestination.PROFILE)
        ToollyDestination.BACKUP_CHOICE -> state.copy(destination = ToollyDestination.PRIVACY_CENTER)
        else -> state
    }

    is ToollyUiEvent.CaptureReviewStarted -> {
        if (
            state.destination !in setOf(ToollyDestination.HOME, ToollyDestination.LIBRARY) ||
            state.sessionState == ToollySessionState.SIGNED_OUT ||
            event.pageCount <= 0
        ) {
            state
        } else {
            state.copy(destination = ToollyDestination.CAPTURE_REVIEW, reviewPageCount = event.pageCount)
        }
    }

    ToollyUiEvent.CaptureDiscarded -> {
        if (state.destination != ToollyDestination.CAPTURE_REVIEW) {
            state
        } else {
            state.copy(destination = ToollyDestination.LIBRARY, reviewPageCount = 0)
        }
    }
}

interface ToollyUiActions {
    fun finishSplash()
    fun nextTutorial()
    fun skipTutorial()
    fun completeTutorial()
    fun showSignIn()
    fun showCreateProfile()
    fun backToWelcome()
    fun authenticate(method: ToollyAuthenticationMethod)
    fun useDevelopmentAccess()
    fun openHome()
    fun openLibrary()
    fun openSearch()
    fun openProfile()
    fun signOut()
    fun scanDocument()
    fun openDocument(id: DocumentUiId)
    fun discardCapture()
    fun saveCapture()
    fun navigateBack()
    fun submitPhoneNumber(phoneNumber: String)
    fun verifyOtp(code: String)
    fun completeAuthentication(email: String, password: String)
    fun selectCreateAccount()
    fun createAccount(email: String, password: String)

    /**
     * Finishes new-account onboarding (post profile-completion) and becomes authenticated. No
     * credentials to submit here -- the account was already created and phone-verified in
     * earlier steps -- so this is a direct, synchronous state transition, not a Firebase call.
     */
    fun finishOnboarding()
    fun selectForgotPassword()
    fun completeProfile()
    fun authStepBack()
    fun openPrivacyCenter()
    fun openBackupSettings()
    fun setBackupPreference(kind: BackupPreferenceKind, enabled: Boolean)
    fun setBackupEnabled(enabled: Boolean)
}
