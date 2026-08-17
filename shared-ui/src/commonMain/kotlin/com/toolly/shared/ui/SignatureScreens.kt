package com.toolly.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.toolly.shared.edit.NormalizedPoint
import com.toolly.shared.edit.SignatureStrokes
import com.toolly.shared.resources.Res
import com.toolly.shared.resources.back
import com.toolly.shared.resources.clear
import com.toolly.shared.resources.esign_description
import com.toolly.shared.resources.esign_done
import com.toolly.shared.resources.esign_title
import org.jetbrains.compose.resources.stringResource

/**
 * Freehand signature capture (not part of any wireframe -- simple visual stamp only, no identity
 * verification, no audit trail; confirmed with the user this is the intended scope, not a
 * legally-binding e-signature flow). [strokes] accumulates while the user drags a finger/pointer
 * across the pad; [onDone] is only enabled once at least one stroke exists.
 */
@Composable
fun SignaturePadScreen(
    strokes: SignatureStrokes,
    onStrokesChange: (SignatureStrokes) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var currentStroke by remember(strokes) { mutableStateOf<List<NormalizedPoint>>(emptyList()) }

    ScreenColumn {
        Text(stringResource(Res.string.esign_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.esign_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                .onSizeChanged { canvasSize = it }
                .pointerInput(canvasSize) {
                    if (canvasSize.width == 0 || canvasSize.height == 0) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            currentStroke = listOf(offset.toNormalizedPoint(canvasSize))
                        },
                        onDragEnd = {
                            if (currentStroke.size > 1) onStrokesChange(strokes + listOf(currentStroke))
                            currentStroke = emptyList()
                        },
                        onDragCancel = { currentStroke = emptyList() },
                        onDrag = { change, _ ->
                            change.consume()
                            currentStroke = currentStroke + change.position.toNormalizedPoint(canvasSize)
                        },
                    )
                },
        ) {
            if (canvasSize.width > 0 && canvasSize.height > 0) {
                Canvas(modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)) {
                    for (stroke in strokes + listOfNotNull(currentStroke.takeIf { it.size > 1 })) {
                        drawPath(
                            path = stroke.toPath(canvasSize),
                            color = ToollyColors.PrimaryText,
                            style = Stroke(width = 4.dp.toPx()),
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(ToollySpacing.Small)) {
            ToollyFilterChip(label = stringResource(Res.string.clear), selected = false, onClick = onClear)
        }
        PrimaryButton(label = Res.string.esign_done, onClick = onDone, enabled = strokes.isNotEmpty())
        SecondaryButton(Res.string.back, onClick = onBack)
    }
}

private fun Offset.toNormalizedPoint(canvasSize: IntSize): NormalizedPoint = NormalizedPoint(
    x = (x / canvasSize.width).coerceIn(0f, 1f),
    y = (y / canvasSize.height).coerceIn(0f, 1f),
)

private fun List<NormalizedPoint>.toPath(canvasSize: IntSize): Path = Path().apply {
    if (isEmpty()) return@apply
    val first = this@toPath[0]
    moveTo(first.x * canvasSize.width, first.y * canvasSize.height)
    for (index in 1 until size) {
        val point = this@toPath[index]
        lineTo(point.x * canvasSize.width, point.y * canvasSize.height)
    }
}
