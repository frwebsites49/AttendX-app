package com.college.attendx.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Brutalist scan-frame overlay: a square cutout with thick corner
 * brackets (no full border, no animated laser/red line - the look
 * explicitly NOT wanted). Drawn over the live camera preview.
 */
@Composable
fun ScannerOverlay(
    modifier: Modifier = Modifier,
    frameColor: Color = Color(0xFFFF6B35),
    maskColor: Color = Color.Black.copy(alpha = 0.55f),
    cornerLength: Dp = 32.dp,
    strokeWidth: Dp = 5.dp
) {
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Square scan area, sized relative to the shorter screen
            // dimension so it always looks right in portrait.
            val frameSize = (canvasWidth.coerceAtMost(canvasHeight)) * 0.7f
            val left = (canvasWidth - frameSize) / 2f
            val top = (canvasHeight - frameSize) / 2f
            val right = left + frameSize
            val bottom = top + frameSize

            // Dim everything OUTSIDE the scan square.
            val outerPath = Path().apply {
                addRect(androidx.compose.ui.geometry.Rect(0f, 0f, canvasWidth, canvasHeight))
            }
            val innerPath = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        left, top, right, bottom,
                        CornerRadius(16f, 16f)
                    )
                )
            }
            val maskPath = Path.combine(
                androidx.compose.ui.graphics.PathOperation.Difference,
                outerPath,
                innerPath
            )
            drawPath(maskPath, color = maskColor)

            // Corner brackets only - this is the "no red line, looks
            // professional" part. Four L-shaped strokes, one per corner.
            val cornerPx = cornerLength.toPx()
            val strokePx = strokeWidth.toPx()
            val stroke = Stroke(width = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)

            // Top-left
            drawLine(frameColor, Offset(left, top + cornerPx), Offset(left, top), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(frameColor, Offset(left, top), Offset(left + cornerPx, top), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)

            // Top-right
            drawLine(frameColor, Offset(right - cornerPx, top), Offset(right, top), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(frameColor, Offset(right, top), Offset(right, top + cornerPx), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)

            // Bottom-left
            drawLine(frameColor, Offset(left, bottom - cornerPx), Offset(left, bottom), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(frameColor, Offset(left, bottom), Offset(left + cornerPx, bottom), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)

            // Bottom-right
            drawLine(frameColor, Offset(right - cornerPx, bottom), Offset(right, bottom), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(frameColor, Offset(right, bottom), Offset(right, bottom - cornerPx), strokeWidth = strokePx, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}