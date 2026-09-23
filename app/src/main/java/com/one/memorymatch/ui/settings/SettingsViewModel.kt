package com.one.memorymatch.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.one.memorymatch.audio.MusicManager
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val language: String = "en",
    val showResetConfirmDialog: Boolean = false,
    val showResetParentGate: Boolean = false,
    val resetSuccess: Boolean = false
)

class SettingsViewModel(
    private val progressRepository: ProgressRepository,
    private val soundManager: SoundManager? = null,
    private val musicManager: MusicManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepository.settingsFlow.collect { settings ->
                _uiState.update { current ->
                    current.copy(
                        soundEnabled = settings.soundOn,
                        musicEnabled = settings.musicOn,
                        language = settings.language
                    )
                }
                soundManager?.soundEnabled = settings.soundOn
                musicManager?.musicEnabled = settings.musicOn
            }
        }
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            soundManager?.soundEnabled = enabled
            progressRepository.setSoundEnabled(enabled)
        }
    }

    fun toggleMusic(enabled: Boolean) {
        viewModelScope.launch {
            musicManager?.setEnabled(enabled)
            progressRepository.setMusicEnabled(enabled)
        }
    }

    fun requestResetProgress() {
        _uiState.update { it.copy(showResetConfirmDialog = true, resetSuccess = false) }
    }

    fun dismissResetConfirm() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    fun confirmResetRequiresGate() {
        _uiState.update {
            it.copy(
                showResetConfirmDialog = false,
                showResetParentGate = true
            )
        }
    }

    fun dismissResetGate() {
        _uiState.update { it.copy(showResetParentGate = false) }
    }

    fun onResetGatePassed() {
        viewModelScope.launch {
            progressRepository.resetAllProgress()
            _uiState.update {
                it.copy(
                    showResetParentGate = false,
                    resetSuccess = true
                )
            }
        }
    }

    fun clearResetSuccess() {
        _uiState.update { it.copy(resetSuccess = false) }
    }

    class Factory(
        private val progressRepository: ProgressRepository,
        private val soundManager: SoundManager? = null,
        private val musicManager: MusicManager? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(progressRepository, soundManager, musicManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
