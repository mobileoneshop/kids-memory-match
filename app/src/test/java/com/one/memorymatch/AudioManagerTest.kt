package com.one.memorymatch

import com.one.memorymatch.audio.MusicManager
import com.one.memorymatch.audio.SoundManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioManagerTest {

    @Test
    fun soundManager_constantsAreCorrect() {
        assertEquals("flip", SoundManager.SOUND_FLIP)
        assertEquals("match", SoundManager.SOUND_MATCH)
        assertEquals("mismatch", SoundManager.SOUND_MISMATCH)
        assertEquals("win", SoundManager.SOUND_WIN)
        assertEquals("click", SoundManager.SOUND_CLICK)
        assertEquals("star", SoundManager.SOUND_STAR)
    }

    @Test
    fun musicManager_defaultAssetIsConfigured() {
        assertEquals("music/bg_loop.mp3", MusicManager.DEFAULT_MUSIC_ASSET)
    }

    @Test
    fun uiAudioFiles_allExistAndHaveValidHeaders() {
        val requiredSounds = listOf(
            "sounds/ui/flip.mp3",
            "sounds/ui/match.mp3",
            "sounds/ui/mismatch.mp3",
            "sounds/ui/win.mp3",
            "sounds/ui/click.mp3",
            "sounds/ui/star.mp3",
            "music/bg_loop.mp3"
        )

        for (relPath in requiredSounds) {
            val file = File("src/main/assets", relPath).let {
                if (it.exists()) it else File("../app/src/main/assets", relPath)
            }
            assertTrue("Audio asset must exist: $relPath", file.exists())
            assertTrue("Audio asset must not be empty: $relPath", file.length() > 100)

            // Check RIFF / ID3 audio header bytes
            val header = ByteArray(4)
            file.inputStream().use { it.read(header) }
            val magic = String(header)
            assertTrue("Audio asset $relPath must have RIFF or ID3 format, got: $magic", magic == "RIFF" || magic.startsWith("ID3"))
        }
    }
}
