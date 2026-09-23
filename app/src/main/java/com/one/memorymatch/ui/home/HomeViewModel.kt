package com.one.memorymatch.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.one.memorymatch.data.model.CardPack
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val packs: List<CardPack> = emptyList(),
    val packStars: Map<String, Int> = emptyMap(),
    val totalStars: Int = 0,
    val maxPossibleStars: Int = 0,
    val showParentGate: Boolean = false,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val packRepository: PackRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPacksAndProgress()
    }

    private fun loadPacksAndProgress() {
        viewModelScope.launch {
            val packs = packRepository.getAllPacks(includeVirtual = true)
            val packIds = packs.map { it.id }
            val maxStars = packs.size * 9 // 3 levels * 3 stars = 9 per pack

            progressRepository.getAllPacksStarsFlow(packIds).collect { starsMap ->
                val total = starsMap.values.sum()
                _uiState.update { current ->
                    current.copy(
                        packs = packs,
                        packStars = starsMap,
                        totalStars = total,
                        maxPossibleStars = maxStars,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun openParentGate() {
        _uiState.update { it.copy(showParentGate = true) }
    }

    fun closeParentGate() {
        _uiState.update { it.copy(showParentGate = false) }
    }

    class Factory(
        private val packRepository: PackRepository,
        private val progressRepository: ProgressRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(packRepository, progressRepository) as T
        }
    }
}
