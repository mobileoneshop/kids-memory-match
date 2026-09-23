package com.one.memorymatch.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class Speaker(
    context: Context,
    var enabled: Boolean = true
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    configureTts()
                } else {
                    // Init failed -> disable speech silently (ERROR_HANDLING §2)
                    System.err.println("KidsMemory: TTS initialization failed with status $status")
                    isInitialized = false
                }
            }
        } catch (e: Exception) {
            System.err.println("KidsMemory: TTS engine missing or inaccessible: ${e.message}")
            isInitialized = false
        }
    }

    private fun configureTts() {
        val ttsEngine = tts ?: return
        try {
            var result = ttsEngine.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to Locale.ENGLISH (ERROR_HANDLING §2)
                result = ttsEngine.setLanguage(Locale.ENGLISH)
            }

            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                // TRD §1: en-US, pitch 1.1, rate 0.95
                ttsEngine.setPitch(1.1f)
                ttsEngine.setSpeechRate(0.95f)
                isInitialized = true
            } else {
                System.err.println("KidsMemory: English language data missing in TTS")
                isInitialized = false
            }
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error configuring TTS: ${e.message}")
            isInitialized = false
        }
    }

    fun speakName(text: String) {
        if (!enabled || !isInitialized || text.isBlank()) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ITEM_NAME)
        } catch (e: Exception) {
            System.err.println("KidsMemory: Failed to speak '$text': ${e.message}")
        }
    }

    fun celebrate() {
        val cheers = listOf("Great job!", "You did it!", "Awesome!", "Super!")
        speakName(cheers.random())
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error stopping TTS: ${e.message}")
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            System.err.println("KidsMemory: Error shutting down TTS: ${e.message}")
        }
    }

    companion object {
        private const val UTTERANCE_ITEM_NAME = "item_name"
    }
}
