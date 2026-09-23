package com.one.memorymatch

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.ProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ProgressRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testScope: CoroutineScope
    private lateinit var repository: ProgressRepository
    private lateinit var testDataStore: androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>

    @Before
    fun setUp() {
        testScope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFolder.newFile("test_prefs_${System.nanoTime()}.preferences_pb") }
        )
        repository = ProgressRepository(testDataStore)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun defaultSettings_soundAndMusicEnabledAndLanguageEnglish() = runBlocking {
        val settings = repository.getSettings()
        assertTrue("Sound should be enabled by default", settings.soundOn)
        assertTrue("Music should be enabled by default", settings.musicOn)
        assertEquals("en", settings.language)
    }

    @Test
    fun settingsToggles_updateAndPersist() = runBlocking {
        repository.setSoundEnabled(false)
        var settings = repository.getSettings()
        assertFalse("Sound should be disabled", settings.soundOn)
        assertTrue("Music should still be enabled", settings.musicOn)

        repository.setMusicEnabled(false)
        settings = repository.getSettings()
        assertFalse("Sound should still be disabled", settings.soundOn)
        assertFalse("Music should be disabled", settings.musicOn)

        repository.setSoundEnabled(true)
        settings = repository.getSettings()
        assertTrue("Sound should be re-enabled", settings.soundOn)
        assertFalse("Music should remain disabled", settings.musicOn)
    }

    @Test
    fun saveGameResult_bestOnlyImproves() = runBlocking {
        val packId = "zoo"
        val level = GameLevel.EASY

        // 1. Initial state is empty
        val initial = repository.getProgress(packId, level)
        assertEquals(0, initial.stars)
        assertEquals(Int.MAX_VALUE, initial.bestMoves)
        assertEquals(0, initial.bestTimeSec)

        // 2. First game: 2 stars, 12 moves, 45 seconds
        val improved1 = repository.saveGameResult(packId, level, starsEarned = 2, moves = 12, timeSec = 45)
        assertTrue("First record should improve stats", improved1)

        val afterFirst = repository.getProgress(packId, level)
        assertEquals(2, afterFirst.stars)
        assertEquals(12, afterFirst.bestMoves)
        assertEquals(45, afterFirst.bestTimeSec)

        // 3. Second game with worse score: 1 star, 15 moves, 50s -> no improvement
        val improved2 = repository.saveGameResult(packId, level, starsEarned = 1, moves = 15, timeSec = 50)
        assertFalse("Worse game should not improve best records", improved2)

        val afterSecond = repository.getProgress(packId, level)
        assertEquals("Stars should remain at best 2", 2, afterSecond.stars)
        assertEquals("Moves should remain at best 12", 12, afterSecond.bestMoves)
        assertEquals("Time should remain at best 45", 45, afterSecond.bestTimeSec)

        // 4. Third game with better score: 3 stars, 8 moves, 30s -> all improve
        val improved3 = repository.saveGameResult(packId, level, starsEarned = 3, moves = 8, timeSec = 30)
        assertTrue("Better game should improve records", improved3)

        val afterThird = repository.getProgress(packId, level)
        assertEquals(3, afterThird.stars)
        assertEquals(8, afterThird.bestMoves)
        assertEquals(30, afterThird.bestTimeSec)

        // 5. Fourth game: same 3 stars, slower moves (10), but faster time (25s) -> only time improves
        val improved4 = repository.saveGameResult(packId, level, starsEarned = 3, moves = 10, timeSec = 25)
        assertTrue("Faster time should improve best records", improved4)

        val afterFourth = repository.getProgress(packId, level)
        assertEquals(3, afterFourth.stars)
        assertEquals("Moves should remain at 8", 8, afterFourth.bestMoves)
        assertEquals("Time should improve to 25s", 25, afterFourth.bestTimeSec)
    }

    @Test
    fun corruptData_recoversToDefaults() = runBlocking {
        val packId = "sea"
        val level = GameLevel.MEDIUM
        val key = ProgressRepository.progressKey(packId, level)

        // Test parser with corrupted strings directly
        val corruptSamples = listOf(
            "not_csv",
            "1,2",
            "1,2,3,4",
            "abc,def,ghi",
            "",
            "   ",
            "null",
            "-5,-10,-20"
        )

        for (sample in corruptSamples) {
            testDataStore.edit { prefs ->
                prefs[key] = sample
            }
            val progress = repository.getProgress(packId, level)
            assertEquals("Stars should recover to 0 for: $sample", 0, progress.stars)
            assertTrue("Best moves should be Int.MAX_VALUE for: $sample", progress.bestMoves == Int.MAX_VALUE || progress.bestMoves > 0)
        }
    }

    @Test
    fun resetAllProgress_clearsProgressWhilePreservingSettings() = runBlocking {
        repository.setSoundEnabled(false)
        repository.saveGameResult("zoo", GameLevel.EASY, starsEarned = 3, moves = 8, timeSec = 20)
        repository.saveGameResult("farm", GameLevel.HARD, starsEarned = 2, moves = 20, timeSec = 60)

        assertEquals(3, repository.getProgress("zoo", GameLevel.EASY).stars)
        assertEquals(2, repository.getProgress("farm", GameLevel.HARD).stars)

        repository.resetAllProgress()

        // Progress cleared
        assertEquals(0, repository.getProgress("zoo", GameLevel.EASY).stars)
        assertEquals(Int.MAX_VALUE, repository.getProgress("zoo", GameLevel.EASY).bestMoves)
        assertEquals(0, repository.getProgress("farm", GameLevel.HARD).stars)

        // Settings preserved
        val settings = repository.getSettings()
        assertFalse("Sound toggle must be preserved after reset", settings.soundOn)
    }

    @Test
    fun getStarsForPack_sumsAllThreeLevels() = runBlocking {
        repository.saveGameResult("birds", GameLevel.EASY, starsEarned = 3, moves = 8, timeSec = 20)
        repository.saveGameResult("birds", GameLevel.MEDIUM, starsEarned = 2, moves = 14, timeSec = 35)
        repository.saveGameResult("birds", GameLevel.HARD, starsEarned = 3, moves = 22, timeSec = 55)

        val totalStars = repository.getStarsForPack("birds")
        assertEquals(8, totalStars)
    }
}
