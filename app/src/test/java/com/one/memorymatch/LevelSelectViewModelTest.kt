package com.one.memorymatch

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.level.LevelSelectViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
class LevelSelectViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope
    private lateinit var packRepository: PackRepository
    private lateinit var progressRepository: ProgressRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(testDispatcher + SupervisorJob())

        val openAsset: (String) -> InputStream = { path ->
            val file = File("src/main/assets", path)
            if (file.exists()) {
                FileInputStream(file)
            } else {
                File("../app/src/main/assets", path).inputStream()
            }
        }
        packRepository = PackRepository(openAsset)

        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFolder.newFile("test_prefs_${System.nanoTime()}.preferences_pb") }
        )
        progressRepository = ProgressRepository(testDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun initialState_loadsPackDetailsAndDefaultProgress() = runTest(testDispatcher) {
        val viewModel = LevelSelectViewModel("zoo", packRepository, progressRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Should no longer be loading", state.isLoading)
        assertNotNull("Pack must not be null", state.pack)
        assertEquals("zoo", state.pack?.id)
        assertEquals("Zoo Animals", state.pack?.name)
        assertEquals(0, state.totalPackStars)

        // Verify all 3 levels present
        assertEquals(3, state.progress.size)
        val easy = state.progress[GameLevel.EASY]
        assertNotNull(easy)
        assertEquals(0, easy?.stars)
        assertEquals(Int.MAX_VALUE, easy?.bestMoves)

        val medium = state.progress[GameLevel.MEDIUM]
        assertNotNull(medium)
        assertEquals(0, medium?.stars)

        val hard = state.progress[GameLevel.HARD]
        assertNotNull(hard)
        assertEquals(0, hard?.stars)
    }

    @Test
    fun savedProgress_updatesLevelsAndTotalStars() = runTest(testDispatcher) {
        val viewModel = LevelSelectViewModel("zoo", packRepository, progressRepository)
        advanceUntilIdle()

        progressRepository.saveGameResult(
            packId = "zoo",
            level = GameLevel.EASY,
            starsEarned = 3,
            moves = 7,
            timeSec = 18
        )
        progressRepository.saveGameResult(
            packId = "zoo",
            level = GameLevel.HARD,
            starsEarned = 2,
            moves = 16,
            timeSec = 45
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.totalPackStars)

        val easy = state.progress[GameLevel.EASY]
        assertEquals(3, easy?.stars)
        assertEquals(7, easy?.bestMoves)
        assertEquals(18, easy?.bestTimeSec)

        val medium = state.progress[GameLevel.MEDIUM]
        assertEquals(0, medium?.stars)
        assertEquals(Int.MAX_VALUE, medium?.bestMoves)

        val hard = state.progress[GameLevel.HARD]
        assertEquals(2, hard?.stars)
        assertEquals(16, hard?.bestMoves)
        assertEquals(45, hard?.bestTimeSec)
    }

    @Test
    fun allMixPack_loadsCorrectly() = runTest(testDispatcher) {
        val viewModel = LevelSelectViewModel("allmix", packRepository, progressRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.pack)
        assertEquals("allmix", state.pack?.id)
        assertTrue("All Mix must be virtual", state.pack?.isVirtual == true)
        assertEquals(3, state.progress.size)
    }
}
