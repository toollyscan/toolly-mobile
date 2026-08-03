package com.toolly.shared.model

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
    fun localUseDoesNotRequireAuthentication() {
        val welcome = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.SplashFinished,
        )
        val library = reduceToollyUiState(
            welcome,
            ToollyUiEvent.LocalSessionStarted(ToollyDestination.LIBRARY),
        )
        val search = reduceToollyUiState(
            library,
            ToollyUiEvent.MainDestinationSelected(ToollyDestination.SEARCH),
        )

        assertEquals(ToollySessionState.LOCAL, library.sessionState)
        assertEquals(ToollyDestination.LIBRARY, library.destination)
        assertEquals(ToollyDestination.SEARCH, search.destination)
    }

    @Test
    fun localSessionCanOpenOptionalAccountAndReturnToProfile() {
        val welcome = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.SplashFinished,
        )
        val local = reduceToollyUiState(
            welcome,
            ToollyUiEvent.LocalSessionStarted(ToollyDestination.HOME),
        )
        val profile = reduceToollyUiState(
            local,
            ToollyUiEvent.MainDestinationSelected(ToollyDestination.PROFILE),
        )
        val signIn = reduceToollyUiState(profile, ToollyUiEvent.SignInSelected)
        val returned = reduceToollyUiState(signIn, ToollyUiEvent.BackToWelcome)

        assertEquals(ToollyDestination.SIGN_IN, signIn.destination)
        assertEquals(ToollySessionState.LOCAL, signIn.sessionState)
        assertEquals(ToollyDestination.PROFILE, returned.destination)
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
    fun signedOutUserCannotOpenProductDestinationsWithoutChoosingLocalUse() {
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
}
