package com.toolly.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.toolly.shared.model.DocumentListItem
import com.toolly.shared.model.ToollyAuthenticationMethod
import com.toolly.shared.model.ToollyDestination
import com.toolly.shared.model.ToollySessionState
import com.toolly.shared.model.ToollyUiActions
import com.toolly.shared.model.ToollyUiState
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.account_and_backup
import com.toolly.shared.resources.account_description
import com.toolly.shared.resources.all_documents
import com.toolly.shared.resources.app_name
import com.toolly.shared.resources.apple_sign_in
import com.toolly.shared.resources.back
import com.toolly.shared.resources.backup_optional
import com.toolly.shared.resources.create_account
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
import com.toolly.shared.resources.local_documents_notice
import com.toolly.shared.resources.no_documents
import com.toolly.shared.resources.phone_sign_in
import com.toolly.shared.resources.profile_description
import com.toolly.shared.resources.product_name
import com.toolly.shared.resources.review_page_count
import com.toolly.shared.resources.review_scan
import com.toolly.shared.resources.save
import com.toolly.shared.resources.scan
import com.toolly.shared.resources.scan_document
import com.toolly.shared.resources.scanned_document
import com.toolly.shared.resources.search
import com.toolly.shared.resources.search_documents
import com.toolly.shared.resources.search_subtitle
import com.toolly.shared.resources.sign_in
import com.toolly.shared.resources.sign_in_description
import com.toolly.shared.resources.sign_out
import com.toolly.shared.resources.skip
import com.toolly.shared.resources.splash_tagline
import com.toolly.shared.resources.toolly_mark
import com.toolly.shared.resources.tutorial_continue
import com.toolly.shared.resources.tutorial_organize_body
import com.toolly.shared.resources.tutorial_organize_title
import com.toolly.shared.resources.tutorial_privacy_body
import com.toolly.shared.resources.tutorial_privacy_title
import com.toolly.shared.resources.tutorial_scan_body
import com.toolly.shared.resources.tutorial_scan_title
import com.toolly.shared.resources.welcome_body
import com.toolly.shared.resources.welcome_headline
import com.toolly.shared.resources.welcome_sign_in_prompt
import com.toolly.shared.resources.working
import com.toolly.shared.resources.you
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ToollyApp(
    state: ToollyUiState,
    actions: ToollyUiActions,
    documentsContent: (@Composable () -> Unit)? = null,
    searchContent: (@Composable () -> Unit)? = null,
) {
    ToollyTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (state.destination) {
                ToollyDestination.SPLASH -> SplashScreen(actions)
                ToollyDestination.TUTORIAL -> TutorialScreen(state, actions)
                ToollyDestination.WELCOME -> WelcomeScreen(state, actions)
                ToollyDestination.SIGN_IN -> SignInScreen(state, actions)
                ToollyDestination.CREATE_PROFILE -> CreateProfileScreen(state, actions)
                ToollyDestination.PHONE_ENTRY -> PhoneEntryScreen(state, actions)
                ToollyDestination.OTP_VERIFICATION -> OtpVerificationScreen(state, actions)
                ToollyDestination.EMAIL_SIGN_IN -> EmailSignInScreen(state, actions)
                ToollyDestination.CREATE_ACCOUNT -> CreateAccountScreen(state, actions)
                ToollyDestination.RESET_PASSWORD -> ResetPasswordScreen(actions)
                ToollyDestination.PROFILE_COMPLETION -> ProfileCompletionScreen(state, actions)
                ToollyDestination.SESSION_ROUTING -> SessionRoutingScreen(actions)
                ToollyDestination.HOME,
                ToollyDestination.LIBRARY,
                ToollyDestination.SEARCH,
                ToollyDestination.PROFILE -> MainShell(state, actions, documentsContent, searchContent)
                ToollyDestination.CAPTURE_REVIEW -> ReviewScreen(state, actions)
                ToollyDestination.DOCUMENT_VIEWER -> ViewerScreen(state, actions)
                ToollyDestination.PRIVACY_CENTER -> PrivacyCenterScreen(
                    onOpenBackupSettings = actions::openBackupSettings,
                    onBack = actions::navigateBack,
                )
                ToollyDestination.BACKUP_CHOICE -> BackupChoiceScreen(
                    preferences = state.backupPreferences,
                    onPreferenceChanged = actions::setBackupPreference,
                    onEnabledChanged = actions::setBackupEnabled,
                    onBack = actions::navigateBack,
                )
            }
        }
    }
}

