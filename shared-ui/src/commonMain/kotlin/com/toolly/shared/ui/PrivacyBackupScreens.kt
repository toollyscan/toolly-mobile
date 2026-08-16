package com.toolly.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.toolly.shared.model.BackupPreferenceKind
import com.toolly.shared.model.BackupPreferences
import com.toolly.shared.model.BackupProvider
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.back
import com.toolly.shared.resources.backup_auto_backup_description
import com.toolly.shared.resources.backup_auto_backup_label
import com.toolly.shared.resources.backup_choice_title
import com.toolly.shared.resources.backup_delete_cloud_copy_description
import com.toolly.shared.resources.backup_delete_cloud_copy_label
import com.toolly.shared.resources.backup_e2e_encryption_description
import com.toolly.shared.resources.backup_e2e_encryption_label
import com.toolly.shared.resources.backup_policy_title
import com.toolly.shared.resources.backup_provider_google_drive_description
import com.toolly.shared.resources.backup_provider_google_drive_label
import com.toolly.shared.resources.backup_provider_icloud_description
import com.toolly.shared.resources.backup_provider_icloud_label
import com.toolly.shared.resources.backup_provider_local_only_badge
import com.toolly.shared.resources.backup_provider_local_only_description
import com.toolly.shared.resources.backup_provider_local_only_label
import com.toolly.shared.resources.backup_settings_button
import com.toolly.shared.resources.backup_wifi_only_description
import com.toolly.shared.resources.backup_wifi_only_label
import com.toolly.shared.resources.continue_button
import com.toolly.shared.resources.privacy_center_backed_up_description_empty
import com.toolly.shared.resources.privacy_center_backed_up_title
import com.toolly.shared.resources.privacy_center_needs_attention_description_empty
import com.toolly.shared.resources.privacy_center_needs_attention_title
import com.toolly.shared.resources.privacy_center_only_on_device_description
import com.toolly.shared.resources.privacy_center_only_on_device_title
import com.toolly.shared.resources.privacy_center_title
import org.jetbrains.compose.resources.stringResource

/**
 * Privacy/backup overview (wireframe `6.1/6.3 Privacy center`). Only "Only on this device",
 * "Backed up" and "Needs attention" are shown -- the wireframe's fourth row ("Shared") is dropped
 * because Toolly has no multi-user sharing/collaboration feature anywhere in its architecture
 * (export via the OS share sheet is one-way and not a tracked "shared with" state). "Backed up"
 * and "Needs attention" show honest empty-state copy rather than fabricated counts, since Phase 5
 * (optional cloud backup) isn't implemented yet -- see [BackupPreferences]'s doc.
 */
@Composable
fun PrivacyCenterScreen(onOpenBackupSettings: () -> Unit, onBack: () -> Unit) {
    ScreenColumn {
        Text(stringResource(Res.string.privacy_center_title), style = MaterialTheme.typography.headlineMedium)
        PrivacyStatusRow(
            dotColor = MaterialTheme.colorScheme.outline,
            title = stringResource(Res.string.privacy_center_only_on_device_title),
            description = stringResource(Res.string.privacy_center_only_on_device_description),
        )
        PrivacyStatusRow(
            dotColor = ToollyColors.Positive,
            title = stringResource(Res.string.privacy_center_backed_up_title),
            description = stringResource(Res.string.privacy_center_backed_up_description_empty),
        )
        PrivacyStatusRow(
            dotColor = ToollyColors.Positive,
            title = stringResource(Res.string.privacy_center_needs_attention_title),
            description = stringResource(Res.string.privacy_center_needs_attention_description_empty),
        )
        SecondaryButton(Res.string.backup_settings_button, onClick = onOpenBackupSettings)
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            ToollyBackIcon(iconSize = 18.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.back))
        }
    }
}

