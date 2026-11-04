package com.example.orbittap.game

import androidx.compose.runtime.Immutable

enum class GameStatus {
    RUNNING,
    PAUSED,
    GAME_OVER
}

@Immutable
data class Particle(
    val id: Long,
    val xRatio: Float, // Relative to orbit center [-1, 1]
    val yRatio: Float,
    val vx: Float,
    val vy: Float,
    val alpha: Float = 1.0f,
    val radius: Float = 3.5f,
    val colorType: Int = 0 // 0 = cyan, 1 = gold, 2 = white
)

@Immutable
data class ScorePopup(
    val id: Long,
    val text: String,
    val isPerfect: Boolean,
    val alpha: Float = 1.0f,
    val offsetY: Float = 0.0f
)

@Immutable
data class GameState(
    val status: GameStatus = GameStatus.RUNNING,
    val objectAngle: Float = 0f,
    val targetAngle: Float = 90f,
    val targetSpan: Float = DifficultyManager.INITIAL_ARC_SPAN,
    val direction: Float = 1f,
    val speed: Float = DifficultyManager.INITIAL_SPEED,
    val score: Int = 0,
    val streak: Int = 0,
    val highestStreakInRun: Int = 0,
    val successfulHits: Int = 0,
    val perfectHits: Int = 0,
    val bestScore: Int = 0,
    val isNewBestScore: Boolean = false,
    val particles: List<Particle> = emptyList(),
    val scorePopups: List<ScorePopup> = emptyList(),
    val hitPulseProgress: Float = 0f, // 0f to 1f for expanding arc / pulse
    val shakeOffset: Float = 0f,
    val isMissFlashing: Boolean = false
)
