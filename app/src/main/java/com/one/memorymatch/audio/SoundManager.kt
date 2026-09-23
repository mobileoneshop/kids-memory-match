package com.one.memorymatch.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.IOException

class SoundManager(
    private val context: Context,
    var soundEnabled: Boolean = true
) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<String, Int>()
    private var volume = 1.0f

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        preloadUiSounds()
    }

    private fun preloadUiSounds() {
        val uiSounds = listOf(
            SOUND_FLIP to "sounds/ui/flip.mp3",
            SOUND_MATCH to "sounds/ui/match.mp3",
            SOUND_MISMATCH to "sounds/ui/mismatch.mp3",
            SOUND_WIN to "sounds/ui/win.mp3",
            SOUND_CLICK to "sounds/ui/click.mp3",
            SOUND_STAR to "sounds/ui/star.mp3"
        )

        for ((key, assetPath) in uiSounds) {
            loadAssetSound(key, assetPath)
        }
    }

    fun loadAssetSound(key: String, assetPath: String): Int {
        if (soundMap.containsKey(key)) {
            return soundMap[key] ?: 0
        }
        return try {
            val afd = context.assets.openFd(assetPath)
            val soundId = soundPool.load(afd, 1)
            soundMap[key] = soundId
            soundId
        } catch (e: Exception) {
            // Missing asset or load fail -> graceful fallback, never crash (ERROR_HANDLING §1)
            System.err.println("KidsMemory: Could not load sound asset '$assetPath': ${e.message}")
            0
        }
    }

    private fun play(key: String) {
        if (!soundEnabled) return
        val soundId = soundMap[key] ?: return
        if (soundId == 0) return

        try {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error playing sound '$key': ${e.message}")
        }
    }

    fun playFlip() = play(SOUND_FLIP)
    fun playMatch() = play(SOUND_MATCH)
    fun playMismatch() = play(SOUND_MISMATCH)
    fun playWin() = play(SOUND_WIN)
    fun playClick() = play(SOUND_CLICK)
    fun playStar() = play(SOUND_STAR)

    fun playItemSound(soundAsset: String?): Boolean {
        if (!soundEnabled || soundAsset.isNullOrBlank()) return false
        val soundId = loadAssetSound(soundAsset, soundAsset)
        if (soundId > 0) {
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
            return true
        }
        return false
    }

    fun setVolume(newVolume: Float) {
        volume = newVolume.coerceIn(0.0f, 1.0f)
    }

    fun release() {
        try {
            soundPool.release()
            soundMap.clear()
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error releasing SoundPool: ${e.message}")
        }
    }

    companion object {
        const val SOUND_FLIP = "flip"
        const val SOUND_MATCH = "match"
        const val SOUND_MISMATCH = "mismatch"
        const val SOUND_WIN = "win"
        const val SOUND_CLICK = "click"
        const val SOUND_STAR = "star"
    }
}
