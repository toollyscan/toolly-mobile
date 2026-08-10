package com.toolly.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.back
import com.toolly.shared.resources.export_builder_description
import com.toolly.shared.resources.export_builder_title
import com.toolly.shared.resources.export_format_jpeg
import com.toolly.shared.resources.export_format_pdf
import com.toolly.shared.resources.export_securely
import com.toolly.shared.resources.premium_lock_label
import com.toolly.shared.resources.privacy_check_description
import com.toolly.shared.resources.privacy_check_title
import com.toolly.shared.resources.save_to_device
import com.toolly.shared.resources.searchable_pdf_label
import com.toolly.shared.resources.searchable_pdf_premium_notice
import com.toolly.shared.resources.share
import org.jetbrains.compose.resources.stringResource

/**
 * UI-level export format -- deliberately just PDF/JPEG, matching what
 * `com.toolly.domain.model.DocumentExportFormat`/`AndroidDocumentExporter` actually support today.
 * shared-ui cannot reference that domain type directly (it lives in the Android-only spike-capture
 * module), so the platform host maps between this and the real domain enum at the call site.
 *
 * The wireframe's "Long image" format and Small/Balanced/Best quality tiers are not offered here:
 * neither has exporter support yet, and this project's policy is not to show controls that don't
 * do anything real (see docs/product/ENTITLEMENTS.md's own precedent -- premium *lock* affordances
 * are fine to show ahead of billing; a control with literally no backing implementation is not the
 * same thing).
 */
enum class ToollyExportFormat { PDF, JPEG }

/**
 * Export format selection (wireframe `5.1 Export builder`). [previewContent] is an optional slot
 * for a page-thumbnail preview (the platform host already has a working grid for this -- see
 * `DocumentPageGrid` in spike-capture -- so this screen doesn't reinvent one).
 */
@Composable
fun ExportBuilderScreen(
    format: ToollyExportFormat,
    onFormatChange: (ToollyExportFormat) -> Unit,
    onContinue: () -> Unit,
    busy: Boolean = false,
    continueEnabled: Boolean = true,
    onBack: (() -> Unit)? = null,
    previewContent: (@Composable () -> Unit)? = null,
) {
    val formatOptions = listOf(
        ToollyChipOption(ToollyExportFormat.PDF, Res.string.export_format_pdf),
        ToollyChipOption(ToollyExportFormat.JPEG, Res.string.export_format_jpeg),
    )
    ScreenColumn {
        Text(stringResource(Res.string.export_builder_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.export_builder_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        previewContent?.invoke()
        ToollyChipRow(options = formatOptions, selected = format, onSelected = onFormatChange)
        ToollyCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(Res.string.searchable_pdf_label), style = MaterialTheme.typography.titleMedium)
                PremiumLockBadge()
            }
            Text(
                stringResource(Res.string.searchable_pdf_premium_notice),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(label = Res.string.export_securely, onClick = onContinue, enabled = !busy && continueEnabled)
        if (onBack != null) {
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
}

/**
 * Explicit disclosure before an export leaves the encrypted local vault (per
 * docs/security/PRIVACY_READINESS.md), matching wireframe `5.3 Privacy check`. Save-to-device and
 * Share (`5.4`) are both one tap from here -- Share invokes the OS share sheet directly
 * (architecture rule ES-12: no custom share UI), so there is no separate "share options" screen.
 */
@Composable
fun ExportPrivacyCheckScreen(
    onSaveToDevice: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean = false,
) {
    ScreenColumn {
        Text(stringResource(Res.string.privacy_check_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.privacy_check_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        PrimaryButton(label = Res.string.save_to_device, onClick = onSaveToDevice, enabled = !busy)
        SecondaryButton(Res.string.share, onShare)
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
internal fun PremiumLockBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    ) {
        Text(
            stringResource(Res.string.premium_lock_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ToollySpacing.Small, vertical = ToollySpacing.ExtraSmall),
        )
    }
}
