package com.toolly.shared.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.toolly.shared.edit.CropRegion
import com.toolly.shared.edit.EnhancementMode
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.auto_crop
import com.toolly.shared.resources.confirm_crop
import com.toolly.shared.resources.corner_bottom_left
import com.toolly.shared.resources.corner_bottom_right
import com.toolly.shared.resources.corner_top_left
import com.toolly.shared.resources.corner_top_right
import com.toolly.shared.resources.crop_edges_detected
import com.toolly.shared.resources.enhance_intensity_label
import com.toolly.shared.resources.enhance_mode_auto
import com.toolly.shared.resources.enhance_mode_black_and_white
import com.toolly.shared.resources.enhance_mode_clean
import com.toolly.shared.resources.enhance_mode_color
import com.toolly.shared.resources.enhance_mode_gray
import com.toolly.shared.resources.enhance_title
import com.toolly.shared.resources.retake
import com.toolly.shared.resources.save_page
import org.jetbrains.compose.resources.stringResource

/**
 * Crop-review step (wireframes `1.3 Correct crop` / `3.1 Manual corners`): immersive [CropOverlay]
 * plus Auto crop / Rotate / Retake actions and a Continue button. [image] is an already-decoded
 * platform bitmap (see [CropOverlay]'s doc) so this screen has no Android/iOS-specific imports.
 */
@Composable
fun CropPageScreen(
    image: ImageBitmap?,
    region: CropRegion,
    onRegionChange: (CropRegion) -> Unit,
    onAutoCrop: () -> Unit,
    onRotate: () -> Unit,
    onRetake: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
) {
    val cornerLabels = mapOf(
        Corner.TOP_LEFT to stringResource(Res.string.corner_top_left),
        Corner.TOP_RIGHT to stringResource(Res.string.corner_top_right),
        Corner.BOTTOM_RIGHT to stringResource(Res.string.corner_bottom_right),
        Corner.BOTTOM_LEFT to stringResource(Res.string.corner_bottom_left),
    )
    ScreenColumn(modifier = modifier) {
        Text(stringResource(Res.string.product_name), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.crop_edges_detected),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp), contentAlignment = Alignment.Center) {
            CropOverlay(
                image = image,
                region = region,
                onRegionChange = onRegionChange,
                modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp),
                cornerLabel = { cornerLabels.getValue(it) },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small)) {
            ToollyFilterChip(label = stringResource(Res.string.auto_crop), selected = true, onClick = onAutoCrop)
            ToollyFilterChip(label = stringResource(Res.string.rotate), selected = false, onClick = onRotate)
            ToollyFilterChip(label = stringResource(Res.string.retake), selected = false, onClick = onRetake)
        }
        PrimaryButton(label = Res.string.confirm_crop, onClick = onContinue, enabled = !busy)
    }
}

/**
 * Enhancement step (wireframe `1.4 Clean and save`): live preview, filter-mode chips and an
 * intensity slider.
 */
@Composable
fun EnhancePageScreen(
    image: ImageBitmap?,
    mode: EnhancementMode,
    intensity: Float,
    onModeChange: (EnhancementMode) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
) {
    val modeOptions = listOf(
        ToollyChipOption(EnhancementMode.AUTO, Res.string.enhance_mode_auto),
        ToollyChipOption(EnhancementMode.CLEAN, Res.string.enhance_mode_clean),
        ToollyChipOption(EnhancementMode.COLOR, Res.string.enhance_mode_color),
        ToollyChipOption(EnhancementMode.GRAY, Res.string.enhance_mode_gray),
        ToollyChipOption(EnhancementMode.BLACK_AND_WHITE, Res.string.enhance_mode_black_and_white),
    )
    ScreenColumn(modifier = modifier) {
        Text(stringResource(Res.string.product_name), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(Res.string.enhance_title), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 340.dp),
                )
            }
        }
        ToollyChipRow(options = modeOptions, selected = mode, onSelected = onModeChange)
        ToollySlider(
            label = stringResource(Res.string.enhance_intensity_label),
            value = intensity,
            onValueChange = onIntensityChange,
        )
        PrimaryButton(label = Res.string.save_page, onClick = onSave, enabled = !busy)
    }
}
