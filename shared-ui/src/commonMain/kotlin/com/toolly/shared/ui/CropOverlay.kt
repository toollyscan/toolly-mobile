package com.toolly.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.toolly.shared.edit.CropRegion
import com.toolly.shared.edit.NormalizedPoint
import kotlin.math.roundToInt

/**
 * Draggable four-corner crop overlay matching wireframes `1.3 Correct crop` / `3.1 Manual corners`:
 * a dark immersive canvas, a highlighted quadrilateral with 28dp-visible/48dp-touch-target corner
 * handles, and a precision loupe that follows whichever handle is being dragged.
 *
 * Takes an already-decoded [image] rather than a platform bitmap type or file path, so this
 * composable has no Android/iOS-specific imports -- the platform host is responsible for decoding
 * the source asset once (see `spike-capture`'s crop/enhance host) and can reuse the same
 * [ImageBitmap] for both the main view and the loupe without decoding twice.
 */
@Composable
fun CropOverlay(
    image: ImageBitmap?,
    region: CropRegion,
    onRegionChange: (CropRegion) -> Unit,
    modifier: Modifier = Modifier,
    cornerLabel: (Corner) -> String = { "" },
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var activeCorner by remember { mutableStateOf<Corner?>(null) }

    Box(
        modifier = modifier
            .background(ToollyColors.CameraSurface, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large)
            .onSizeChanged { canvasSize = it },
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.matchParentSize(),
            )
        }
        if (canvasSize.width > 0 && canvasSize.height > 0) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val points = region.corners().map { it.toPx(canvasSize) }
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
                    close()
                }
                drawPath(path, color = ToollyColors.Primary, style = Stroke(width = 3.dp.toPx()))
                val midpoints = listOf(
                    (points[0] + points[1]) / 2f,
                    (points[1] + points[2]) / 2f,
                    (points[2] + points[3]) / 2f,
                    (points[3] + points[0]) / 2f,
                )
                midpoints.forEach { drawCircle(ToollyColors.Primary, radius = 3.dp.toPx(), center = it) }
            }
            Corner.entries.forEach { corner ->
                CropHandle(
                    point = region.pointFor(corner),
                    canvasSize = canvasSize,
                    label = cornerLabel(corner),
                    onDragStart = { activeCorner = corner },
                    onDragEnd = { activeCorner = null },
                    onDrag = { deltaPx ->
                        onRegionChange(
                            region.withCorner(
                                corner,
                                region.pointFor(corner).movedBy(deltaPx, canvasSize),
                            ),
                        )
                    },
                )
            }
            activeCorner?.let { corner ->
                PrecisionLoupe(
                    image = image,
                    point = region.pointFor(corner),
                    canvasSize = canvasSize,
                )
            }
        }
    }
}

enum class Corner { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }

private fun CropRegion.corners(): List<NormalizedPoint> =
    listOf(topLeft, topRight, bottomRight, bottomLeft)

private fun CropRegion.pointFor(corner: Corner): NormalizedPoint = when (corner) {
    Corner.TOP_LEFT -> topLeft
    Corner.TOP_RIGHT -> topRight
    Corner.BOTTOM_RIGHT -> bottomRight
    Corner.BOTTOM_LEFT -> bottomLeft
}

private fun CropRegion.withCorner(corner: Corner, point: NormalizedPoint): CropRegion = when (corner) {
    Corner.TOP_LEFT -> copy(topLeft = point)
    Corner.TOP_RIGHT -> copy(topRight = point)
    Corner.BOTTOM_RIGHT -> copy(bottomRight = point)
    Corner.BOTTOM_LEFT -> copy(bottomLeft = point)
}

private fun NormalizedPoint.toPx(canvasSize: IntSize): Offset =
    Offset(x * canvasSize.width, y * canvasSize.height)

private fun NormalizedPoint.movedBy(deltaPx: Offset, canvasSize: IntSize): NormalizedPoint {
    val newX = (x * canvasSize.width + deltaPx.x).coerceIn(0f, canvasSize.width.toFloat())
    val newY = (y * canvasSize.height + deltaPx.y).coerceIn(0f, canvasSize.height.toFloat())
    return NormalizedPoint(
        x = (newX / canvasSize.width).coerceIn(0f, 1f),
        y = (newY / canvasSize.height).coerceIn(0f, 1f),
    )
}

@Composable
private fun CropHandle(
    point: NormalizedPoint,
    canvasSize: IntSize,
    label: String,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit,
) {
    val positionPx = point.toPx(canvasSize)
    val touchTargetPx = with(LocalDensity.current) { ToollySpacing.MinimumTarget.toPx() }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (positionPx.x - touchTargetPx / 2).roundToInt(),
                    (positionPx.y - touchTargetPx / 2).roundToInt(),
                )
            }
            .size(ToollySpacing.MinimumTarget)
            .pointerInput(canvasSize) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                )
            }
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, ToollyColors.Primary, CircleShape),
        )
    }
}

@Composable
private fun PrecisionLoupe(image: ImageBitmap?, point: NormalizedPoint, canvasSize: IntSize) {
    if (image == null) return
    val density = LocalDensity.current
    val diameter = 96.dp
    val diameterPx = with(density) { diameter.toPx() }
    val magnification = 2.5f
    val pointPx = point.toPx(canvasSize)
    val marginPx = with(density) { 16.dp.toPx() }
    val offsetXPx = (pointPx.x - diameterPx / 2)
        .coerceIn(0f, (canvasSize.width - diameterPx).coerceAtLeast(0f))
    val offsetYPx = (pointPx.y - diameterPx - marginPx).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetXPx.roundToInt(), offsetYPx.roundToInt()) }
            .size(diameter)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, ToollyColors.Primary, CircleShape),
    ) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(with(density) { canvasSize.width.toDp() }, with(density) { canvasSize.height.toDp() })
                .graphicsLayer {
                    scaleX = magnification
                    scaleY = magnification
                    transformOrigin = TransformOrigin(0f, 0f)
                    translationX = diameterPx / 2 - pointPx.x * magnification
                    translationY = diameterPx / 2 - pointPx.y * magnification
                },
        )
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val armPx = 8.dp.toPx()
            drawLine(ToollyColors.Primary, center.copy(x = center.x - armPx), center.copy(x = center.x + armPx), 2.dp.toPx())
            drawLine(ToollyColors.Primary, center.copy(y = center.y - armPx), center.copy(y = center.y + armPx), 2.dp.toPx())
        }
    }
}

@Composable
internal fun CropCaption(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(ToollyColors.PrimaryText.copy(alpha = 0.85f), MaterialTheme.shapes.extraLarge),
    ) {
        Text(
            text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = ToollySpacing.Large, vertical = ToollySpacing.Small),
        )
    }
}
