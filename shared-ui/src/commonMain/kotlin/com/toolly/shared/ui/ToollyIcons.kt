package com.toolly.shared.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Toolly's own small line-icon set, drawn directly with [Canvas] rather than pulled from an
 * icon-font/vector-icon library dependency.
 *
 * Deliberate, not an oversight: `shared-ui`'s Gradle configurations are dependency-locked and
 * verified (`gradle.lockfile` / `gradle/verification-metadata.xml`), and this repo's Android
 * Gradle tasks are already blocked on this Windows machine by a pre-existing
 * verification-metadata gap (see `docs/execution/ROADMAP.md`) -- there is no way to regenerate a
 * correct lockfile entry for a new dependency here without a real build to run it against. These
 * icons cost no new dependency at all, matching the style already established by the bottom
 * nav's hand-drawn Scan "+" glyph.
 *
 * All icons default to [LocalContentColor] so they tint correctly for selected/unselected
 * NavigationBarItem state and any surrounding content color, exactly like [androidx.compose.material3.Icon].
 */
private const val DEFAULT_ICON_SIZE_DP = 22

@Composable
fun ToollyHomeIcon(modifier: Modifier = Modifier, iconSize: Dp = DEFAULT_ICON_SIZE_DP.dp) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = strokeWidth()
        val w = size.width
        val h = size.height
        val roofTipY = h * 0.06f
        val eaveY = h * 0.42f
        val baseInset = w * 0.14f
        drawLine(color, Offset(baseInset, eaveY), Offset(w / 2f, roofTipY), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w / 2f, roofTipY), Offset(w - baseInset, eaveY), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(baseInset, eaveY), Offset(baseInset, h * 0.92f), stroke, cap = StrokeCap.Round)
        drawLine(
            color,
            Offset(w - baseInset, eaveY),
            Offset(w - baseInset, h * 0.92f),
            stroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color,
            Offset(baseInset, h * 0.92f),
            Offset(w - baseInset, h * 0.92f),
            stroke,
            cap = StrokeCap.Round,
        )
        val doorLeft = w * 0.42f
        val doorRight = w * 0.58f
        drawLine(color, Offset(doorLeft, h * 0.92f), Offset(doorLeft, h * 0.6f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(doorRight, h * 0.92f), Offset(doorRight, h * 0.6f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(doorLeft, h * 0.6f), Offset(doorRight, h * 0.6f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun ToollyLibraryIcon(modifier: Modifier = Modifier, iconSize: Dp = DEFAULT_ICON_SIZE_DP.dp) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = strokeWidth()
        val w = size.width
        val h = size.height
        val tabWidth = w * 0.42f
        val bodyTop = h * 0.28f
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.9f)
            lineTo(w * 0.1f, bodyTop + h * 0.06f)
            lineTo(w * 0.1f + tabWidth * 0.35f, bodyTop)
            lineTo(w * 0.1f + tabWidth, bodyTop)
            lineTo(w * 0.1f + tabWidth + h * 0.06f, bodyTop + h * 0.06f)
            lineTo(w * 0.9f, bodyTop + h * 0.06f)
            lineTo(w * 0.9f, h * 0.9f)
            close()
        }
        drawPath(
            path,
            color = color,
            style = Stroke(width = stroke, join = StrokeJoin.Round, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun ToollySearchIcon(modifier: Modifier = Modifier, iconSize: Dp = DEFAULT_ICON_SIZE_DP.dp) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = strokeWidth()
        val w = size.width
        val h = size.height
        val radius = w * 0.32f
        val center = Offset(w * 0.42f, h * 0.42f)
        drawCircle(color, radius = radius, center = center, style = Stroke(width = stroke))
        val handleStart = Offset(
            center.x + radius * kotlin.math.cos(kotlin.math.PI.toFloat() / 4f),
            center.y + radius * kotlin.math.sin(kotlin.math.PI.toFloat() / 4f),
        )
        drawLine(color, handleStart, Offset(w * 0.9f, h * 0.9f), stroke, cap = StrokeCap.Round)
    }
}

@Composable
fun ToollyProfileIcon(modifier: Modifier = Modifier, iconSize: Dp = DEFAULT_ICON_SIZE_DP.dp) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = strokeWidth()
        val w = size.width
        val h = size.height
        val headRadius = w * 0.19f
        drawCircle(color, radius = headRadius, center = Offset(w / 2f, h * 0.32f), style = Stroke(width = stroke))
        val path = Path().apply {
            moveTo(w * 0.16f, h * 0.9f)
            cubicTo(
                w * 0.16f, h * 0.62f,
                w * 0.32f, h * 0.55f,
                w * 0.5f, h * 0.55f,
            )
            cubicTo(
                w * 0.68f, h * 0.55f,
                w * 0.84f, h * 0.62f,
                w * 0.84f, h * 0.9f,
            )
        }
        drawPath(path, color = color, style = Stroke(width = stroke, cap = StrokeCap.Round))
    }
}

@Composable
fun ToollyBackIcon(modifier: Modifier = Modifier, iconSize: Dp = DEFAULT_ICON_SIZE_DP.dp) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = strokeWidth()
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.62f, h * 0.2f), Offset(w * 0.32f, h * 0.5f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.32f, h * 0.5f), Offset(w * 0.62f, h * 0.8f), stroke, cap = StrokeCap.Round)
    }
}

/** A generic scanned-page glyph used as a leading icon on Library/Search document rows. */
@Composable
fun ToollyDocumentIcon(modifier: Modifier = Modifier, iconSize: Dp = DEFAULT_ICON_SIZE_DP.dp) {
    val color = LocalContentColor.current
    Canvas(modifier = modifier.size(iconSize)) {
        val stroke = strokeWidth()
        val w = size.width
        val h = size.height
        val cornerFold = w * 0.22f
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.08f)
            lineTo(w * 0.78f - cornerFold, h * 0.08f)
            lineTo(w * 0.78f, h * 0.08f + cornerFold)
            lineTo(w * 0.78f, h * 0.92f)
            lineTo(w * 0.22f, h * 0.92f)
            close()
        }
        drawPath(path, color = color, style = Stroke(width = stroke, join = StrokeJoin.Round))
        val lineInset = w * 0.34f
        for (fraction in listOf(0.4f, 0.55f, 0.7f)) {
            drawLine(
                color,
                Offset(lineInset, h * fraction),
                Offset(w * 0.66f, h * fraction),
                stroke * 0.7f,
                cap = StrokeCap.Round,
            )
        }
    }
}

private fun DrawScope.strokeWidth(): Float = size.minDimension * 0.085f
