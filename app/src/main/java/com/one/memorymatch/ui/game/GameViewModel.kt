package com.one.memorymatch.ui.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.one.memorymatch.audio.SoundManager
import com.one.memorymatch.audio.Speaker
import com.one.memorymatch.data.model.Card
import com.one.memorymatch.data.model.CardItem
import com.one.memorymatch.data.model.CardPack
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.GamePhase
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.game.FlipResult
import com.one.memorymatch.game.GameEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class GameScreenState(
    val pack: CardPack? = null,
    val level: GameLevel = GameLevel.EASY,
    val cards: List<Card> = emptyList(),
    val itemsMap: Map<String, CardItem> = emptyMap(),
    val moves: Int = 0,
    val elapsedSec: Int = 0,
    val matchedPairs: Int = 0,
    val totalPairs: Int = 0,
    val phase: GamePhase = GamePhase.PLAYING,
    val isPaused: Boolean = false,
    val starsEarned: Int = 0,
    val isNewBest: Boolean = false,
    val showWinDialog: Boolean = false
)

class GameViewModel(
    val packId: String,
    val level: GameLevel,
    private val packRepository: PackRepository,
    private val progressRepository: ProgressRepository? = null,
    private val soundManager: SoundManager? = null,
    private val speaker: Speaker? = null,
    private val customDeck: List<Card>? = null,
    startTimerImmediately: Boolean = true
) : ViewModel() {

    private val pack: CardPack? = packRepository.getPack(packId)
    private val items: List<CardItem> = packRepository.getItemsForPack(packId)
    private val itemsMap: Map<String, CardItem> = items.associateBy { it.id }

    private var engine: GameEngine = createEngine()
    private var timerJob: Job? = null
    private var mismatchJob: Job? = null

    private val _uiState = MutableStateFlow(
        GameScreenState(
            pack = pack,
            level = level,
            cards = engine.uiState.cards,
            itemsMap = itemsMap,
            moves = engine.moves,
            elapsedSec = 0,
            matchedPairs = engine.matchedPairs,
            totalPairs = engine.totalPairs,
            phase = engine.phase,
            isPaused = false,
            starsEarned = 0,
            isNewBest = false,
            showWinDialog = false
        )
    )
    val uiState: StateFlow<GameScreenState> = _uiState.asStateFlow()

    init {
        if (startTimerImmediately) {
            startTimer()
        }
    }

    private fun createEngine(): GameEngine {
        val deck = if (customDeck != null) {
            customDeck
        } else if (items.isNotEmpty()) {
            GameEngine.newDeck(items, level)
        } else {
            emptyList()
        }
        return GameEngine(level, deck)
    }

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (!_uiState.value.isPaused && _uiState.value.phase == GamePhase.PLAYING) {
                    _uiState.update { current ->
                        current.copy(elapsedSec = current.elapsedSec + 1)
                    }
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun onCardClicked(cardUid: Int) {
        if (_uiState.value.isPaused || engine.isLocked || engine.phase == GamePhase.WON) {
            return
        }

        soundManager?.playFlip()

        when (val result = engine.flip(cardUid)) {
            is FlipResult.FirstCard -> {
                updateFromEngine()
            }
            is FlipResult.Match -> {
                updateFromEngine()
                soundManager?.playMatch()
                val item = itemsMap[result.firstCard.itemId]
                if (item != null) {
                    speaker?.speakName(item.speakText)
                }
            }
            is FlipResult.Mismatch -> {
                updateFromEngine()
                soundManager?.playMismatch()
                // Mismatch locks engine input and automatically flips back after 900ms per UI_SPEC §2
                mismatchJob?.cancel()
                mismatchJob = viewModelScope.launch {
                    delay(900)
                    engine.resolveMismatch()
                    updateFromEngine()
                }
            }
            is FlipResult.Win -> {
                updateFromEngine()
                stopTimer()
                soundManager?.playMatch()
                val item = itemsMap[result.firstCard.itemId]
                if (item != null) {
                    speaker?.speakName(item.speakText)
                }

                soundManager?.playWin()

                viewModelScope.launch {
                    val improved = progressRepository?.saveGameResult(
                        packId = packId,
                        level = level,
                        starsEarned = result.stars,
                        moves = engine.moves,
                        timeSec = _uiState.value.elapsedSec
                    ) ?: false

                    _uiState.update { current ->
                        current.copy(
                            starsEarned = result.stars,
                            isNewBest = improved,
                            showWinDialog = true
                        )
                    }
                }
            }
            is FlipResult.NoOp -> {
                // Ignore invalid or already flipped cards
            }
        }
    }

    fun restart() {
        mismatchJob?.cancel()
        engine = createEngine()
        _uiState.update { current ->
            current.copy(
                cards = engine.uiState.cards,
                moves = 0,
                elapsedSec = 0,
                matchedPairs = 0,
                phase = GamePhase.PLAYING,
                isPaused = false,
                starsEarned = 0,
                isNewBest = false,
                showWinDialog = false
            )
        }
        startTimer()
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun setPaused(paused: Boolean) {
        _uiState.update { it.copy(isPaused = paused) }
    }

    private fun updateFromEngine() {
        _uiState.update { current ->
            current.copy(
                cards = engine.uiState.cards,
                moves = engine.moves,
                matchedPairs = engine.matchedPairs,
                phase = engine.phase
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        mismatchJob?.cancel()
    }

    class Factory(
        private val context: Context,
        private val packId: String,
        private val levelStr: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val gameLevel = try {
                GameLevel.valueOf(levelStr.uppercase())
            } catch (_: Exception) {
                GameLevel.EASY
            }
            val appContext = context.applicationContext
            val packRepository = PackRepository(appContext)
            val progressRepository = ProgressRepository(appContext)
            val soundManager = SoundManager(appContext)
            val speaker = Speaker(appContext)
            return GameViewModel(
                packId = packId,
                level = gameLevel,
                packRepository = packRepository,
                progressRepository = progressRepository,
                soundManager = soundManager,
                speaker = speaker
            ) as T
        }
    }
}
