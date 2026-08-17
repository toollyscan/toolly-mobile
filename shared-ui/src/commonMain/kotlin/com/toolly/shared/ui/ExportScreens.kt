package com.toolly.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.toolly.shared.resources.document_page_count
import com.toolly.shared.resources.export_builder_description
import com.toolly.shared.resources.export_builder_title
import com.toolly.shared.resources.export_format_jpeg
import com.toolly.shared.resources.export_format_label
import com.toolly.shared.resources.export_format_pdf
import com.toolly.shared.resources.export_preview_caption
import com.toolly.shared.resources.export_quality_balanced
import com.toolly.shared.resources.export_quality_best
import com.toolly.shared.resources.export_quality_label
import com.toolly.shared.resources.export_quality_small
import com.toolly.shared.resources.export_securely
import com.toolly.shared.resources.export_watermark_hint
import com.toolly.shared.resources.export_watermark_label
import com.toolly.shared.resources.premium_lock_label
import com.toolly.shared.resources.privacy_check_description
import com.toolly.shared.resources.privacy_check_title
import com.toolly.shared.resources.save_to_device
import com.toolly.shared.resources.searchable_pdf_label
import com.toolly.shared.resources.searchable_pdf_premium_notice
import com.toolly.shared.resources.share
import com.toolly.shared.resources.sign_document
import com.toolly.shared.resources.signed_indicator
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * UI-level export format -- deliberately just PDF/JPEG, matching what
 * `com.toolly.domain.model.DocumentExportFormat`/`AndroidDocumentExporter` actually support today.
 * shared-ui cannot reference that domain type directly (it lives in the Android-only spike-capture
 * module), so the platform host maps between this and the real domain enum at the call site.
 *
 * The wireframe's "Long image" format is still not offered: no exporter support exists for it, and
 * this project's policy is not to show controls that don't do anything real (see
 * docs/product/ENTITLEMENTS.md's own precedent -- premium *lock* affordances are fine to show ahead
 * of billing; a control with literally no backing implementation is not the same thing). Watermark
 * text (not part of any wireframe) is real: a blank field means no watermark; typed text gets
 * stamped diagonally across every exported page.
 */
enum class ToollyExportFormat { PDF, JPEG }

/**
 * Export quality tiers (wireframe `5.1`'s Small/Balanced/Best chips), mirroring
 * `AndroidDocumentExporter.ExportQuality` for the same shared-ui/spike-capture boundary reason as
 * [ToollyExportFormat]. Real, not cosmetic: each tier re-encodes every page at a different JPEG
 * quality (and downscales the smaller tiers), genuinely changing output file size.
 */
enum class ToollyExportQuality { SMALL, BALANCED, BEST }

/**
 * Export format selection (wireframe `5.1 Export builder`). [previewContent] is an optional slot
 * for a page-thumbnail preview (the platform host already has a working grid for this -- see
 * `DocumentPageGrid` in spike-capture -- so this screen doesn't reinvent one).
 */
@Composable
fun ExportBuilderScreen(
    format: ToollyExportFormat,
    onFormatChange: (ToollyExportFormat) -> Unit,
    quality: ToollyExportQuality,
    onQualityChange: (ToollyExportQuality) -> Unit,
    watermarkText: String,
    onWatermarkTextChange: (String) -> Unit,
    onContinue: () -> Unit,
    busy: Boolean = false,
    continueEnabled: Boolean = true,
    onBack: (() -> Unit)? = null,
    previewContent: (@Composable () -> Unit)? = null,
    documentTitle: String? = null,
    totalPageCount: Int? = null,
    hasSignature: Boolean = false,
    onSignDocument: (() -> Unit)? = null,
) {
    val formatOptions = listOf(
        ToollyChipOption(ToollyExportFormat.PDF, Res.string.export_format_pdf),
        ToollyChipOption(ToollyExportFormat.JPEG, Res.string.export_format_jpeg),
    )
    val qualityOptions = listOf(
        ToollyChipOption(ToollyExportQuality.SMALL, Res.string.export_quality_small),
        ToollyChipOption(ToollyExportQuality.BALANCED, Res.string.export_quality_balanced),
        ToollyChipOption(ToollyExportQuality.BEST, Res.string.export_quality_best),
    )
    ScreenColumn {
        Text(stringResource(Res.string.export_builder_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.export_builder_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (documentTitle != null && totalPageCount != null) {
            ToollyCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(documentTitle, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${pluralStringResource(Res.plurals.document_page_count, totalPageCount, totalPageCount)} • " +
                                stringResource(if (format == ToollyExportFormat.PDF) Res.string.export_format_pdf else Res.string.export_format_jpeg),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ToollyBadge(
                        label = stringResource(if (format == ToollyExportFormat.PDF) Res.string.export_format_pdf else Res.string.export_format_jpeg),
                    )
                }
            }
        }
        if (previewContent != null) {
            previewContent()
            Text(
                stringResource(Res.string.export_preview_caption),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(stringResource(Res.string.export_format_label), style = MaterialTheme.typography.titleSmall)
        ToollyChipRow(options = formatOptions, selected = format, onSelected = onFormatChange)
        Text(stringResource(Res.string.export_quality_label), style = MaterialTheme.typography.titleSmall)
        ToollyChipRow(options = qualityOptions, selected = quality, onSelected = onQualityChange)
        ToollyTextField(
            value = watermarkText,
            onValueChange = onWatermarkTextChange,
            label = stringResource(Res.string.export_watermark_label),
            placeholder = stringResource(Res.string.export_watermark_hint),
        )
        if (onSignDocument != null) {
            SecondaryButton(
                label = if (hasSignature) Res.string.signed_indicator else Res.string.sign_document,
                onClick = onSignDocument,
            )
        }
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
 *
 * The wireframe also shows live per-page "SSN detected on page 2" / "Phone number on page 3" cards
 * with per-finding Redact buttons. That's not built: it would need on-device text extraction (OCR)
 * to even see the page content, and this app has none yet (Search's own doc notes the same gap).
 * Showing static example findings would be fabricated data, not a real detector -- so this screen
 * stays a general disclosure until real text extraction exists to back it.
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
        SecondaryButton(Res.string.share, onClick = onShare)
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
    ToollyBadge(label = stringResource(Res.string.premium_lock_label), modifier = modifier)
}

/** A small pill label -- the export-format badge (`5.1`/`5.2`) reuses [PremiumLockBadge]'s styling. */
@Composable
internal fun ToollyBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ToollySpacing.Small, vertical = ToollySpacing.ExtraSmall),
        )
    }
}
