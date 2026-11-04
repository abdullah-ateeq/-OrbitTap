package com.example.orbittap.game

import kotlin.math.abs
import kotlin.math.min

object GameMath {

    const val FULL_CIRCLE = 360f
    const val BASE_SCORE = 100
    const val MAX_PRECISION_BONUS = 100
    const val PERFECT_BONUS = 50
    const val PERFECT_THRESHOLD_RATIO = 0.82f

    /**
     * Normalizes any angle in degrees into the [0, 360) range.
     */
    fun normalizeAngle(angle: Float): Float {
        var a = angle % FULL_CIRCLE
        if (a < 0f) {
            a += FULL_CIRCLE
        }
        return a
    }

    /**
     * Computes the shortest angular distance between two angles in degrees.
     * Correctly handles wrapping around the 0°/360° boundary.
     * Returns a value between 0.0 and 180.0 degrees.
     */
    fun shortestAngularDistance(angleA: Float, angleB: Float): Float {
        val normA = normalizeAngle(angleA)
        val normB = normalizeAngle(angleB)
        val diff = abs(normA - normB)
        return min(diff, FULL_CIRCLE - diff)
    }

    /**
     * Returns true if the object angle is within the target arc defined by
     * targetCenterAngle and arcSpan (total arc width in degrees).
     */
    fun isHit(objectAngle: Float, targetCenterAngle: Float, arcSpan: Float): Boolean {
        require(arcSpan > 0f) { "arcSpan must be positive" }
        val distance = shortestAngularDistance(objectAngle, targetCenterAngle)
        return distance <= (arcSpan / 2f)
    }

    /**
     * Calculates precision ratio between 0.0 (at the outer boundary of the arc)
     * and 1.0 (exact center of target). Returns 0.0 if outside the arc.
     */
    fun calculatePrecision(objectAngle: Float, targetCenterAngle: Float, arcSpan: Float): Float {
        if (!isHit(objectAngle, targetCenterAngle, arcSpan)) {
            return 0f
        }
        val distance = shortestAngularDistance(objectAngle, targetCenterAngle)
        val halfSpan = arcSpan / 2f
        if (halfSpan <= 0f) return 0f
        val ratio = (halfSpan - distance) / halfSpan
        return ratio.coerceIn(0f, 1f)
    }

    /**
     * Evaluates if a hit is considered "Perfect".
     */
    fun isPerfectHit(precisionRatio: Float): Boolean {
        return precisionRatio >= PERFECT_THRESHOLD_RATIO
    }

    /**
     * Calculates the streak multiplier.
     * Every 3 consecutive hits increases multiplier by 0.25x (1.0x, 1.25x, 1.5x, up to 4.0x).
     */
    fun calculateStreakMultiplier(streak: Int): Float {
        if (streak <= 0) return 1.0f
        val bonusSteps = streak / 3
        val multiplier = 1.0f + (bonusSteps * 0.25f)
        return multiplier.coerceAtMost(4.0f)
    }

    /**
     * Calculates score awarded for a hit.
     */
    fun calculateHitScore(
        precisionRatio: Float,
        currentStreak: Int
    ): HitScoreResult {
        val isPerfect = isPerfectHit(precisionRatio)
        val precisionBonus = (precisionRatio * MAX_PRECISION_BONUS).toInt()
        val perfectBonus = if (isPerfect) PERFECT_BONUS else 0
        val subtotal = BASE_SCORE + precisionBonus + perfectBonus
        val multiplier = calculateStreakMultiplier(currentStreak)
        val totalPoints = (subtotal * multiplier).toInt()

        return HitScoreResult(
            baseScore = BASE_SCORE,
            precisionBonus = precisionBonus,
            perfectBonus = perfectBonus,
            multiplier = multiplier,
            totalPoints = totalPoints,
            isPerfect = isPerfect
        )
    }
}

data class HitScoreResult(
    val baseScore: Int,
    val precisionBonus: Int,
    val perfectBonus: Int,
    val multiplier: Float,
    val totalPoints: Int,
    val isPerfect: Boolean
)
