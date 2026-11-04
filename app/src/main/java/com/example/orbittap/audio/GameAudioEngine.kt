package com.example.orbittap.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class GameAudioEngine {

    private val sampleRate = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var isEnabled: Boolean = true

    /**
     * Synthesizes and plays a smooth sine wave tone with attack and decay envelopes.
     */
    private fun playTone(
        startFreq: Float,
        endFreq: Float = startFreq,
        durationMs: Int,
        volume: Float = 0.5f,
        harmonic: Boolean = false
    ) {
        if (!isEnabled) return

        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch
                val buffer = ShortArray(numSamples)

                var currentFreq = startFreq
                val freqStep = (endFreq - startFreq) / numSamples.toFloat()
                var phase = 0.0

                val attackSamples = (sampleRate * 0.005f).toInt().coerceAtLeast(1)
                val decaySamples = (sampleRate * 0.02f).toInt().coerceAtLeast(1)

                for (i in 0 until numSamples) {
                    val envelope = when {
                        i < attackSamples -> i.toFloat() / attackSamples
                        i > numSamples - decaySamples -> (numSamples - i).toFloat() / decaySamples
                        else -> 1.0f
                    }

                    var sample = sin(phase)
                    if (harmonic) {
                        sample = (sample * 0.7) + (sin(phase * 1.5) * 0.3)
                    }

                    val shortVal = (sample * envelope * volume * Short.MAX_VALUE).toInt()
                    buffer[i] = shortVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

                    phase += 2.0 * Math.PI * currentFreq / sampleRate
                    currentFreq += freqStep
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                // Release track after playback completes
                launch {
                    kotlinx.coroutines.delay(durationMs.toLong() + 50L)
                    try {
                        audioTrack.stop()
                        audioTrack.release()
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                // Audio fallback / non-fatal
            }
        }
    }

    /**
     * Hit sound: scales in frequency with current streak for exciting audio feedback.
     */
    fun playHit(streak: Int) {
        val baseFreq = 520f
        val pitchStep = (streak.coerceAtMost(16) * 28f)
        val frequency = baseFreq + pitchStep
        playTone(
            startFreq = frequency,
            endFreq = frequency + 40f,
            durationMs = 90,
            volume = 0.45f
        )
    }

    /**
     * Perfect hit sound: bright harmonic crystal chime.
     */
    fun playPerfect(streak: Int) {
        val baseFreq = 780f + (streak.coerceAtMost(16) * 25f)
        playTone(
            startFreq = baseFreq,
            endFreq = baseFreq + 120f,
            durationMs = 150,
            volume = 0.55f,
            harmonic = true
        )
    }

    /**
     * Miss sound: low descending arcade thud.
     */
    fun playMiss() {
        playTone(
            startFreq = 220f,
            endFreq = 75f,
            durationMs = 220,
            volume = 0.6f
        )
    }

    /**
     * Subtle UI button click.
     */
    fun playUiClick() {
        playTone(
            startFreq = 640f,
            endFreq = 720f,
            durationMs = 40,
            volume = 0.25f
        )
    }
}