@Composable
private fun PrivacyStatusRow(dotColor: Color, title: String, description: String) {
    ToollyCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Medium),
        ) {
            Box(modifier = Modifier.size(32.dp).background(dotColor.copy(alpha = 0.35f), CircleShape))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Backup provider picker (wireframe `6.2 Backup choice`). Selecting a provider only updates local,
 * presentation-only [BackupPreferences] state -- see that type's doc for why (Phase 5 gate). No
 * provider is contacted; "Continue" advances to the `6.4 Backup policy` screen.
 */
@Composable
fun BackupChoiceScreen(
    selectedProvider: BackupProvider,
    onProviderSelected: (BackupProvider) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    ScreenColumn {
        Text(stringResource(Res.string.backup_choice_title), style = MaterialTheme.typography.headlineMedium)
        ToollyRadioRow(
            title = stringResource(Res.string.backup_provider_icloud_label),
            description = stringResource(Res.string.backup_provider_icloud_description),
            selected = selectedProvider == BackupProvider.ICLOUD,
            onClick = { onProviderSelected(BackupProvider.ICLOUD) },
        )
        ToollyRadioRow(
            title = "${stringResource(Res.string.backup_provider_local_only_label)} ${stringResource(Res.string.backup_provider_local_only_badge)}",
            description = stringResource(Res.string.backup_provider_local_only_description),
            selected = selectedProvider == BackupProvider.LOCAL_ONLY,
            onClick = { onProviderSelected(BackupProvider.LOCAL_ONLY) },
        )
        ToollyRadioRow(
            title = stringResource(Res.string.backup_provider_google_drive_label),
            description = stringResource(Res.string.backup_provider_google_drive_description),
            selected = selectedProvider == BackupProvider.GOOGLE_DRIVE,
            onClick = { onProviderSelected(BackupProvider.GOOGLE_DRIVE) },
        )
        PrimaryButton(label = Res.string.continue_button, onClick = onContinue)
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            ToollyBackIcon(iconSize = 18.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.back))
        }
    }
}

/**
 * Backup policy toggles (wireframe `6.4 Backup policy`). Every value here is local,
 * presentation-only [BackupPreferences] state -- see that type's doc for why (Phase 5 gate). The
 * bottom "Backup settings" action commits [BackupPreferences.enabled] and returns to the privacy
 * center, matching the wireframe's own button label for this screen's single action.
 */
@Composable
fun BackupPolicyScreen(
    preferences: BackupPreferences,
    onPreferenceChanged: (BackupPreferenceKind, Boolean) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    ScreenColumn {
        Text(stringResource(Res.string.backup_policy_title), style = MaterialTheme.typography.headlineMedium)
        ToollySwitchRow(
            title = stringResource(Res.string.backup_auto_backup_label),
            description = stringResource(Res.string.backup_auto_backup_description),
            checked = preferences.autoBackupNewScans,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.AUTO_BACKUP_NEW_SCANS, it) },
        )
        ToollySwitchRow(
            title = stringResource(Res.string.backup_wifi_only_label),
            description = stringResource(Res.string.backup_wifi_only_description),
            checked = preferences.wifiOnly,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.WIFI_ONLY, it) },
        )
        ToollySwitchRow(
            title = stringResource(Res.string.backup_e2e_encryption_label),
            description = stringResource(Res.string.backup_e2e_encryption_description),
            checked = preferences.endToEndEncryption,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.END_TO_END_ENCRYPTION, it) },
        )
        ToollySwitchRow(
            title = stringResource(Res.string.backup_delete_cloud_copy_label),
            description = stringResource(Res.string.backup_delete_cloud_copy_description),
            checked = preferences.deleteCloudCopyOnLocalDelete,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.DELETE_CLOUD_COPY_ON_LOCAL_DELETE, it) },
        )
        PrimaryButton(label = Res.string.backup_settings_button, onClick = onConfirm)
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        ) {
            ToollyBackIcon(iconSize = 18.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.back))
        }
    }
}
