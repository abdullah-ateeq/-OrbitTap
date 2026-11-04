package com.example.orbittap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.orbittap.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "orbit_tap_prefs")

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val bestScore: Int = 0,
    val highestStreak: Int = 0
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val BEST_SCORE = intPreferencesKey("best_score")
        val HIGHEST_STREAK = intPreferencesKey("highest_streak")
    }

    val preferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val themeString = prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name
        val themeMode = try {
            AppThemeMode.valueOf(themeString)
        } catch (_: Exception) {
            AppThemeMode.SYSTEM
        }
        val sound = prefs[Keys.SOUND_ENABLED] ?: true
        val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
        val bestScore = prefs[Keys.BEST_SCORE] ?: 0
        val highestStreak = prefs[Keys.HIGHEST_STREAK] ?: 0

        UserPreferences(
            themeMode = themeMode,
            soundEnabled = sound,
            hapticsEnabled = haptics,
            bestScore = bestScore,
            highestStreak = highestStreak
        )
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateScores(score: Int, streak: Int): Boolean {
        var isNewBest = false
        context.dataStore.edit { prefs ->
            val currentBestScore = prefs[Keys.BEST_SCORE] ?: 0
            val currentHighestStreak = prefs[Keys.HIGHEST_STREAK] ?: 0

            if (score > currentBestScore) {
                prefs[Keys.BEST_SCORE] = score
                isNewBest = true
            }
            if (streak > currentHighestStreak) {
                prefs[Keys.HIGHEST_STREAK] = streak
            }
        }
        return isNewBest
    }

    suspend fun resetBestStats() {
        context.dataStore.edit { prefs ->
            prefs[Keys.BEST_SCORE] = 0
            prefs[Keys.HIGHEST_STREAK] = 0
        }
    }
}
