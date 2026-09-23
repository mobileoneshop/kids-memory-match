package com.one.memorymatch

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.home.HomeViewModel
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope
    private lateinit var packRepository: PackRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var viewModel: HomeViewModel

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

        viewModel = HomeViewModel(packRepository, progressRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun initialState_loadsPacksFromManifestIncludingAllMix() = runTest(testDispatcher) {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("Should no longer be loading", state.isLoading)
        assertEquals("Expected 9 packs (8 manifest + All Mix)", 9, state.packs.size)

        val expectedIds = listOf(
            "zoo", "farm", "sea", "birds", "fruits", "vegetables", "vehicles", "shapes_colors", "allmix"
        )
        assertEquals(expectedIds, state.packs.map { it.id })
        assertEquals(81, state.maxPossibleStars) // 9 packs * 9 stars
        assertEquals(0, state.totalStars)
        assertFalse(state.showParentGate)
    }

    @Test
    fun parentGateToggles_updateUiState() = runTest(testDispatcher) {
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showParentGate)

        viewModel.openParentGate()
        assertTrue(viewModel.uiState.value.showParentGate)

        viewModel.closeParentGate()
        assertFalse(viewModel.uiState.value.showParentGate)
    }

    @Test
    fun progressUpdates_reflectInHomeUiState() = runTest(testDispatcher) {
        advanceUntilIdle()

        progressRepository.saveGameResult(
            packId = "zoo",
            level = GameLevel.EASY,
            starsEarned = 3,
            moves = 6,
            timeSec = 20
        )
        progressRepository.saveGameResult(
            packId = "zoo",
            level = GameLevel.MEDIUM,
            starsEarned = 2,
            moves = 10,
            timeSec = 35
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.packStars["zoo"])
        assertEquals(5, state.totalStars)
    }
}
