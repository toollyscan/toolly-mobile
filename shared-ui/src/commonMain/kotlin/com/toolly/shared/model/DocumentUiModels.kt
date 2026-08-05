package com.toolly.shared.model

import kotlin.jvm.JvmInline

@JvmInline
value class DocumentUiId(val value: String)

data class DocumentListItem(
    val id: DocumentUiId,
    val pageCount: Int,
)

enum class ToollySessionState {
    SIGNED_OUT,
    LOCAL,
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
    HOME,
    LIBRARY,
    SEARCH,
    PROFILE,
    CAPTURE_REVIEW,
    DOCUMENT_VIEWER,
}

sealed interface ToollyUiEvent {
    data object SplashFinished : ToollyUiEvent
    data object TutorialAdvanced : ToollyUiEvent
    data object TutorialSkipped : ToollyUiEvent
    data object TutorialCompleted : ToollyUiEvent
    data object SignInSelected : ToollyUiEvent
    data object CreateProfileSelected : ToollyUiEvent
    data object BackToWelcome : ToollyUiEvent
    data class LocalSessionStarted(val destination: ToollyDestination) : ToollyUiEvent
    data object AuthenticationSucceeded : ToollyUiEvent
    data object DevelopmentAccessGranted : ToollyUiEvent
    data class MainDestinationSelected(val destination: ToollyDestination) : ToollyUiEvent
    data object SignedOut : ToollyUiEvent
    data object CaptureStarted : ToollyUiEvent
    data class CaptureCompleted(val pageCount: Int) : ToollyUiEvent {
        init {
            require(pageCount > 0)
        }
    }
    data object CaptureCancelled : ToollyUiEvent
    data object CaptureFailed : ToollyUiEvent
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
) {
    init {
        require(tutorialPageIndex in 0 until TUTORIAL_PAGE_COUNT)
        require(reviewPageCount >= 0)
        require(documents.all { it.pageCount > 0 })
        require(
            sessionState != ToollySessionState.SIGNED_OUT ||
                destination !in productDestinations,
        )
    }

    companion object {
        const val TUTORIAL_PAGE_COUNT = 3

        private val productDestinations = setOf(
            ToollyDestination.HOME,
            ToollyDestination.LIBRARY,
            ToollyDestination.SEARCH,
            ToollyDestination.PROFILE,
            ToollyDestination.CAPTURE_REVIEW,
            ToollyDestination.DOCUMENT_VIEWER,
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
        if (
            state.destination !in setOf(ToollyDestination.WELCOME, ToollyDestination.PROFILE) ||
            state.sessionState in setOf(
                ToollySessionState.AUTHENTICATED,
                ToollySessionState.DEVELOPMENT,
            )
        ) state
        else state.copy(destination = ToollyDestination.SIGN_IN)
    }

    ToollyUiEvent.CreateProfileSelected -> {
        if (
            state.destination !in setOf(
                ToollyDestination.WELCOME,
                ToollyDestination.SIGN_IN,
                ToollyDestination.PROFILE,
            ) ||
            state.sessionState in setOf(
                ToollySessionState.AUTHENTICATED,
                ToollySessionState.DEVELOPMENT,
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
            destination = if (state.sessionState == ToollySessionState.LOCAL) {
                ToollyDestination.PROFILE
            } else {
                ToollyDestination.WELCOME
            },
        )
    }

    is ToollyUiEvent.LocalSessionStarted -> {
        if (
            state.destination != ToollyDestination.WELCOME ||
            event.destination !in setOf(ToollyDestination.HOME, ToollyDestination.LIBRARY)
        ) {
            state
        } else {
            state.copy(
                destination = event.destination,
                sessionState = ToollySessionState.LOCAL,
            )
        }
    }

    ToollyUiEvent.AuthenticationSucceeded -> {
        if (
            state.destination !in setOf(
                ToollyDestination.SIGN_IN,
                ToollyDestination.CREATE_PROFILE,
            )
        ) state
        else state.copy(
            destination = ToollyDestination.HOME,
            sessionState = ToollySessionState.AUTHENTICATED,
        )
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
            state.busy ||
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

    ToollyUiEvent.CaptureStarted -> {
        if (
            state.sessionState == ToollySessionState.SIGNED_OUT ||
            state.busy ||
            state.destination !in setOf(ToollyDestination.HOME, ToollyDestination.LIBRARY)
        ) {
            state
        } else {
            state.copy(
                busy = true,
                reviewPageCount = 0,
                selectedDocumentId = null,
            )
        }
    }

    is ToollyUiEvent.CaptureCompleted -> {
        if (!state.busy) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.CAPTURE_REVIEW,
                reviewPageCount = event.pageCount,
                busy = false,
            )
        }
    }

    ToollyUiEvent.CaptureCancelled,
    ToollyUiEvent.CaptureFailed -> {
        if (!state.busy) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.LIBRARY,
                reviewPageCount = 0,
                busy = false,
            )
        }
    }

    ToollyUiEvent.CaptureDiscarded -> {
        if (state.destination != ToollyDestination.CAPTURE_REVIEW) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.LIBRARY,
                reviewPageCount = 0,
                busy = false,
            )
        }
    }

    ToollyUiEvent.SignedOut -> {
        if (state.sessionState == ToollySessionState.LOCAL) {
            state
        } else {
            state.copy(
                destination = ToollyDestination.WELCOME,
                sessionState = ToollySessionState.SIGNED_OUT,
                selectedDocumentId = null,
                reviewPageCount = 0,
                busy = false,
            )
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
    fun continueLocally(destination: ToollyDestination)
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
}
