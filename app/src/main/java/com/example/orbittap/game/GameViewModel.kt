package com.example.orbittap.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.orbittap.audio.GameAudioEngine
import com.example.orbittap.audio.GameHaptics
import com.example.orbittap.data.UserPreferences
import com.example.orbittap.data.UserPreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)
    val audioEngine = GameAudioEngine()
    val haptics = GameHaptics(application)

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var loopJob: Job? = null
    private var nextParticleId = 0L
    private var nextPopupId = 0L

    init {
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                audioEngine.isEnabled = prefs.soundEnabled
                haptics.isEnabled = prefs.hapticsEnabled
                _gameState.update { it.copy(bestScore = prefs.bestScore) }
            }
        }
        startNewGame()
    }

    fun startNewGame() {
        loopJob?.cancel()

        val initialAngle = 0f
        val initialDirection = 1f
        val initialSpan = DifficultyManager.INITIAL_ARC_SPAN
        val initialSpeed = DifficultyManager.INITIAL_SPEED
        val initialTarget = DifficultyManager.generateNextTargetAngle(
            currentObjectAngle = initialAngle,
            direction = initialDirection
        )

        _gameState.value = GameState(
            status = GameStatus.RUNNING,
            objectAngle = initialAngle,
            targetAngle = initialTarget,
            targetSpan = initialSpan,
            direction = initialDirection,
            speed = initialSpeed,
            score = 0,
            streak = 0,
            highestStreakInRun = 0,
            successfulHits = 0,
            perfectHits = 0,
            bestScore = userPreferences.value.bestScore,
            isNewBestScore = false,
            particles = emptyList(),
            scorePopups = emptyList(),
            hitPulseProgress = 0f,
            shakeOffset = 0f,
            isMissFlashing = false
        )

        startLoop()
    }

    private fun startLoop() {
        loopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTimeNanos = now

                val current = _gameState.value
                if (current.status == GameStatus.RUNNING) {
                    val angleAdvance = current.speed * current.direction * dt
                    val newAngle = GameMath.normalizeAngle(current.objectAngle + angleAdvance)

                    // Update particles
                    val updatedParticles = current.particles.mapNotNull { p ->
                        val newAlpha = p.alpha - (dt * 2.2f)
                        if (newAlpha <= 0f) null
                        else p.copy(
                            xRatio = p.xRatio + (p.vx * dt),
                            yRatio = p.yRatio + (p.vy * dt),
                            alpha = newAlpha
                        )
                    }

                    // Update popups
                    val updatedPopups = current.scorePopups.mapNotNull { popup ->
                        val newAlpha = popup.alpha - (dt * 1.6f)
                        if (newAlpha <= 0f) null
                        else popup.copy(
                            offsetY = popup.offsetY - (dt * 45f),
                            alpha = newAlpha
                        )
                    }

                    // Damping hit pulse & shake
                    val newHitPulse = (current.hitPulseProgress - (dt * 3.5f)).coerceAtLeast(0f)
                    val newShake = (current.shakeOffset - (dt * 60f)).coerceAtLeast(0f)

                    _gameState.value = current.copy(
                        objectAngle = newAngle,
                        particles = updatedParticles,
                        scorePopups = updatedPopups,
                        hitPulseProgress = newHitPulse,
                        shakeOffset = newShake
                    )
                } else if (current.status == GameStatus.GAME_OVER && current.shakeOffset > 0f) {
                    val newShake = (current.shakeOffset - (dt * 60f)).coerceAtLeast(0f)
                    _gameState.value = current.copy(shakeOffset = newShake)
                }

                // Smooth ~60-120fps frame delay
                delay(16)
            }
        }
    }

    fun onTap() {
        val current = _gameState.value
        if (current.status != GameStatus.RUNNING) return

        val hit = GameMath.isHit(current.objectAngle, current.targetAngle, current.targetSpan)

        if (hit) {
            handleHit(current)
        } else {
            handleMiss(current)
        }
    }

    private fun handleHit(current: GameState) {
        val precisionRatio = GameMath.calculatePrecision(
            current.objectAngle,
            current.targetAngle,
            current.targetSpan
        )
        val scoreResult = GameMath.calculateHitScore(precisionRatio, current.streak)

        val newStreak = current.streak + 1
        val newHighestStreak = max(current.highestStreakInRun, newStreak)
        val newHits = current.successfulHits + 1
        val newPerfects = if (scoreResult.isPerfect) current.perfectHits + 1 else current.perfectHits
        val newScore = current.score + scoreResult.totalPoints
        val isNewBest = newScore > current.bestScore

        // Trigger Audio & Haptics
        if (scoreResult.isPerfect) {
            audioEngine.playPerfect(newStreak)
            haptics.playPerfectHaptic()
        } else {
            audioEngine.playHit(newStreak)
            haptics.playHitHaptic()
        }

        // Spawn hit particles
        val hitAngleRad = Math.toRadians(current.objectAngle.toDouble())
        val hitX = cos(hitAngleRad).toFloat()
        val hitY = sin(hitAngleRad).toFloat()

        val newParticles = mutableListOf<Particle>()
        val particleCount = if (scoreResult.isPerfect) 18 else 12
        for (i in 0 until particleCount) {
            val speed = Random.nextFloat() * 0.7f + 0.2f
            val spreadAngle = hitAngleRad + (Random.nextFloat() - 0.5f) * 1.5
            val vx = (cos(spreadAngle) * speed).toFloat()
            val vy = (sin(spreadAngle) * speed).toFloat()
            newParticles.add(
                Particle(
                    id = nextParticleId++,
                    xRatio = hitX,
                    yRatio = hitY,
                    vx = vx,
                    vy = vy,
                    alpha = 1.0f,
                    radius = if (scoreResult.isPerfect) 4.5f else 3.2f,
                    colorType = if (scoreResult.isPerfect) 1 else 0
                )
            )
        }

        // Spawn floating score popup
        val popupText = if (scoreResult.isPerfect) {
            "PERFECT! +${scoreResult.totalPoints}"
        } else {
            "+${scoreResult.totalPoints}"
        }
        val newPopup = ScorePopup(
            id = nextPopupId++,
            text = popupText,
            isPerfect = scoreResult.isPerfect,
            alpha = 1.0f,
            offsetY = 0f
        )

        // Difficulty adjustments
        val newSpeed = DifficultyManager.calculateSpeed(newHits)
        val newSpan = DifficultyManager.calculateArcSpan(newHits)
        val newDirection = DifficultyManager.determineDirection(newStreak, current.direction)
        val nextTarget = DifficultyManager.generateNextTargetAngle(
            currentObjectAngle = current.objectAngle,
            direction = newDirection
        )

        _gameState.value = current.copy(
            targetAngle = nextTarget,
            targetSpan = newSpan,
            direction = newDirection,
            speed = newSpeed,
            score = newScore,
            streak = newStreak,
            highestStreakInRun = newHighestStreak,
            successfulHits = newHits,
            perfectHits = newPerfects,
            isNewBestScore = isNewBest,
            particles = current.particles + newParticles,
            scorePopups = listOf(newPopup),
            hitPulseProgress = 1.0f,
            isMissFlashing = false
        )
    }

    private fun handleMiss(current: GameState) {
        audioEngine.playMiss()
        haptics.playMissHaptic()

        val finalScore = current.score
        val finalStreak = current.highestStreakInRun
        val isNewBest = finalScore > current.bestScore

        _gameState.value = current.copy(
            status = GameStatus.GAME_OVER,
            shakeOffset = 18f,
            isMissFlashing = true,
            isNewBestScore = isNewBest
        )

        viewModelScope.launch {
            preferencesRepository.updateScores(finalScore, finalStreak)
        }
    }

    fun pauseGame() {
        if (_gameState.value.status == GameStatus.RUNNING) {
            _gameState.update { it.copy(status = GameStatus.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.status == GameStatus.PAUSED) {
            _gameState.update { it.copy(status = GameStatus.RUNNING) }
        }
    }

    fun setThemeMode(mode: com.example.orbittap.ui.theme.AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEnabled(enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }

    fun resetBestScore() {
        viewModelScope.launch {
            preferencesRepository.resetBestStats()
            _gameState.update { it.copy(bestScore = 0, isNewBestScore = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
    }
}
