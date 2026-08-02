package com.toolly.shared.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.toolly.shared.model.DocumentListItem
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollySessionState
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.app_name
import com.toolly.shared.resources.apple_sign_in
import com.toolly.shared.resources.back
import com.toolly.shared.resources.create_profile
import com.toolly.shared.resources.create_profile_description
import com.toolly.shared.resources.development_access
import com.toolly.shared.resources.development_access_description
import com.toolly.shared.resources.discard
import com.toolly.shared.resources.document_page_count
import com.toolly.shared.resources.documents
import com.toolly.shared.resources.email_sign_in
import com.toolly.shared.resources.get_started
import com.toolly.shared.resources.google_sign_in
import com.toolly.shared.resources.home
import com.toolly.shared.resources.home_description
import com.toolly.shared.resources.library_subtitle
import com.toolly.shared.resources.local_private
import com.toolly.shared.resources.local_private_description
import com.toolly.shared.resources.no_documents
import com.toolly.shared.resources.offline_ready
import com.toolly.shared.resources.offline_ready_description
import com.toolly.shared.resources.phone_sign_in
import com.toolly.shared.resources.profile
import com.toolly.shared.resources.profile_description
import com.toolly.shared.resources.review_page_count
import com.toolly.shared.resources.review_scan
import com.toolly.shared.resources.save
import com.toolly.shared.resources.scan_document
import com.toolly.shared.resources.scanned_document
import com.toolly.shared.resources.secure_by_design
import com.toolly.shared.resources.secure_by_design_description
import com.toolly.shared.resources.sign_in
import com.toolly.shared.resources.sign_in_description
import com.toolly.shared.resources.sign_out
import com.toolly.shared.resources.splash_description
import com.toolly.shared.resources.tools
import com.toolly.shared.resources.tools_description
import com.toolly.shared.resources.tutorial_title
import com.toolly.shared.resources.welcome_description
import com.toolly.shared.resources.welcome_title
import com.toolly.shared.resources.working
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToollyApp(
    state: ToollyUiState,
    actions: ToollyUiActions,
    documentsContent: (@Composable () -> Unit)? = null,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (state.destination) {
                ToollyDestination.SPLASH -> SplashScreen(actions)
                ToollyDestination.TUTORIAL -> TutorialScreen(actions)
                ToollyDestination.WELCOME -> WelcomeScreen(state, actions)
                ToollyDestination.SIGN_IN -> SignInScreen(state, actions)
                ToollyDestination.CREATE_PROFILE -> CreateProfileScreen(state, actions)
                ToollyDestination.HOME,
                ToollyDestination.LIBRARY,
                ToollyDestination.TOOLS,
                ToollyDestination.PROFILE -> MainShell(state, actions, documentsContent)
                ToollyDestination.CAPTURE_REVIEW -> ReviewScreen(state, actions)
                ToollyDestination.DOCUMENT_VIEWER -> ViewerScreen(state, actions)
            }
        }
    }
}

@Composable
private fun SplashScreen(actions: ToollyUiActions) {
    LaunchedEffect(Unit) { actions.finishSplash() }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(Res.string.splash_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TutorialScreen(actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.tutorial_title), style = MaterialTheme.typography.headlineMedium)
        TutorialCard(Res.string.local_private, Res.string.local_private_description)
        TutorialCard(Res.string.offline_ready, Res.string.offline_ready_description)
        TutorialCard(Res.string.secure_by_design, Res.string.secure_by_design_description)
        Box(modifier = Modifier.weight(1f))
        Button(
            onClick = actions::completeTutorial,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.get_started))
        }
    }
}

@Composable
private fun TutorialCard(
    titleResource: org.jetbrains.compose.resources.StringResource,
    bodyResource: org.jetbrains.compose.resources.StringResource,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(titleResource), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(bodyResource),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WelcomeScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.welcome_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(modifier = Modifier.weight(1f))
        Button(onClick = actions::showSignIn, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.sign_in))
        }
        OutlinedButton(onClick = actions::showCreateProfile, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.create_profile))
        }
        DevelopmentAccess(state, actions)
    }
}

@Composable
private fun SignInScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.sign_in), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.sign_in_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AuthenticationButton(Res.string.phone_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.PHONE)
        }
        AuthenticationButton(Res.string.email_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.EMAIL)
        }
        AuthenticationButton(Res.string.google_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.GOOGLE)
        }
        AuthenticationButton(Res.string.apple_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.APPLE)
        }
        DevelopmentAccess(state, actions)
        Box(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = actions::backToWelcome, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.back))
        }
    }
}

