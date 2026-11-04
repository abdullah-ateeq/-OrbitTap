package com.example.orbittap.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameMathTest {

    private val delta = 0.001f

    @Test
    fun normalizeAngle_withinRange_remainsSame() {
        assertEquals(0f, GameMath.normalizeAngle(0f), delta)
        assertEquals(180f, GameMath.normalizeAngle(180f), delta)
        assertEquals(359.9f, GameMath.normalizeAngle(359.9f), delta)
    }

    @Test
    fun normalizeAngle_wrapsOver360() {
        assertEquals(0f, GameMath.normalizeAngle(360f), delta)
        assertEquals(15f, GameMath.normalizeAngle(375f), delta)
        assertEquals(30f, GameMath.normalizeAngle(750f), delta)
    }

    @Test
    fun normalizeAngle_handlesNegativeAngles() {
        assertEquals(350f, GameMath.normalizeAngle(-10f), delta)
        assertEquals(180f, GameMath.normalizeAngle(-180f), delta)
        assertEquals(330f, GameMath.normalizeAngle(-390f), delta)
    }

    @Test
    fun shortestAngularDistance_normalAngles() {
        assertEquals(20f, GameMath.shortestAngularDistance(10f, 30f), delta)
        assertEquals(20f, GameMath.shortestAngularDistance(30f, 10f), delta)
        assertEquals(0f, GameMath.shortestAngularDistance(45f, 45f), delta)
    }

    @Test
    fun shortestAngularDistance_acrossBoundary0And360() {
        // 355° to 5° should be 10°
        assertEquals(10f, GameMath.shortestAngularDistance(355f, 5f), delta)
        assertEquals(10f, GameMath.shortestAngularDistance(5f, 355f), delta)

        // 350° to 10° should be 20°
        assertEquals(20f, GameMath.shortestAngularDistance(350f, 10f), delta)
        assertEquals(20f, GameMath.shortestAngularDistance(10f, 350f), delta)

        // 1° to 359° should be 2°
        assertEquals(2f, GameMath.shortestAngularDistance(1f, 359f), delta)

        // Opposite points
        assertEquals(180f, GameMath.shortestAngularDistance(0f, 180f), delta)
        assertEquals(180f, GameMath.shortestAngularDistance(90f, 270f), delta)
    }

    @Test
    fun isHit_correctlyIdentifiesHitsAndMisses() {
        val targetCenter = 90f
        val arcSpan = 40f // half span = 20° (70° to 110°)

        // Center hit
        assertTrue(GameMath.isHit(90f, targetCenter, arcSpan))

        // Inside arc
        assertTrue(GameMath.isHit(75f, targetCenter, arcSpan))
        assertTrue(GameMath.isHit(105f, targetCenter, arcSpan))

        // Exact boundaries
        assertTrue(GameMath.isHit(70f, targetCenter, arcSpan))
        assertTrue(GameMath.isHit(110f, targetCenter, arcSpan))

        // Outside arc
        assertFalse(GameMath.isHit(69f, targetCenter, arcSpan))
        assertFalse(GameMath.isHit(111f, targetCenter, arcSpan))
        assertFalse(GameMath.isHit(270f, targetCenter, arcSpan))
    }

    @Test
    fun isHit_acrossBoundary() {
        val targetCenter = 0f
        val arcSpan = 30f // half span = 15° (345° to 15°)

        assertTrue(GameMath.isHit(0f, targetCenter, arcSpan))
        assertTrue(GameMath.isHit(350f, targetCenter, arcSpan))
        assertTrue(GameMath.isHit(10f, targetCenter, arcSpan))
        assertTrue(GameMath.isHit(345f, targetCenter, arcSpan))
        assertTrue(GameMath.isHit(15f, targetCenter, arcSpan))

        assertFalse(GameMath.isHit(344f, targetCenter, arcSpan))
        assertFalse(GameMath.isHit(16f, targetCenter, arcSpan))
        assertFalse(GameMath.isHit(180f, targetCenter, arcSpan))
    }

    @Test
    fun calculatePrecision_centerYieldsMaxPrecision() {
        val precision = GameMath.calculatePrecision(90f, 90f, 40f)
        assertEquals(1.0f, precision, delta)
    }

    @Test
    fun calculatePrecision_boundaryYieldsZero() {
        val precision = GameMath.calculatePrecision(110f, 90f, 40f)
        assertEquals(0.0f, precision, delta)
    }

    @Test
    fun calculatePrecision_halfwayYieldsHalf() {
        // Distance is 10°, half span is 20° -> precision 0.5
        val precision = GameMath.calculatePrecision(100f, 90f, 40f)
        assertEquals(0.5f, precision, delta)
    }

    @Test
    fun isPerfectHit_thresholdDetection() {
        assertTrue(GameMath.isPerfectHit(1.0f))
        assertTrue(GameMath.isPerfectHit(0.85f))
        assertFalse(GameMath.isPerfectHit(0.75f))
        assertFalse(GameMath.isPerfectHit(0.0f))
    }

    @Test
    fun calculateStreakMultiplier_progressiveSteps() {
        assertEquals(1.0f, GameMath.calculateStreakMultiplier(0), delta)
        assertEquals(1.0f, GameMath.calculateStreakMultiplier(1), delta)
        assertEquals(1.0f, GameMath.calculateStreakMultiplier(2), delta)
        assertEquals(1.25f, GameMath.calculateStreakMultiplier(3), delta)
        assertEquals(1.25f, GameMath.calculateStreakMultiplier(5), delta)
        assertEquals(1.5f, GameMath.calculateStreakMultiplier(6), delta)
        assertEquals(2.0f, GameMath.calculateStreakMultiplier(12), delta)
    }

    @Test
    fun calculateHitScore_precisionAndPerfectBonus() {
        // Perfect hit at streak 0 (multiplier 1.0x)
        val perfectResult = GameMath.calculateHitScore(precisionRatio = 1.0f, currentStreak = 0)
        assertEquals(GameMath.BASE_SCORE, perfectResult.baseScore)
        assertEquals(GameMath.MAX_PRECISION_BONUS, perfectResult.precisionBonus)
        assertEquals(GameMath.PERFECT_BONUS, perfectResult.perfectBonus)
        assertTrue(perfectResult.isPerfect)
        assertEquals(250, perfectResult.totalPoints)

        // Perfect hit at streak 6 (multiplier 1.5x)
        val highStreakResult = GameMath.calculateHitScore(precisionRatio = 1.0f, currentStreak = 6)
        assertEquals(1.5f, highStreakResult.multiplier, delta)
        assertEquals((250 * 1.5f).toInt(), highStreakResult.totalPoints)
    }
}
