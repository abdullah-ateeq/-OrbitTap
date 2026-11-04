package com.example.orbittap.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DifficultyManagerTest {

    private val delta = 0.001f

    @Test
    fun calculateSpeed_increasesWithHitsAndCapsAtMax() {
        val initialSpeed = DifficultyManager.calculateSpeed(0)
        assertEquals(DifficultyManager.INITIAL_SPEED, initialSpeed, delta)

        val speedAt10 = DifficultyManager.calculateSpeed(10)
        assertTrue(speedAt10 > initialSpeed)
        assertEquals(DifficultyManager.INITIAL_SPEED + 10 * DifficultyManager.SPEED_STEP_PER_HIT, speedAt10, delta)

        val speedAtMax = DifficultyManager.calculateSpeed(100)
        assertEquals(DifficultyManager.MAX_SPEED, speedAtMax, delta)
    }

    @Test
    fun calculateArcSpan_shrinksWithHitsAndFloorsAtMin() {
        val initialSpan = DifficultyManager.calculateArcSpan(0)
        assertEquals(DifficultyManager.INITIAL_ARC_SPAN, initialSpan, delta)

        val spanAt10 = DifficultyManager.calculateArcSpan(10)
        assertTrue(spanAt10 < initialSpan)
        assertEquals(DifficultyManager.INITIAL_ARC_SPAN - 10 * DifficultyManager.ARC_STEP_PER_HIT, spanAt10, delta)

        val spanAtMax = DifficultyManager.calculateArcSpan(100)
        assertEquals(DifficultyManager.MIN_ARC_SPAN, spanAtMax, delta)
    }

    @Test
    fun generateNextTargetAngle_positionsAheadWithinFairBounds() {
        val currentObjAngle = 40f
        val direction = 1f // clockwise

        // Fraction = 0.0 -> minimum offset
        val minTarget = DifficultyManager.generateNextTargetAngle(
            currentObjectAngle = currentObjAngle,
            direction = direction,
            randomFraction = 0.0f
        )
        val expectedMin = GameMath.normalizeAngle(currentObjAngle + DifficultyManager.MIN_SPAWN_AHEAD_OFFSET)
        assertEquals(expectedMin, minTarget, delta)

        // Fraction = 1.0 -> maximum offset
        val maxTarget = DifficultyManager.generateNextTargetAngle(
            currentObjectAngle = currentObjAngle,
            direction = direction,
            randomFraction = 1.0f
        )
        val expectedMax = GameMath.normalizeAngle(currentObjAngle + DifficultyManager.MAX_SPAWN_AHEAD_OFFSET)
        assertEquals(expectedMax, maxTarget, delta)
    }

    @Test
    fun generateNextTargetAngle_handlesCounterClockwiseDirection() {
        val currentObjAngle = 50f
        val direction = -1f // counter-clockwise

        val minTarget = DifficultyManager.generateNextTargetAngle(
            currentObjectAngle = currentObjAngle,
            direction = direction,
            randomFraction = 0.0f
        )
        // 50 - 100 = -50 = 310°
        assertEquals(310f, minTarget, delta)
    }

    @Test
    fun determineDirection_flipsAtThresholds() {
        // Below streak 12, stays the same
        assertEquals(1f, DifficultyManager.determineDirection(streak = 5, currentDirection = 1f), delta)
        assertEquals(1f, DifficultyManager.determineDirection(streak = 11, currentDirection = 1f), delta)

        // At streak 12, direction swaps
        assertEquals(-1f, DifficultyManager.determineDirection(streak = 12, currentDirection = 1f), delta)
        assertEquals(1f, DifficultyManager.determineDirection(streak = 12, currentDirection = -1f), delta)
    }
}
