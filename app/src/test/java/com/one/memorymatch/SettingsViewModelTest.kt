package com.one.memorymatch

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.settings.SettingsViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testScope: CoroutineScope
    private lateinit var progressRepository: ProgressRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        testScope = CoroutineScope(testDispatcher + SupervisorJob())

        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tempFolder.newFile("test_settings_prefs_${System.nanoTime()}.preferences_pb") }
        )
        progressRepository = ProgressRepository(testDataStore)
        viewModel = SettingsViewModel(progressRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cancel()
    }

    @Test
    fun defaultSettings_soundAndMusicEnabled() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue("Sound should be enabled by default", state.soundEnabled)
        assertTrue("Music should be enabled by default", state.musicEnabled)
        assertEquals("en", state.language)
        assertFalse(state.showResetConfirmDialog)
        assertFalse(state.showResetParentGate)
        assertFalse(state.resetSuccess)
    }

    @Test
    fun toggleSound_updatesRepositoryAndState() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.toggleSound(false)
        advanceUntilIdle()

        assertFalse("Sound state should be disabled", viewModel.uiState.value.soundEnabled)
        assertFalse("Repository sound should be disabled", progressRepository.getSettings().soundOn)

        viewModel.toggleSound(true)
        advanceUntilIdle()

        assertTrue("Sound state should be re-enabled", viewModel.uiState.value.soundEnabled)
        assertTrue("Repository sound should be re-enabled", progressRepository.getSettings().soundOn)
    }

    @Test
    fun toggleMusic_updatesRepositoryAndState() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.toggleMusic(false)
        advanceUntilIdle()

        assertFalse("Music state should be disabled", viewModel.uiState.value.musicEnabled)
        assertFalse("Repository music should be disabled", progressRepository.getSettings().musicOn)

        viewModel.toggleMusic(true)
        advanceUntilIdle()

        assertTrue("Music state should be re-enabled", viewModel.uiState.value.musicEnabled)
        assertTrue("Repository music should be re-enabled", progressRepository.getSettings().musicOn)
    }

    @Test
    fun resetProgress_dialogAndGateFlow() = runTest(testDispatcher) {
        advanceUntilIdle()

        // 1. Initial state
        assertFalse(viewModel.uiState.value.showResetConfirmDialog)
        assertFalse(viewModel.uiState.value.showResetParentGate)

        // 2. Request reset
        viewModel.requestResetProgress()
        assertTrue(viewModel.uiState.value.showResetConfirmDialog)
        assertFalse(viewModel.uiState.value.showResetParentGate)

        // 3. Dismiss dialog
        viewModel.dismissResetConfirm()
        assertFalse(viewModel.uiState.value.showResetConfirmDialog)
        assertFalse(viewModel.uiState.value.showResetParentGate)

        // 4. Request reset again and confirm -> requires gate
        viewModel.requestResetProgress()
        viewModel.confirmResetRequiresGate()
        assertFalse(viewModel.uiState.value.showResetConfirmDialog)
        assertTrue(viewModel.uiState.value.showResetParentGate)

        // 5. Dismiss gate
        viewModel.dismissResetGate()
        assertFalse(viewModel.uiState.value.showResetParentGate)
    }

    @Test
    fun onResetGatePassed_resetsRepositoryProgressPreservingSettings() = runTest(testDispatcher) {
        advanceUntilIdle()

        // Seed some game progress
        progressRepository.saveGameResult("zoo", GameLevel.EASY, starsEarned = 3, moves = 8, timeSec = 20)
        progressRepository.setSoundEnabled(false)
        advanceUntilIdle()

        assertEquals(3, progressRepository.getProgress("zoo", GameLevel.EASY).stars)
        assertFalse(progressRepository.getSettings().soundOn)

        // Flow to reset
        viewModel.requestResetProgress()
        viewModel.confirmResetRequiresGate()
        viewModel.onResetGatePassed()
        advanceUntilIdle()

        // Gate closed and success flag set
        assertFalse(viewModel.uiState.value.showResetParentGate)
        assertTrue(viewModel.uiState.value.resetSuccess)

        // Progress cleared
        assertEquals(0, progressRepository.getProgress("zoo", GameLevel.EASY).stars)
        // Settings preserved
        assertFalse("Sound should remain disabled", progressRepository.getSettings().soundOn)

        // Clear success message
        viewModel.clearResetSuccess()
        assertFalse(viewModel.uiState.value.resetSuccess)
    }
}
