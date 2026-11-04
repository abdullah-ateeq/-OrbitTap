package com.example.orbittap.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.orbittap.ui.theme.LocalOrbitVisualColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun OrbitPreviewCanvas(
    modifier: Modifier = Modifier,
    targetAngle: Float = 45f,
    targetSpan: Float = 48f
) {
    val orbitColors = LocalOrbitVisualColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "preview_orbit")
    val currentAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "runner_angle"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val center = Offset(centerX, centerY)
        val radius = min(size.width, size.height) * 0.38f

        // Center dot
        drawCircle(
            color = orbitColors.centerDotPulse,
            radius = 12.dp.toPx(),
            center = center
        )
        drawCircle(
            color = orbitColors.centerDot,
            radius = 3.5.dp.toPx(),
            center = center
        )

        // Orbit ring
        drawCircle(
            color = orbitColors.orbitTrack,
            radius = radius,
            center = center,
            style = Stroke(width = 1.8.dp.toPx())
        )

        // Target arc
        val arcTopLeft = Offset(centerX - radius, centerY - radius)
        val arcSize = Size(radius * 2f, radius * 2f)
        val startAngle = targetAngle - (targetSpan / 2f)

        // Target glow
        drawArc(
            color = orbitColors.targetGlow.copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = targetSpan,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
        // Target line
        drawArc(
            color = orbitColors.targetArc,
            startAngle = startAngle,
            sweepAngle = targetSpan,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Rotating object
        val rad = Math.toRadians(currentAngle.toDouble())
        val objX = centerX + (radius * cos(rad).toFloat())
        val objY = centerY + (radius * sin(rad).toFloat())
        val objCenter = Offset(objX, objY)

        drawCircle(
            color = orbitColors.runnerGlow,
            radius = 12.dp.toPx(),
            center = objCenter
        )
        drawCircle(
            color = orbitColors.runnerDot,
            radius = 5.5.dp.toPx(),
            center = objCenter
        )
    }
}
