package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator

object ChessSoundPlayer {
    enum class SoundEffect {
        MOVE,
        CAPTURE,
        CHECK,
        CASTLE,
        GAME_END,
        PUZZLE_SUCCESS,
        ERROR
    }

    fun play(effect: SoundEffect, enabled: Boolean) {
        if (!enabled) return
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 45)
            val (toneType, durationMs) = when (effect) {
                SoundEffect.MOVE -> ToneGenerator.TONE_PROP_BEEP to 35
                SoundEffect.CAPTURE -> ToneGenerator.TONE_PROP_BEEP2 to 55
                SoundEffect.CASTLE -> ToneGenerator.TONE_PROP_ACK to 65
                SoundEffect.CHECK -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 90
                SoundEffect.GAME_END -> ToneGenerator.TONE_PROP_PROMPT to 140
                SoundEffect.PUZZLE_SUCCESS -> ToneGenerator.TONE_PROP_ACK to 110
                SoundEffect.ERROR -> ToneGenerator.TONE_PROP_NACK to 80
            }
            toneGen.startTone(toneType, durationMs)
        } catch (_: Throwable) {
            // Safe fallback on headless JVM / silent hardware
        }
    }
}
