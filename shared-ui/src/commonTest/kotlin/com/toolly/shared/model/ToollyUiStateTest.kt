package com.toolly.shared.model

import com.toolly.shared.auth.AuthError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToollyUiStateTest {
    @Test
    fun firstLaunchStartsAtSplashWithoutFixtureContent() {
        val state = ToollyUiState.firstLaunch()

        assertEquals(ToollyDestination.SPLASH, state.destination)
        assertEquals(ToollySessionState.SIGNED_OUT, state.sessionState)
        assertEquals(0, state.tutorialPageIndex)
        assertEquals(emptyList(), state.documents)
        assertEquals(0, state.reviewPageCount)
        assertFalse(state.appleSignInAvailable)
    }

    @Test
    fun firstLaunchMovesThroughAllTutorialPagesAndWelcome() {
        val firstPage = reduceToollyUiState(
            ToollyUiState.firstLaunch(),
            ToollyUiEvent.SplashFinished,
        )
        val secondPage = reduceToollyUiState(firstPage, ToollyUiEvent.TutorialAdvanced)
        val thirdPage = reduceToollyUiState(secondPage, ToollyUiEvent.TutorialAdvanced)
        val cannotAdvancePastLast = reduceToollyUiState(thirdPage, ToollyUiEvent.TutorialAdvanced)
        val welcome = reduceToollyUiState(thirdPage, ToollyUiEvent.TutorialCompleted)

        assertEquals(ToollyDestination.TUTORIAL, firstPage.destination)
        assertEquals(0, firstPage.tutorialPageIndex)
        assertEquals(1, secondPage.tutorialPageIndex)
        assertEquals(2, thirdPage.tutorialPageIndex)
        assertEquals(thirdPage, cannotAdvancePastLast)
        assertEquals(ToollyDestination.WELCOME, welcome.destination)
        assertTrue(welcome.tutorialCompleted)
    }

    @Test
    fun skippingTutorialPersistsTheCompletionStateContract() {
        val tutorial = reduceToollyUiState(
            ToollyUiState.firstLaunch(),
            ToollyUiEvent.SplashFinished,
        )
        val welcome = reduceToollyUiState(tutorial, ToollyUiEvent.TutorialSkipped)

        assertTrue(welcome.tutorialCompleted)
        assertEquals(ToollyDestination.WELCOME, welcome.destination)
    }

    @Test
    fun returningLaunchSkipsTutorial() {
        val state = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.SplashFinished,
        )

        assertEquals(ToollyDestination.WELCOME, state.destination)
    }

    @Test
    fun authenticatedUserCanNavigateToLibraryAndSearch() {
        val library = reduceToollyUiState(atHome(), ToollyUiEvent.MainDestinationSelected(ToollyDestination.LIBRARY))
        val search = reduceToollyUiState(library, ToollyUiEvent.MainDestinationSelected(ToollyDestination.SEARCH))

        assertEquals(ToollySessionState.AUTHENTICATED, library.sessionState)
        assertEquals(ToollyDestination.LIBRARY, library.destination)
        assertEquals(ToollyDestination.SEARCH, search.destination)
    }

    @Test
    fun signInIsOnlyReachableFromWelcomeNowThatThereIsNoLocalSession() {
        // D-049: sign-in is required before first scan -- there is no local/guest session, so the
        // "optional account entry from Profile" path this used to test no longer exists.
        val signIn = reduceToollyUiState(atProfile(), ToollyUiEvent.SignInSelected)

        assertEquals(atProfile(), signIn)
    }

    @Test
    fun developmentAccessFailsClosedUnlessExplicitlyAvailable() {
        val welcome = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.SplashFinished,
        )
        val developmentWelcome = reduceToollyUiState(
            ToollyUiState.returningSignedOut(developmentAccessAvailable = true),
            ToollyUiEvent.SplashFinished,
        )
        val unavailable = reduceToollyUiState(
            welcome,
            ToollyUiEvent.DevelopmentAccessGranted,
        )
        val available = reduceToollyUiState(
            developmentWelcome,
            ToollyUiEvent.DevelopmentAccessGranted,
        )

        assertEquals(ToollySessionState.SIGNED_OUT, unavailable.sessionState)
        assertEquals(ToollyDestination.WELCOME, unavailable.destination)
        assertEquals(ToollySessionState.DEVELOPMENT, available.sessionState)
        assertEquals(ToollyDestination.HOME, available.destination)
    }

    @Test
    fun signedOutUserCannotOpenProductDestinationsWithoutAuthenticating() {
        val state = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.MainDestinationSelected(ToollyDestination.LIBRARY),
        )

        assertEquals(ToollyDestination.SPLASH, state.destination)
        assertEquals(ToollySessionState.SIGNED_OUT, state.sessionState)
    }

    @Test
    fun authenticatedUserCanNavigateAndSignOut() {
        val welcome = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.SplashFinished,
        )
        val signIn = reduceToollyUiState(welcome, ToollyUiEvent.SignInSelected)
        val authenticated = reduceToollyUiState(
            signIn,
            ToollyUiEvent.AuthenticationSucceeded,
        )
        val library = reduceToollyUiState(
            authenticated,
            ToollyUiEvent.MainDestinationSelected(ToollyDestination.LIBRARY),
        )
        val signedOut = reduceToollyUiState(library, ToollyUiEvent.SignedOut)

        assertEquals(ToollyDestination.LIBRARY, library.destination)
        assertEquals(ToollySessionState.SIGNED_OUT, signedOut.sessionState)
        assertEquals(ToollyDestination.WELCOME, signedOut.destination)
    }

    @Test
    fun appleSignInCapabilityIsExplicit() {
        val iosState = ToollyUiState.firstLaunch(appleSignInAvailable = true)
        val androidState = ToollyUiState.firstLaunch(appleSignInAvailable = false)

        assertTrue(iosState.appleSignInAvailable)
        assertFalse(androidState.appleSignInAvailable)
    }

    @Test
    fun invalidCountsAndTutorialPageAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            ToollyUiState.firstLaunch().copy(reviewPageCount = -1)
        }
        assertFailsWith<IllegalArgumentException> {
            ToollyUiState.firstLaunch().copy(
                tutorialPageIndex = ToollyUiState.TUTORIAL_PAGE_COUNT,
            )
        }
    }

    @Test
    fun otpVerificationRequiresAPendingPhoneNumber() {
        assertFailsWith<IllegalArgumentException> {
            ToollyUiState.firstLaunch().copy(destination = ToollyDestination.OTP_VERIFICATION)
        }
    }

    private fun atSignIn(): ToollyUiState {
        val welcome = reduceToollyUiState(ToollyUiState.returningSignedOut(), ToollyUiEvent.SplashFinished)
        return reduceToollyUiState(welcome, ToollyUiEvent.SignInSelected)
    }

    /**
     * Shortest real path to an authenticated Home -- existing-email sign-in, no OTP/profile-
     * completion detour needed (see [existingEmailAccountSignsInDirectlyWithoutProfileCompletion]).
     * There is no local/guest session (D-049), so this is the only way any other fixture below
     * reaches a product destination.
     */
    private fun atHome(): ToollyUiState {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        return reduceToollyUiState(emailSignIn, ToollyUiEvent.AuthenticationSucceeded)
    }

    @Test
    fun phoneJourneyReachesHomeThroughOtpProfileAndSessionRouting() {
        val signIn = atSignIn()
        val phoneEntry = reduceToollyUiState(
            signIn,
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.PHONE),
        )
        val otp = reduceToollyUiState(phoneEntry, ToollyUiEvent.PhoneNumberSubmitted("9876543210"))
        val profile = reduceToollyUiState(otp, ToollyUiEvent.OtpVerified)
        val routing = reduceToollyUiState(profile, ToollyUiEvent.ProfileCompleted)
        val home = reduceToollyUiState(routing, ToollyUiEvent.AuthenticationSucceeded)

        assertEquals(ToollyDestination.PHONE_ENTRY, phoneEntry.destination)
        assertEquals(ToollyDestination.SIGN_IN, phoneEntry.authOrigin)
        assertEquals(ToollyDestination.OTP_VERIFICATION, otp.destination)
        assertEquals("9876543210", otp.pendingPhoneNumber)
        assertEquals(ToollyDestination.PROFILE_COMPLETION, profile.destination)
        assertEquals(ToollyDestination.SESSION_ROUTING, routing.destination)
        assertEquals(ToollyDestination.HOME, home.destination)
        assertEquals(ToollySessionState.AUTHENTICATED, home.sessionState)
        assertEquals(null, home.pendingPhoneNumber)
        assertEquals(null, home.authOrigin)
    }

    @Test
    fun blankPhoneNumberIsRejectedAsANoOp() {
        val phoneEntry = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.PHONE),
        )
        val unchanged = reduceToollyUiState(phoneEntry, ToollyUiEvent.PhoneNumberSubmitted("   "))

        assertEquals(phoneEntry, unchanged)
    }

    @Test
    fun authenticationStartedSetsBusyAndClearsAnyPriorError() {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        val failed = reduceToollyUiState(
            reduceToollyUiState(emailSignIn, ToollyUiEvent.AuthenticationStarted),
            ToollyUiEvent.AuthenticationFailed(AuthError.InvalidCredential),
        )
        val retrying = reduceToollyUiState(failed, ToollyUiEvent.AuthenticationStarted)

        assertTrue(retrying.authBusy)
        assertEquals(null, retrying.authError)
    }

    @Test
    fun authenticationStartedIsANoOpFromANonAuthDestination() {
        val home = atHome()

        assertEquals(home, reduceToollyUiState(home, ToollyUiEvent.AuthenticationStarted))
    }

    @Test
    fun authenticationFailedSetsErrorAndClearsBusyOnlyWhenBusy() {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        val notBusy = reduceToollyUiState(
            emailSignIn,
            ToollyUiEvent.AuthenticationFailed(AuthError.NetworkUnavailable),
        )
        val busy = reduceToollyUiState(emailSignIn, ToollyUiEvent.AuthenticationStarted)
        val failed = reduceToollyUiState(
            busy,
            ToollyUiEvent.AuthenticationFailed(AuthError.NetworkUnavailable),
        )

        assertEquals(emailSignIn, notBusy)
        assertFalse(failed.authBusy)
        assertEquals(AuthError.NetworkUnavailable, failed.authError)
        assertEquals(ToollyDestination.EMAIL_SIGN_IN, failed.destination)
    }

    @Test
    fun successfulAuthenticationClearsBusyAndError() {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        val busy = reduceToollyUiState(emailSignIn, ToollyUiEvent.AuthenticationStarted)
        val home = reduceToollyUiState(busy, ToollyUiEvent.AuthenticationSucceeded)

        assertFalse(home.authBusy)
        assertEquals(null, home.authError)
    }

    @Test
    fun googleAndAppleSelectionAreNoOpsUntilAProviderAdapterExists() {
        val signIn = atSignIn()
        val google = reduceToollyUiState(
            signIn,
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.GOOGLE),
        )
        val apple = reduceToollyUiState(
            signIn,
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.APPLE),
        )

        assertEquals(signIn, google)
        assertEquals(signIn, apple)
    }

    @Test
    fun existingEmailAccountSignsInDirectlyWithoutProfileCompletion() {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        val home = reduceToollyUiState(emailSignIn, ToollyUiEvent.AuthenticationSucceeded)

        assertEquals(ToollyDestination.EMAIL_SIGN_IN, emailSignIn.destination)
        assertEquals(ToollyDestination.HOME, home.destination)
        assertEquals(ToollySessionState.AUTHENTICATED, home.sessionState)
    }

    @Test
    fun creatingAnAccountRequiresAPhoneSecurityStepBeforeProfileCompletion() {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        val createAccount = reduceToollyUiState(emailSignIn, ToollyUiEvent.CreateAccountSelected)
        val phoneEntry = reduceToollyUiState(
            createAccount,
            ToollyUiEvent.AccountCreated("new@example.com"),
        )
        val otp = reduceToollyUiState(phoneEntry, ToollyUiEvent.PhoneNumberSubmitted("9123456780"))
        val profile = reduceToollyUiState(otp, ToollyUiEvent.OtpVerified)

        assertEquals(ToollyDestination.CREATE_ACCOUNT, createAccount.destination)
        assertEquals(ToollyDestination.PHONE_ENTRY, phoneEntry.destination)
        assertEquals("new@example.com", phoneEntry.pendingEmail)
        assertEquals(ToollyDestination.PROFILE_COMPLETION, profile.destination)
        assertEquals("new@example.com", profile.pendingEmail)
    }

    @Test
    fun forgotPasswordRoutesToResetAndBackReturnsToEmailSignIn() {
        val emailSignIn = reduceToollyUiState(
            atSignIn(),
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL),
        )
        val reset = reduceToollyUiState(emailSignIn, ToollyUiEvent.ForgotPasswordSelected)
        val backToEmail = reduceToollyUiState(reset, ToollyUiEvent.AuthStepBackRequested)

        assertEquals(ToollyDestination.RESET_PASSWORD, reset.destination)
        assertEquals(ToollyDestination.EMAIL_SIGN_IN, backToEmail.destination)
    }

    @Test
    fun authStepBackUnwindsPhoneEntryToItsOrigin() {
        val createProfile = reduceToollyUiState(
            reduceToollyUiState(ToollyUiState.returningSignedOut(), ToollyUiEvent.SplashFinished),
            ToollyUiEvent.CreateProfileSelected,
        )
        val phoneEntry = reduceToollyUiState(
            createProfile,
            ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.PHONE),
        )
        val back = reduceToollyUiState(phoneEntry, ToollyUiEvent.AuthStepBackRequested)

        assertEquals(ToollyDestination.CREATE_PROFILE, back.destination)
    }

    @Test
    fun authStepBackFromPhoneEntryAfterEmailCreateAccountReturnsToEmailSignIn() {
        val emailSignIn = reduceToollyUiState(atSignIn(), ToollyUiEvent.AuthenticationMethodSelected(ToollyAuthenticationMethod.EMAIL))
        val createAccount = reduceToollyUiState(emailSignIn, ToollyUiEvent.CreateAccountSelected)
        val phoneEntry = reduceToollyUiState(createAccount, ToollyUiEvent.AccountCreated("new@example.com"))
        val back = reduceToollyUiState(phoneEntry, ToollyUiEvent.AuthStepBackRequested)

        assertEquals(ToollyDestination.EMAIL_SIGN_IN, back.destination)
    }

    @Test
    fun eventsAreNoOpsFromTheWrongDestination() {
        val home = atHome()

        assertEquals(home, reduceToollyUiState(home, ToollyUiEvent.OtpVerified))
        assertEquals(home, reduceToollyUiState(home, ToollyUiEvent.CreateAccountSelected))
        assertEquals(home, reduceToollyUiState(home, ToollyUiEvent.ForgotPasswordSelected))
        assertEquals(home, reduceToollyUiState(home, ToollyUiEvent.ProfileCompleted))
        assertEquals(
            home,
            reduceToollyUiState(home, ToollyUiEvent.PhoneNumberSubmitted("9876543210")),
        )
    }

    private fun atProfile(): ToollyUiState =
        reduceToollyUiState(atHome(), ToollyUiEvent.MainDestinationSelected(ToollyDestination.PROFILE))

    @Test
    fun privacyCenterAndBackupChoiceAreReachableFromProfileAndUnwindWithNavigateBack() {
        val profile = atProfile()
        val privacyCenter = reduceToollyUiState(profile, ToollyUiEvent.PrivacyCenterOpened)
        val backupChoice = reduceToollyUiState(privacyCenter, ToollyUiEvent.BackupSettingsOpened)
        val backToPrivacyCenter = reduceToollyUiState(backupChoice, ToollyUiEvent.NavigateBack)
        val backToProfile = reduceToollyUiState(backToPrivacyCenter, ToollyUiEvent.NavigateBack)

        assertEquals(ToollyDestination.PRIVACY_CENTER, privacyCenter.destination)
        assertEquals(ToollyDestination.BACKUP_CHOICE, backupChoice.destination)
        assertEquals(ToollyDestination.PRIVACY_CENTER, backToPrivacyCenter.destination)
        assertEquals(ToollyDestination.PROFILE, backToProfile.destination)
    }

    @Test
    fun navigateBackFromDocumentViewerReturnsToLibrary() {
        val viewer = atLibrary().copy(destination = ToollyDestination.DOCUMENT_VIEWER)

        val back = reduceToollyUiState(viewer, ToollyUiEvent.NavigateBack)

        assertEquals(ToollyDestination.LIBRARY, back.destination)
    }

    @Test
    fun navigateBackIsANoOpFromDestinationsWithoutABackTarget() {
        val profile = atProfile()

        assertEquals(profile, reduceToollyUiState(profile, ToollyUiEvent.NavigateBack))
    }

    @Test
    fun backupPreferencesDefaultMatchesWireframeAndOnlyToggleFromBackupChoice() {
        val defaults = ToollyUiState.empty().backupPreferences

        assertFalse(defaults.enabled)
        assertTrue(defaults.wifiOnly)
        assertTrue(defaults.whileCharging)
        assertFalse(defaults.includeOriginals)
        assertFalse(defaults.endToEndEncryption)

        val profile = atProfile()
        val unchanged = reduceToollyUiState(
            profile,
            ToollyUiEvent.BackupPreferenceToggled(BackupPreferenceKind.WIFI_ONLY, false),
        )
        assertEquals(profile, unchanged)
    }

    @Test
    fun togglingEachBackupPreferenceOnlyChangesThatPreference() {
        val backupChoice = reduceToollyUiState(
            reduceToollyUiState(atProfile(), ToollyUiEvent.PrivacyCenterOpened),
            ToollyUiEvent.BackupSettingsOpened,
        )

        val wifiOff = reduceToollyUiState(
            backupChoice,
            ToollyUiEvent.BackupPreferenceToggled(BackupPreferenceKind.WIFI_ONLY, false),
        )
        val originalsOn = reduceToollyUiState(
            wifiOff,
            ToollyUiEvent.BackupPreferenceToggled(BackupPreferenceKind.INCLUDE_ORIGINALS, true),
        )
        val enabled = reduceToollyUiState(originalsOn, ToollyUiEvent.BackupEnabledChanged(true))

        assertFalse(enabled.backupPreferences.wifiOnly)
        assertTrue(enabled.backupPreferences.includeOriginals)
        assertTrue(enabled.backupPreferences.whileCharging)
        assertFalse(enabled.backupPreferences.endToEndEncryption)
        assertTrue(enabled.backupPreferences.enabled)
    }

    private fun atLibrary(): ToollyUiState =
        reduceToollyUiState(atHome(), ToollyUiEvent.MainDestinationSelected(ToollyDestination.LIBRARY))

    @Test
    fun captureReviewStartedEntersReviewWithTheRealPageCountAndDiscardReturnsToLibrary() {
        val library = atLibrary()
        val review = reduceToollyUiState(library, ToollyUiEvent.CaptureReviewStarted(3))
        val discarded = reduceToollyUiState(review, ToollyUiEvent.CaptureDiscarded)

        assertEquals(ToollyDestination.CAPTURE_REVIEW, review.destination)
        assertEquals(3, review.reviewPageCount)
        assertEquals(ToollyDestination.LIBRARY, discarded.destination)
        assertEquals(0, discarded.reviewPageCount)
    }

    @Test
    fun captureReviewStartedIsANoOpWithoutASessionOrAwayFromHomeAndLibrary() {
        val welcome = reduceToollyUiState(ToollyUiState.returningSignedOut(), ToollyUiEvent.SplashFinished)
        val signedOutAttempt = reduceToollyUiState(welcome, ToollyUiEvent.CaptureReviewStarted(2))
        val profileAttempt = reduceToollyUiState(atProfile(), ToollyUiEvent.CaptureReviewStarted(2))
        val zeroPagesAttempt = reduceToollyUiState(atLibrary(), ToollyUiEvent.CaptureReviewStarted(0))

        assertEquals(welcome, signedOutAttempt)
        assertEquals(atProfile(), profileAttempt)
        assertEquals(atLibrary(), zeroPagesAttempt)
    }

    @Test
    fun captureDiscardedIsANoOpOutsideCaptureReview() {
        val library = atLibrary()
        val unchanged = reduceToollyUiState(library, ToollyUiEvent.CaptureDiscarded)

        assertEquals(library, unchanged)
    }
}