@Composable
private fun CreateProfileScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.create_profile), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.create_profile_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AuthenticationButton(Res.string.phone_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.PHONE)
        }
        AuthenticationButton(Res.string.email_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.EMAIL)
        }
        AuthenticationButton(Res.string.google_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.GOOGLE)
        }
        AuthenticationButton(Res.string.apple_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.APPLE)
        }
        DevelopmentAccess(state, actions)
        Box(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = actions::backToWelcome, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.back))
        }
    }
}

@Composable
private fun AuthenticationButton(
    label: org.jetbrains.compose.resources.StringResource,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(label))
    }
}

@Composable
private fun DevelopmentAccess(state: ToollyUiState, actions: ToollyUiActions) {
    if (state.developmentAccessAvailable) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(Res.string.development_access_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = actions::useDevelopmentAccess, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.development_access))
                }
            }
        }
    }
}

@Composable
private fun MainShell(
    state: ToollyUiState,
    actions: ToollyUiActions,
    documentsContent: (@Composable () -> Unit)?,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when (state.destination) {
                ToollyDestination.HOME -> HomeScreen(state, actions)
                ToollyDestination.LIBRARY -> {
                    if (documentsContent == null) LibraryScreen(state, actions)
                    else documentsContent()
                }
                ToollyDestination.TOOLS -> ToolsScreen()
                ToollyDestination.PROFILE -> ProfileScreen(state, actions)
                else -> Unit
            }
        }
        MainNavigation(state, actions)
    }
}

@Composable
private fun HomeScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.home), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.home_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = actions::scanDocument,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.scan_document))
        }
        if (state.documents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(Res.string.no_documents))
            }
        } else {
            DocumentList(state.documents, actions)
        }
    }
}

@Composable
private fun MainNavigation(state: ToollyUiState, actions: ToollyUiActions) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        MainNavigationItem(Res.string.home, state.destination == ToollyDestination.HOME, actions::openHome)
        MainNavigationItem(Res.string.documents, state.destination == ToollyDestination.LIBRARY, actions::openLibrary)
        MainNavigationItem(Res.string.tools, state.destination == ToollyDestination.TOOLS, actions::openTools)
        MainNavigationItem(Res.string.profile, state.destination == ToollyDestination.PROFILE, actions::openProfile)
    }
}

@Composable
private fun RowScope.MainNavigationItem(
    label: org.jetbrains.compose.resources.StringResource,
    selected: Boolean,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Box(modifier = Modifier.size(1.dp)) },
        label = { Text(stringResource(label)) },
    )
}

@Composable
private fun LibraryScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.documents), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.library_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = actions::scanDocument,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.busy) {
                val description = stringResource(Res.string.working)
                CircularProgressIndicator(
                    modifier = Modifier.semantics { contentDescription = description },
                    strokeWidth = 2.dp,
                )
            } else {
                Text(stringResource(Res.string.scan_document))
            }
        }
        if (state.documents.isEmpty() && !state.busy) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(Res.string.no_documents))
            }
        } else {
            DocumentList(state.documents, actions)
        }
    }
}

@Composable
private fun ColumnScope.DocumentList(documents: List<DocumentListItem>, actions: ToollyUiActions) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(documents, key = { it.id.value }) { document ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { actions.openDocument(document.id) },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(Res.string.scanned_document))
                    Text(
                        pluralStringResource(
                            Res.plurals.document_page_count,
                            document.pageCount,
                            document.pageCount,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolsScreen() {
    AdaptiveContent {
        Text(stringResource(Res.string.tools), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.tools_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.profile), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.profile_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (state.sessionState == ToollySessionState.DEVELOPMENT) {
            Text(
                stringResource(Res.string.development_access_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(modifier = Modifier.weight(1f))
        OutlinedButton(onClick = actions::signOut, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.sign_out))
        }
    }
}

@Composable
private fun ReviewScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Text(stringResource(Res.string.review_scan), style = MaterialTheme.typography.headlineSmall)
        Text(
            pluralStringResource(
                Res.plurals.review_page_count,
                state.reviewPageCount,
                state.reviewPageCount,
            ),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = actions::discardCapture,
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.discard))
            }
            Button(
                onClick = actions::saveCapture,
                enabled = !state.busy,
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }
}

@Composable
private fun ViewerScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AdaptiveContent {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = actions::navigateBack) {
                Text(stringResource(Res.string.back))
            }
            Text(stringResource(Res.string.scanned_document), style = MaterialTheme.typography.headlineSmall)
        }
        val selected = state.documents.firstOrNull { it.id == state.selectedDocumentId }
        selected?.let {
            Text(
                pluralStringResource(
                    Res.plurals.document_page_count,
                    it.pageCount,
                    it.pageCount,
                ),
            )
        }
        Box(modifier = Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun AdaptiveContent(content: @Composable ColumnScope.() -> Unit) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val maximumWidth = if (maxWidth >= 600.dp) 920.dp else 640.dp
        Column(
            modifier = Modifier.fillMaxSize().widthIn(max = maximumWidth).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}
