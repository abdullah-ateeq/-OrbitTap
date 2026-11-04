package com.example.orbittap.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.orbittap.game.GameState
import com.example.orbittap.ui.theme.LocalOrbitVisualColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun OrbitPlayfieldCanvas(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    val orbitColors = LocalOrbitVisualColors.current

    // Subtle breathing animation for ambient orbit glow
    val infiniteTransition = rememberInfiniteTransition(label = "orbit_ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val center = Offset(centerX, centerY)
        val radius = min(size.width, size.height) * 0.38f

        // 1. Center concentric pulses
        drawCircle(
            color = orbitColors.centerDotPulse,
            radius = 16.dp.toPx() * ambientPulse,
            center = center
        )
        drawCircle(
            color = orbitColors.centerDot,
            radius = 4.dp.toPx(),
            center = center
        )

        // 2. Base orbit line
        val orbitStrokeWidth = 2.dp.toPx()
        drawCircle(
            color = orbitColors.orbitTrack,
            radius = radius,
            center = center,
            style = Stroke(width = orbitStrokeWidth)
        )

        // 3. Target Arc
        val targetStartAngle = gameState.targetAngle - (gameState.targetSpan / 2f)
        val arcTopLeft = Offset(centerX - radius, centerY - radius)
        val arcSize = Size(radius * 2f, radius * 2f)

        val hitExpansion = gameState.hitPulseProgress * 8.dp.toPx()
        val targetColor = if (gameState.isMissFlashing) {
            orbitColors.missFlash
        } else {
            orbitColors.targetArc
        }

        // Target outer glow
        val glowStrokeWidth = 12.dp.toPx() + hitExpansion
        drawArc(
            color = if (gameState.isMissFlashing) {
                orbitColors.missFlash.copy(alpha = 0.45f)
            } else {
                orbitColors.targetGlow.copy(alpha = 0.35f + (gameState.hitPulseProgress * 0.4f))
            },
            startAngle = targetStartAngle,
            sweepAngle = gameState.targetSpan,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(
                width = glowStrokeWidth,
                cap = StrokeCap.Round
            )
        )

        // Target primary arc
        val arcStrokeWidth = 5.dp.toPx() + (gameState.hitPulseProgress * 3.dp.toPx())
        drawArc(
            color = targetColor,
            startAngle = targetStartAngle,
            sweepAngle = gameState.targetSpan,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(
                width = arcStrokeWidth,
                cap = StrokeCap.Round
            )
        )

        // 4. Rotating runner object
        val objRad = Math.toRadians(gameState.objectAngle.toDouble())
        val objX = centerX + (radius * cos(objRad).toFloat())
        val objY = centerY + (radius * sin(objRad).toFloat())
        val objCenter = Offset(objX, objY)

        val runnerRadius = 7.dp.toPx() + (gameState.hitPulseProgress * 3.dp.toPx())
        val auraRadius = 16.dp.toPx() + (gameState.hitPulseProgress * 6.dp.toPx())

        // Outer glow
        drawCircle(
            color = orbitColors.runnerGlow,
            radius = auraRadius,
            center = objCenter
        )

        // Core dot
        drawCircle(
            color = orbitColors.runnerDot,
            radius = runnerRadius,
            center = objCenter
        )

        // Inner highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.9f),
            radius = runnerRadius * 0.5f,
            center = objCenter
        )

        // 5. Hit particles
        for (p in gameState.particles) {
            val px = centerX + (radius * p.xRatio)
            val py = centerY + (radius * p.yRatio)
            val pColor = when (p.colorType) {
                1 -> orbitColors.perfectFlash
                else -> orbitColors.targetArc
            }
            drawCircle(
                color = pColor.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                radius = p.radius.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}
