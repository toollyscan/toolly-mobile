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
    TOOLS,
    PROFILE,
    CAPTURE_REVIEW,
    DOCUMENT_VIEWER,
}

sealed interface ToollyUiEvent {
    data object SplashFinished : ToollyUiEvent
    data object TutorialCompleted : ToollyUiEvent
    data object SignInSelected : ToollyUiEvent
    data object CreateProfileSelected : ToollyUiEvent
    data object BackToWelcome : ToollyUiEvent
    data object AuthenticationSucceeded : ToollyUiEvent
    data object DevelopmentAccessGranted : ToollyUiEvent
    data class MainDestinationSelected(val destination: ToollyDestination) : ToollyUiEvent
    data object SignedOut : ToollyUiEvent
}

data class ToollyUiState(
    val destination: ToollyDestination,
    val tutorialCompleted: Boolean,
    val sessionState: ToollySessionState,
    val developmentAccessAvailable: Boolean,
    val documents: List<DocumentListItem>,
    val selectedDocumentId: DocumentUiId?,
    val reviewPageCount: Int,
    val busy: Boolean,
) {
    init {
        require(reviewPageCount >= 0)
        require(documents.all { it.pageCount > 0 })
        require(
            sessionState != ToollySessionState.SIGNED_OUT ||
                destination !in authenticatedDestinations,
        )
    }

    companion object {
        private val authenticatedDestinations = setOf(
            ToollyDestination.HOME,
            ToollyDestination.LIBRARY,
            ToollyDestination.TOOLS,
            ToollyDestination.PROFILE,
            ToollyDestination.CAPTURE_REVIEW,
            ToollyDestination.DOCUMENT_VIEWER,
        )

        fun firstLaunch(developmentAccessAvailable: Boolean = false): ToollyUiState = ToollyUiState(
            destination = ToollyDestination.SPLASH,
            tutorialCompleted = false,
            sessionState = ToollySessionState.SIGNED_OUT,
            developmentAccessAvailable = developmentAccessAvailable,
            documents = emptyList(),
            selectedDocumentId = null,
            reviewPageCount = 0,
            busy = false,
        )

        fun returningSignedOut(
            developmentAccessAvailable: Boolean = false,
        ): ToollyUiState = firstLaunch(developmentAccessAvailable).copy(
            tutorialCompleted = true,
        )

        fun empty(): ToollyUiState = firstLaunch()
    }
}

fun reduceToollyUiState(
    state: ToollyUiState,
    event: ToollyUiEvent,
): ToollyUiState = when (event) {
    ToollyUiEvent.SplashFinished -> {
        if (state.destination != ToollyDestination.SPLASH) state
        else state.copy(
            destination = if (state.tutorialCompleted) {
                ToollyDestination.WELCOME
            } else {
                ToollyDestination.TUTORIAL
            },
        )
    }

    ToollyUiEvent.TutorialCompleted -> {
        if (state.destination != ToollyDestination.TUTORIAL) state
        else state.copy(
            destination = ToollyDestination.WELCOME,
            tutorialCompleted = true,
        )
    }

    ToollyUiEvent.SignInSelected -> {
        if (state.destination != ToollyDestination.WELCOME) state
        else state.copy(destination = ToollyDestination.SIGN_IN)
    }

    ToollyUiEvent.CreateProfileSelected -> {
        if (state.destination != ToollyDestination.WELCOME) state
        else state.copy(destination = ToollyDestination.CREATE_PROFILE)
    }

    ToollyUiEvent.BackToWelcome -> {
        if (
            state.sessionState != ToollySessionState.SIGNED_OUT ||
            state.destination !in setOf(
                ToollyDestination.SIGN_IN,
                ToollyDestination.CREATE_PROFILE,
            )
        ) state
        else state.copy(destination = ToollyDestination.WELCOME)
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
            event.destination !in setOf(
                ToollyDestination.HOME,
                ToollyDestination.LIBRARY,
                ToollyDestination.TOOLS,
                ToollyDestination.PROFILE,
            )
        ) {
            state
        } else {
            state.copy(destination = event.destination)
        }
    }

    ToollyUiEvent.SignedOut -> state.copy(
        destination = ToollyDestination.WELCOME,
        sessionState = ToollySessionState.SIGNED_OUT,
        selectedDocumentId = null,
        reviewPageCount = 0,
        busy = false,
    )
}

interface ToollyUiActions {
    fun finishSplash()
    fun completeTutorial()
    fun showSignIn()
    fun showCreateProfile()
    fun backToWelcome()
    fun authenticate(method: ToollyAuthenticationMethod)
    fun useDevelopmentAccess()
    fun openHome()
    fun openLibrary()
    fun openTools()
    fun openProfile()
    fun signOut()
    fun scanDocument()
    fun openDocument(id: DocumentUiId)
    fun discardCapture()
    fun saveCapture()
    fun navigateBack()
}
