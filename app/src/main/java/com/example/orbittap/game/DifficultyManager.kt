package com.example.orbittap.game

import kotlin.random.Random

object DifficultyManager {

    const val INITIAL_SPEED = 130f // degrees per second (~2.77 sec per full rotation)
    const val MAX_SPEED = 340f // degrees per second (~1.05 sec per full rotation)
    const val SPEED_STEP_PER_HIT = 6.5f

    const val INITIAL_ARC_SPAN = 50f // degrees
    const val MIN_ARC_SPAN = 18f // degrees
    const val ARC_STEP_PER_HIT = 1.1f

    const val MIN_SPAWN_AHEAD_OFFSET = 100f // degrees ahead of object
    const val MAX_SPAWN_AHEAD_OFFSET = 260f // degrees ahead of object

    /**
     * Calculates the rotation speed (degrees / second) based on hits.
     */
    fun calculateSpeed(hits: Int): Float {
        val calculated = INITIAL_SPEED + (hits.coerceAtLeast(0) * SPEED_STEP_PER_HIT)
        return calculated.coerceAtMost(MAX_SPEED)
    }

    /**
     * Calculates target arc span (in degrees) based on hits.
     */
    fun calculateArcSpan(hits: Int): Float {
        val calculated = INITIAL_ARC_SPAN - (hits.coerceAtLeast(0) * ARC_STEP_PER_HIT)
        return calculated.coerceAtLeast(MIN_ARC_SPAN)
    }

    /**
     * Generates a new target center angle positioned safely ahead of the current object position
     * based on the rotation direction.
     * This guarantees the target doesn't spawn immediately under or right in front of the runner,
     * giving the player a fair reaction window.
     */
    fun generateNextTargetAngle(
        currentObjectAngle: Float,
        direction: Float = 1f,
        minOffset: Float = MIN_SPAWN_AHEAD_OFFSET,
        maxOffset: Float = MAX_SPAWN_AHEAD_OFFSET,
        randomFraction: Float = Random.nextFloat()
    ): Float {
        val span = maxOffset - minOffset
        val offset = minOffset + (randomFraction.coerceIn(0f, 1f) * span)
        val dirSign = if (direction >= 0f) 1f else -1f
        val newAngle = currentObjectAngle + (dirSign * offset)
        return GameMath.normalizeAngle(newAngle)
    }

    /**
     * Determines rotation direction. For beginner & intermediate levels, keeps clockwise.
     * At streak >= 12, direction swaps every 6 streak increments to introduce an exciting challenge.
     */
    fun determineDirection(streak: Int, currentDirection: Float = 1f): Float {
        if (streak >= 12 && streak % 6 == 0) {
            return -currentDirection
        }
        return if (currentDirection == 0f) 1f else currentDirection
    }
}