@Composable
private fun SplashScreen(actions: ToollyUiActions) {
    var stage by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(240)
        stage = 1
        delay(280)
        stage = 2
        delay(180)
        actions.finishSplash()
    }

    Box(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ToollySpacing.Medium),
        ) {
            ToollyMark(
                modifier = Modifier
                    .size(72.dp)
                    .scale(if (stage == 1) 1.08f else 1f)
                    .alpha(if (stage == 0) 0.22f else 1f),
            )
            if (stage >= 2) {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.displaySmall)
                Text(
                    stringResource(Res.string.splash_tagline),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun ToollyMark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.toolly_mark),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier,
    )
}

@Composable
private fun TutorialScreen(state: ToollyUiState, actions: ToollyUiActions) {
    val page = state.tutorialPageIndex
    val title = when (page) {
        0 -> Res.string.tutorial_scan_title
        1 -> Res.string.tutorial_organize_title
        else -> Res.string.tutorial_privacy_title
    }
    val body = when (page) {
        0 -> Res.string.tutorial_scan_body
        1 -> Res.string.tutorial_organize_body
        else -> Res.string.tutorial_privacy_body
    }

    ScreenColumn(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (page < ToollyUiState.TUTORIAL_PAGE_COUNT - 1) {
                TextButton(
                    onClick = actions::skipTutorial,
                    modifier = Modifier.heightIn(min = ToollySpacing.MinimumTarget),
                ) {
                    Text(stringResource(Res.string.skip))
                }
            } else {
                Spacer(modifier = Modifier.height(ToollySpacing.MinimumTarget))
            }
        }
        TutorialArtwork(page = page, description = stringResource(title))
        Text(
            stringResource(title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(body),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 360.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        TutorialPageIndicator(selectedPage = page)
        PrimaryButton(
            label = if (page == ToollyUiState.TUTORIAL_PAGE_COUNT - 1) {
                Res.string.get_started
            } else {
                Res.string.tutorial_continue
            },
            onClick = if (page == ToollyUiState.TUTORIAL_PAGE_COUNT - 1) {
                actions::completeTutorial
            } else {
                actions::nextTutorial
            },
        )
    }
}

@Composable
private fun TutorialArtwork(page: Int, description: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 260.dp, max = 320.dp)
            .semantics { contentDescription = description },
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(42.dp)) {
            when (page) {
                0 -> drawScanArtwork()
                1 -> drawOrganizeArtwork()
                else -> drawPrivacyArtwork()
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawScanArtwork() {
    val left = size.width * 0.18f
    val top = size.height * 0.12f
    val right = size.width * 0.82f
    val bottom = size.height * 0.88f
    drawRect(
        color = ToollyColors.PrimaryContainer,
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
    )
    drawLine(ToollyColors.Primary, Offset(left, top), Offset(right, top), 3.dp.toPx())
    drawLine(ToollyColors.Primary, Offset(right, top), Offset(right, bottom), 3.dp.toPx())
    drawLine(ToollyColors.Primary, Offset(right, bottom), Offset(left, bottom), 3.dp.toPx())
    drawLine(ToollyColors.Primary, Offset(left, bottom), Offset(left, top), 3.dp.toPx())
    listOf(
        Offset(left, top),
        Offset(right, top),
        Offset(right, bottom),
        Offset(left, bottom),
    ).forEach { drawCircle(ToollyColors.Primary, 7.dp.toPx(), it) }
    val lineLeft = size.width * 0.34f
    val lineRight = size.width * 0.69f
    repeat(3) { index ->
        drawLine(
            color = ToollyColors.Outline,
            start = Offset(lineLeft, size.height * (0.40f + index * 0.11f)),
            end = Offset(lineRight - index * 8.dp.toPx(), size.height * (0.40f + index * 0.11f)),
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrganizeArtwork() {
    val cardWidth = size.width * 0.62f
    val cardHeight = size.height * 0.76f
    val left = size.width * 0.27f
    val top = size.height * 0.08f
    drawRoundRect(
        color = ToollyColors.PrimaryContainer,
        topLeft = Offset(left - 28.dp.toPx(), top + 30.dp.toPx()),
        size = Size(cardWidth, cardHeight),
        cornerRadius = CornerRadius(14.dp.toPx()),
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(left, top),
        size = Size(cardWidth, cardHeight),
        cornerRadius = CornerRadius(14.dp.toPx()),
    )
    drawRoundRect(
        color = ToollyColors.Primary,
        topLeft = Offset(left, top),
        size = Size(cardWidth, cardHeight),
        cornerRadius = CornerRadius(14.dp.toPx()),
        style = Stroke(2.dp.toPx()),
    )
    val searchTop = size.height * 0.62f
    drawRoundRect(
        color = ToollyColors.CameraSurface,
        topLeft = Offset(size.width * 0.10f, searchTop),
        size = Size(size.width * 0.80f, size.height * 0.22f),
        cornerRadius = CornerRadius(30.dp.toPx()),
    )
    drawCircle(Color.White, 10.dp.toPx(), Offset(size.width * 0.23f, searchTop + size.height * 0.11f))
    drawLine(
        color = ToollyColors.Outline,
        start = Offset(size.width * 0.36f, searchTop + size.height * 0.11f),
        end = Offset(size.width * 0.70f, searchTop + size.height * 0.11f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPrivacyArtwork() {
    val plateWidth = size.width * 0.64f
    val plateHeight = size.height * 0.82f
    val plateLeft = (size.width - plateWidth) / 2
    val plateTop = (size.height - plateHeight) / 2
    drawRoundRect(
        color = ToollyColors.PrimaryContainer,
        topLeft = Offset(plateLeft, plateTop),
        size = Size(plateWidth, plateHeight),
        cornerRadius = CornerRadius(34.dp.toPx()),
    )
    drawRoundRect(
        color = ToollyColors.Primary,
        topLeft = Offset(plateLeft, plateTop),
        size = Size(plateWidth, plateHeight),
        cornerRadius = CornerRadius(34.dp.toPx()),
        style = Stroke(3.dp.toPx()),
    )
    val lockWidth = size.width * 0.36f
    val lockLeft = (size.width - lockWidth) / 2
    val lockTop = size.height * 0.48f
    drawArc(
        color = ToollyColors.Primary,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(size.width * 0.36f, size.height * 0.28f),
        size = Size(size.width * 0.28f, size.height * 0.34f),
        style = Stroke(3.dp.toPx(), cap = StrokeCap.Round),
    )
    drawRoundRect(
        color = ToollyColors.Primary,
        topLeft = Offset(lockLeft, lockTop),
        size = Size(lockWidth, size.height * 0.29f),
        cornerRadius = CornerRadius(14.dp.toPx()),
    )
    drawCircle(Color.White, 7.dp.toPx(), Offset(size.width / 2, lockTop + size.height * 0.14f))
}

@Composable
private fun TutorialPageIndicator(selectedPage: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small)) {
        repeat(ToollyUiState.TUTORIAL_PAGE_COUNT) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == selectedPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun WelcomeScreen(state: ToollyUiState, actions: ToollyUiActions) {
    ScreenColumn {
        Text(stringResource(Res.string.product_name), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(Res.string.welcome_sign_in_prompt),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 230.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(
                modifier = Modifier.padding(ToollySpacing.ExtraLarge),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    stringResource(Res.string.welcome_headline),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(ToollySpacing.Large))
                Text(
                    stringResource(Res.string.welcome_body),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(
            label = Res.string.get_started,
            onClick = actions::showSignIn,
        )
        Text(
            stringResource(Res.string.local_documents_notice),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        DevelopmentAccess(state, actions)
    }
}

@Composable
private fun SignInScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AccountScreen(
        state = state,
        title = Res.string.sign_in,
        description = Res.string.sign_in_description,
        actions = actions,
        footer = {
            TextButton(
                onClick = actions::showCreateProfile,
                modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
            ) {
                Text(stringResource(Res.string.create_account))
            }
        },
    )
}

@Composable
private fun CreateProfileScreen(state: ToollyUiState, actions: ToollyUiActions) {
    AccountScreen(
        state = state,
        title = Res.string.create_account,
        description = Res.string.create_profile_description,
        actions = actions,
    )
}

@Composable
private fun AccountScreen(
    state: ToollyUiState,
    title: StringResource,
    description: StringResource,
    actions: ToollyUiActions,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    ScreenColumn {
        ToollyMark(modifier = Modifier.size(72.dp))
        Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(ToollySpacing.Small))
        PrimaryButton(label = Res.string.phone_sign_in, onClick = {
            actions.authenticate(ToollyAuthenticationMethod.PHONE)
        })
        SecondaryButton(Res.string.email_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.EMAIL)
        }
        SecondaryButton(Res.string.google_sign_in) {
            actions.authenticate(ToollyAuthenticationMethod.GOOGLE)
        }
        if (state.appleSignInAvailable) {
            SecondaryButton(Res.string.apple_sign_in) {
                actions.authenticate(ToollyAuthenticationMethod.APPLE)
            }
        }
        DevelopmentAccess(state, actions)
        Spacer(modifier = Modifier.weight(1f))
        footer()
        TextButton(
            onClick = actions::backToWelcome,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            ToollyBackIcon(iconSize = 18.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.back))
        }
    }
}

@Composable
private fun DevelopmentAccess(state: ToollyUiState, actions: ToollyUiActions) {
    if (state.developmentAccessAvailable) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(ToollySpacing.Large),
                verticalArrangement = Arrangement.spacedBy(ToollySpacing.Small),
            ) {
                Text(
                    stringResource(Res.string.development_access_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = actions::useDevelopmentAccess,
                    modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
                ) {
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
    searchContent: (@Composable () -> Unit)?,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        if (maxWidth >= 600.dp) {
            Row(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                ExpandedNavigation(state, actions)
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    MainDestinationContent(state, actions, documentsContent, searchContent)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    MainDestinationContent(state, actions, documentsContent, searchContent)
                }
                CompactNavigation(state, actions)
            }
        }
    }
}

@Composable
private fun MainDestinationContent(
    state: ToollyUiState,
    actions: ToollyUiActions,
    documentsContent: (@Composable () -> Unit)?,
    searchContent: (@Composable () -> Unit)?,
) {
    when (state.destination) {
        ToollyDestination.HOME -> HomeScreen(state, actions)
        ToollyDestination.LIBRARY -> {
            if (documentsContent == null) LibraryScreen(state, actions)
            else documentsContent()
        }
        ToollyDestination.SEARCH -> {
            if (searchContent == null) SearchScreen()
            else searchContent()
        }
        ToollyDestination.PROFILE -> ProfileScreen(state, actions)
        else -> Unit
    }
}

@Composable
private fun HomeScreen(state: ToollyUiState, actions: ToollyUiActions) {
    ScreenColumn(applySafeInsets = false) {
        Text(stringResource(Res.string.product_name), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.home_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryButton(
            label = Res.string.scan_document,
            enabled = !state.busy,
            onClick = actions::scanDocument,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Column(
                modifier = Modifier.padding(ToollySpacing.Large),
                verticalArrangement = Arrangement.spacedBy(ToollySpacing.Small),
            ) {
                Text(
                    stringResource(Res.string.backup_optional),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(Res.string.local_documents_notice),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (state.documents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(Res.string.no_documents),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            DocumentList(state.documents, actions)
        }
    }
}

@Composable
private fun LibraryScreen(state: ToollyUiState, actions: ToollyUiActions) {
    ScreenColumn(applySafeInsets = false) {
        Text(stringResource(Res.string.documents), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.library_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PrimaryButton(
            label = Res.string.scan_document,
            onClick = actions::scanDocument,
            enabled = !state.busy,
        )
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
private fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    ScreenColumn(applySafeInsets = false) {
        Text(stringResource(Res.string.product_name), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.search_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(stringResource(Res.string.search_documents)) },
            leadingIcon = { ToollySearchIcon() },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.PrimaryActionHeight),
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Text(
                if (query.isBlank()) {
                    stringResource(Res.string.all_documents)
                } else {
                    stringResource(Res.string.no_documents)
                },
                modifier = Modifier.padding(ToollySpacing.Large),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileScreen(state: ToollyUiState, actions: ToollyUiActions) {
    ScreenColumn(applySafeInsets = false) {
        Text(stringResource(Res.string.you), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.profile_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier = Modifier.fillMaxWidth().clickable(onClick = actions::openPrivacyCenter),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
            Column(
                modifier = Modifier.padding(ToollySpacing.Large),
                verticalArrangement = Arrangement.spacedBy(ToollySpacing.Small),
            ) {
                Text(
                    stringResource(Res.string.account_and_backup),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(Res.string.account_description),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        when (state.sessionState) {
            ToollySessionState.DEVELOPMENT,
            ToollySessionState.AUTHENTICATED -> {
                if (state.sessionState == ToollySessionState.DEVELOPMENT) {
                    Text(
                        stringResource(Res.string.development_access_description),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                SecondaryButton(label = Res.string.sign_out, onClick = actions::signOut)
            }
            ToollySessionState.SIGNED_OUT -> Unit
        }
    }
}

@Composable
private fun CompactNavigation(state: ToollyUiState, actions: ToollyUiActions) {
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        MainNavigationItem(
            Res.string.home,
            state.destination == ToollyDestination.HOME,
            actions::openHome,
            icon = { ToollyHomeIcon() },
        )
        MainNavigationItem(
            Res.string.documents,
            state.destination == ToollyDestination.LIBRARY,
            actions::openLibrary,
            icon = { ToollyLibraryIcon() },
        )
        MainNavigationItem(Res.string.scan, false, actions::scanDocument, primary = true)
        MainNavigationItem(
            Res.string.search,
            state.destination == ToollyDestination.SEARCH,
            actions::openSearch,
            icon = { ToollySearchIcon() },
        )
        MainNavigationItem(
            Res.string.you,
            state.destination == ToollyDestination.PROFILE,
            actions::openProfile,
            icon = { ToollyProfileIcon() },
        )
    }
}

@Composable
private fun RowScope.MainNavigationItem(
    label: StringResource,
    selected: Boolean,
    onClick: () -> Unit,
    primary: Boolean = false,
    icon: @Composable () -> Unit = {},
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            if (primary) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val plusColor = MaterialTheme.colorScheme.onPrimary
                        Canvas(modifier = Modifier.size(12.dp)) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = plusColor,
                                start = Offset(center.x, 0f),
                                end = Offset(center.x, size.height),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round,
                            )
                            drawLine(
                                color = plusColor,
                                start = Offset(0f, center.y),
                                end = Offset(size.width, center.y),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round,
                            )
                        }
                    }
                }
            } else {
                icon()
            }
        },
        label = { Text(stringResource(label)) },
    )
}

@Composable
private fun RowScope.ExpandedNavigation(state: ToollyUiState, actions: ToollyUiActions) {
    Surface(
        modifier = Modifier.width(136.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(ToollySpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(ToollySpacing.Small),
        ) {
            ToollyMark(modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.height(ToollySpacing.Medium))
            ExpandedNavigationItem(
                Res.string.home,
                state.destination == ToollyDestination.HOME,
                actions::openHome,
                icon = { ToollyHomeIcon() },
            )
            ExpandedNavigationItem(
                Res.string.documents,
                state.destination == ToollyDestination.LIBRARY,
                actions::openLibrary,
                icon = { ToollyLibraryIcon() },
            )
            PrimaryButton(label = Res.string.scan, onClick = actions::scanDocument)
            ExpandedNavigationItem(
                Res.string.search,
                state.destination == ToollyDestination.SEARCH,
                actions::openSearch,
                icon = { ToollySearchIcon() },
            )
            ExpandedNavigationItem(
                Res.string.you,
                state.destination == ToollyDestination.PROFILE,
                actions::openProfile,
                icon = { ToollyProfileIcon() },
            )
        }
    }
}

@Composable
private fun ExpandedNavigationItem(
    label: StringResource,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Text(stringResource(label))
        }
    }
}

@Composable
private fun ColumnScope.DocumentList(documents: List<DocumentListItem>, actions: ToollyUiActions) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().weight(1f),
        verticalArrangement = Arrangement.spacedBy(ToollySpacing.Medium),
    ) {
        items(documents, key = { it.id.value }) { document ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { actions.openDocument(document.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    modifier = Modifier.padding(ToollySpacing.Large),
                    verticalArrangement = Arrangement.spacedBy(ToollySpacing.ExtraSmall),
                ) {
                    Text(
                        stringResource(Res.string.scanned_document),
                        style = MaterialTheme.typography.titleMedium,
                    )
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
private fun ReviewScreen(state: ToollyUiState, actions: ToollyUiActions) {
    ScreenColumn {
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
            horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Medium),
        ) {
            OutlinedButton(
                onClick = actions::discardCapture,
                enabled = !state.busy,
                modifier = Modifier.weight(1f).heightIn(min = ToollySpacing.PrimaryActionHeight),
            ) {
                Text(stringResource(Res.string.discard))
            }
            Button(
                onClick = actions::saveCapture,
                enabled = !state.busy,
                modifier = Modifier.weight(1f).heightIn(min = ToollySpacing.PrimaryActionHeight),
            ) {
                Text(stringResource(Res.string.save))
            }
        }
    }
}

@Composable
private fun ViewerScreen(state: ToollyUiState, actions: ToollyUiActions) {
    ScreenColumn {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = actions::navigateBack,
                modifier = Modifier.heightIn(min = ToollySpacing.MinimumTarget),
            ) {
                ToollyBackIcon(iconSize = 18.dp)
                Spacer(modifier = Modifier.width(4.dp))
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
internal fun PrimaryButton(
    label: StringResource,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.PrimaryActionHeight),
        shape = MaterialTheme.shapes.small,
    ) {
        if (!enabled) {
            val workingDescription = stringResource(Res.string.working)
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp).semantics {
                    contentDescription = workingDescription
                },
                strokeWidth = 2.dp,
            )
        } else {
            Text(stringResource(label))
        }
    }
}

@Composable
internal fun SecondaryButton(label: StringResource, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.PrimaryActionHeight),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(stringResource(label))
    }
}

@Composable
internal fun ScreenColumn(
    applySafeInsets: Boolean = true,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val maximumWidth = if (maxWidth >= 600.dp) 640.dp else 520.dp
        val baseModifier = Modifier.fillMaxSize().widthIn(max = maximumWidth)
        Column(
            modifier = if (applySafeInsets) {
                baseModifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(ToollySpacing.Screen)
            } else {
                baseModifier.padding(ToollySpacing.Screen)
            },
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(ToollySpacing.Large),
            content = content,
        )
    }
}
