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
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.back
import com.toolly.shared.resources.backup_choice_title
import com.toolly.shared.resources.backup_e2e_encryption_label
import com.toolly.shared.resources.backup_include_originals_label
import com.toolly.shared.resources.backup_settings_button
import com.toolly.shared.resources.backup_wifi_only_label
import com.toolly.shared.resources.backup_while_charging_label
import com.toolly.shared.resources.disable_backup_button
import com.toolly.shared.resources.enable_backup_button
import com.toolly.shared.resources.encrypted_backup_description
import com.toolly.shared.resources.encrypted_backup_label
import com.toolly.shared.resources.encrypted_backup_off
import com.toolly.shared.resources.encrypted_backup_on
import com.toolly.shared.resources.estimated_cloud_use_label
import com.toolly.shared.resources.estimated_cloud_use_value
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
        SecondaryButton(Res.string.backup_settings_button, onOpenBackupSettings)
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
 * Backup preference toggles (wireframe `6.2/6.4 Backup choice`). Every value here is local,
 * presentation-only [BackupPreferences] state -- see that type's doc for why (Phase 5 gate).
 */
@Composable
fun BackupChoiceScreen(
    preferences: BackupPreferences,
    onPreferenceChanged: (BackupPreferenceKind, Boolean) -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    ScreenColumn {
        Text(stringResource(Res.string.backup_choice_title), style = MaterialTheme.typography.headlineMedium)
        ToollyCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(Res.string.encrypted_backup_label), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(if (preferences.enabled) Res.string.encrypted_backup_on else Res.string.encrypted_backup_off),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(stringResource(Res.string.encrypted_backup_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ToollySwitchRow(
            title = stringResource(Res.string.backup_wifi_only_label),
            checked = preferences.wifiOnly,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.WIFI_ONLY, it) },
        )
        ToollySwitchRow(
            title = stringResource(Res.string.backup_while_charging_label),
            checked = preferences.whileCharging,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.WHILE_CHARGING, it) },
        )
        ToollySwitchRow(
            title = stringResource(Res.string.backup_include_originals_label),
            checked = preferences.includeOriginals,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.INCLUDE_ORIGINALS, it) },
        )
        ToollySwitchRow(
            title = stringResource(Res.string.backup_e2e_encryption_label),
            checked = preferences.endToEndEncryption,
            onCheckedChange = { onPreferenceChanged(BackupPreferenceKind.END_TO_END_ENCRYPTION, it) },
        )
        ToollyCard {
            Text(stringResource(Res.string.estimated_cloud_use_label), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.estimated_cloud_use_value), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PrimaryButton(
            label = if (preferences.enabled) Res.string.disable_backup_button else Res.string.enable_backup_button,
            onClick = { onEnabledChanged(!preferences.enabled) },
        )
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
