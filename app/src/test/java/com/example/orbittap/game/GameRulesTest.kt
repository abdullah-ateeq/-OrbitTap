package com.example.orbittap.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    @Test
    fun simulation_successfulHitsSequence_advancesScoreAndDifficulty() {
        var score = 0
        var streak = 0
        var hits = 0
        var perfects = 0

        val targetCenter = 120f
        val arcSpan = DifficultyManager.calculateArcSpan(hits)

        // Player hits exactly on center
        val objAngle = 120f
        val isHit = GameMath.isHit(objAngle, targetCenter, arcSpan)
        assertTrue(isHit)

        val precision = GameMath.calculatePrecision(objAngle, targetCenter, arcSpan)
        assertEquals(1.0f, precision, 0.001f)

        val scoreResult = GameMath.calculateHitScore(precision, streak)
        assertTrue(scoreResult.isPerfect)
        assertEquals(250, scoreResult.totalPoints)

        score += scoreResult.totalPoints
        streak++
        hits++
        perfects++

        assertEquals(250, score)
        assertEquals(1, streak)
        assertEquals(1, hits)
        assertEquals(1, perfects)

        // Check difficulty stepped forward
        val nextSpeed = DifficultyManager.calculateSpeed(hits)
        val nextArcSpan = DifficultyManager.calculateArcSpan(hits)
        assertTrue(nextSpeed > DifficultyManager.INITIAL_SPEED)
        assertTrue(nextArcSpan < DifficultyManager.INITIAL_ARC_SPAN)
    }

    @Test
    fun simulation_missEndsGame() {
        val targetCenter = 90f
        val arcSpan = 40f // span is 70°..110°

        // Object is at 200° (far outside)
        val objAngle = 200f
        val isHit = GameMath.isHit(objAngle, targetCenter, arcSpan)
        assertFalse("Should be a miss", isHit)

        val precision = GameMath.calculatePrecision(objAngle, targetCenter, arcSpan)
        assertEquals(0f, precision, 0.001f)
    }
}
