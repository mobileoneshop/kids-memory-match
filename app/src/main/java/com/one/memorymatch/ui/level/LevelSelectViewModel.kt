package com.one.memorymatch.ui.level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.one.memorymatch.data.model.CardPack
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.PackProgress
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LevelSelectUiState(
    val pack: CardPack? = null,
    val progress: Map<GameLevel, PackProgress> = emptyMap(),
    val totalPackStars: Int = 0,
    val isLoading: Boolean = true
)

class LevelSelectViewModel(
    private val packId: String,
    private val packRepository: PackRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectUiState())
    val uiState: StateFlow<LevelSelectUiState> = _uiState.asStateFlow()

    init {
        loadPackAndProgress()
    }

    private fun loadPackAndProgress() {
        viewModelScope.launch {
            val pack = packRepository.getPack(packId)
            _uiState.update { it.copy(pack = pack) }

            combine(
                progressRepository.getProgressFlow(packId, GameLevel.EASY),
                progressRepository.getProgressFlow(packId, GameLevel.MEDIUM),
                progressRepository.getProgressFlow(packId, GameLevel.HARD)
            ) { easy, medium, hard ->
                val progressMap = mapOf(
                    GameLevel.EASY to easy,
                    GameLevel.MEDIUM to medium,
                    GameLevel.HARD to hard
                )
                val totalStars = easy.stars + medium.stars + hard.stars
                Pair(progressMap, totalStars)
            }.collect { (progressMap, totalStars) ->
                _uiState.update { current ->
                    current.copy(
                        progress = progressMap,
                        totalPackStars = totalStars,
                        isLoading = false
                    )
                }
            }
        }
    }

    class Factory(
        private val packId: String,
        private val packRepository: PackRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LevelSelectViewModel(packId, packRepository, progressRepository) as T
        }
    }
}
