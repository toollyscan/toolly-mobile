package com.toolly.shared.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Shared Toolly design-system primitives that don't exist as plain Material3 defaults --
 * cards, filter chips, labeled text fields, an OTP entry field, switch rows, a labeled slider
 * and a progress bar. Every screen built against the Figma low-fidelity wireframes composes
 * these instead of hand-rolling styling, matching [ScreenColumn]/[PrimaryButton]/[SecondaryButton]
 * in ToollyApp.kt.
 */

@Composable
internal fun ToollyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(ToollySpacing.Large),
            verticalArrangement = Arrangement.spacedBy(ToollySpacing.ExtraSmall),
            content = content,
        )
    }
}

data class ToollyChipOption<T>(val value: T, val label: StringResource)

/** A horizontally scrollable row of single-select pill chips (Library filters, Export format/quality). */
@Composable
internal fun <T> ToollyChipRow(
    options: List<ToollyChipOption<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small),
    ) {
        items(options, key = { it.value.toString() }) { option ->
            ToollyFilterChip(
                label = stringResource(option.label),
                selected = option.value == selected,
                onClick = { onSelected(option.value) },
            )
        }
    }
}

@Composable
internal fun ToollyFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.heightIn(min = ToollySpacing.MinimumTarget),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = ToollySpacing.Large, vertical = ToollySpacing.Medium),
            contentAlignment = Alignment.Center,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * [placeholder] renders inside the field itself (Material3's `placeholder` slot) -- this is what
 * the wireframes use for example text ("name@example.com", "Enter 10-digit number"), distinct
 * from [supportingText], which renders below the field and is reserved for genuine captions like
 * error messages. Passing hint text as [supportingText] was a real fidelity bug (fixed 2026-08-16):
 * it rendered as a caption under the box instead of inside it.
 */
@Composable
internal fun ToollyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        readOnly = readOnly,
        singleLine = singleLine,
        shape = MaterialTheme.shapes.small,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth().heightIn(min = ToollySpacing.PrimaryActionHeight),
    )
}

/**
 * A [length]-digit OTP field (default 6, matching wireframe `2.3 Verification code`). Digits are
 * captured by one hidden focusable text field so backspace/auto-advance behave correctly across
 * cells, and rendered as [length] individually visible boxes with the active cell highlighted.
 */
@Composable
internal fun ToollyOtpField(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int,
    modifier: Modifier = Modifier,
    label: String,
) {
    val focusRequester = remember { FocusRequester() }
    Box(modifier = modifier.clickable { focusRequester.requestFocus() }) {
        BasicTextField(
            value = value,
            onValueChange = { new -> onValueChange(new.filter(Char::isDigit).take(length)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
                .focusRequester(focusRequester)
                .size(1.dp)
                .alpha(0f)
                .semantics { contentDescription = label },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small)) {
            repeat(length) { index ->
                val active = index == value.length
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    ),
                    modifier = Modifier.size(width = 44.dp, height = ToollySpacing.PrimaryActionHeight),
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().height(ToollySpacing.PrimaryActionHeight)) {
                        Text(
                            text = value.getOrNull(index)?.toString().orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

/** Label + description + trailing toggle, used by the backup-preferences list (`6.2 Backup choice`). */
@Composable
internal fun ToollySwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    ToollyCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (description != null) {
                    Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** Labeled slider (enhancement intensity, `1.4 Clean and save`). */
@Composable
internal fun ToollySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = ToollySpacing.MinimumTarget),
        )
    }
}

@Composable
internal fun ToollyLinearProgress(progress: Float, modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(6.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

/** Small dot-step progress indicator for multi-step flows (`4.1-4.3` account-completion steps). */
@Composable
internal fun ToollyStepProgress(stepCount: Int, currentStep: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small)) {
        repeat(stepCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index <= currentStep) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = MaterialTheme.shapes.extraSmall,
                    ),
            )
        }
    }
}
