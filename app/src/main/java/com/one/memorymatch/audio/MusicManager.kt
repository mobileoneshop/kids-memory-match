package com.one.memorymatch.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build

class MusicManager(
    private val context: Context,
    var musicEnabled: Boolean = true
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var isPausedDueToFocusLoss = false
    private var audioFocusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                isPausedDueToFocusLoss = true
                pauseInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                isPausedDueToFocusLoss = true
                pauseInternal()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                if (isPausedDueToFocusLoss && musicEnabled) {
                    isPausedDueToFocusLoss = false
                    startInternal()
                }
            }
        }
    }

    fun startMusic(assetPath: String = DEFAULT_MUSIC_ASSET) {
        if (!musicEnabled) return
        if (mediaPlayer == null) {
            initMediaPlayer(assetPath)
        }
        if (requestAudioFocus()) {
            startInternal()
        }
    }

    fun pauseMusic() {
        pauseInternal()
    }

    fun stopMusic() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            abandonAudioFocus()
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error stopping music: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) {
            pauseMusic()
        } else {
            startMusic()
        }
    }

    private fun initMediaPlayer(assetPath: String) {
        try {
            val afd = context.assets.openFd(assetPath)
            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()
            player.isLooping = true
            player.prepare()
            mediaPlayer = player
        } catch (e: Exception) {
            // Music file missing or unreadable -> play silently, never crash (ERROR_HANDLING §1)
            System.err.println("KidsMemory: Could not load music asset '$assetPath': ${e.message}")
            mediaPlayer = null
        }
    }

    private fun startInternal() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                }
            }
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error starting music: ${e.message}")
        }
    }

    private fun pauseInternal() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error pausing music: ${e.message}")
        }
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()
            audioFocusRequest = request
            am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(focusChangeListener)
        }
    }

    fun release() {
        stopMusic()
    }

    companion object {
        const val DEFAULT_MUSIC_ASSET = "music/bg_loop.mp3"
    }
}
