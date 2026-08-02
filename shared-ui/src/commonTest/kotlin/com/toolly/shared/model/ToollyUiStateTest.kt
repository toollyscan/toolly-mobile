package com.toolly.shared.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ToollyUiStateTest {
    @Test
    fun firstLaunchStartsAtSplashWithoutFixtureContent() {
        val state = ToollyUiState.firstLaunch()

        assertEquals(ToollyDestination.SPLASH, state.destination)
        assertEquals(ToollySessionState.SIGNED_OUT, state.sessionState)
        assertEquals(emptyList(), state.documents)
        assertEquals(0, state.reviewPageCount)
    }

    @Test
    fun firstLaunchMovesThroughTutorialAndWelcome() {
        val tutorial = reduceToollyUiState(
            ToollyUiState.firstLaunch(),
            ToollyUiEvent.SplashFinished,
        )
        val welcome = reduceToollyUiState(tutorial, ToollyUiEvent.TutorialCompleted)

        assertEquals(ToollyDestination.TUTORIAL, tutorial.destination)
        assertEquals(ToollyDestination.WELCOME, welcome.destination)
        assertEquals(true, welcome.tutorialCompleted)
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
    fun signedOutUserCannotOpenAuthenticatedDestinations() {
        val state = reduceToollyUiState(
            ToollyUiState.returningSignedOut(),
            ToollyUiEvent.MainDestinationSelected(ToollyDestination.LIBRARY),
        )

        assertEquals(ToollyDestination.SPLASH, state.destination)
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
        val documents = reduceToollyUiState(
            authenticated,
            ToollyUiEvent.MainDestinationSelected(ToollyDestination.LIBRARY),
        )
        val signedOut = reduceToollyUiState(documents, ToollyUiEvent.SignedOut)

        assertEquals(ToollyDestination.LIBRARY, documents.destination)
        assertEquals(ToollySessionState.SIGNED_OUT, signedOut.sessionState)
        assertEquals(ToollyDestination.WELCOME, signedOut.destination)
    }

    @Test
    fun negativeReviewPageCountIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            ToollyUiState.firstLaunch().copy(reviewPageCount = -1)
        }
    }
}
