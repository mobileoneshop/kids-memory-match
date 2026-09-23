package com.one.memorymatch

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.one.memorymatch.data.model.Card
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.GamePhase
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.data.repository.ProgressRepository
import com.one.memorymatch.ui.game.GameViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class GameViewModelTest {

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
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun init_generatesCorrectDeckSizeForEasyLevel() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        val state = viewModel.uiState.value

        assertEquals(12, state.cards.size)
        assertEquals(6, state.totalPairs)
        assertEquals(0, state.moves)
        assertEquals(0, state.matchedPairs)
        assertEquals(GamePhase.PLAYING, state.phase)
        assertFalse(state.isPaused)
        assertNotNull(state.pack)
        assertEquals("zoo", state.pack?.id)
    }

    @Test
    fun init_generatesCorrectDeckSizeForMediumAndHard() = runTest(testDispatcher) {
        val mediumVm = GameViewModel("farm", GameLevel.MEDIUM, packRepository, startTimerImmediately = false)
        assertEquals(16, mediumVm.uiState.value.cards.size)
        assertEquals(8, mediumVm.uiState.value.totalPairs)

        val hardVm = GameViewModel("sea", GameLevel.HARD, packRepository, startTimerImmediately = false)
        assertEquals(24, hardVm.uiState.value.cards.size)
        assertEquals(12, hardVm.uiState.value.totalPairs)
    }

    @Test
    fun flip_firstCardTurnsFaceUpWithoutIncreasingMoves() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        val firstCard = viewModel.uiState.value.cards.first()

        viewModel.onCardClicked(firstCard.uid)

        val updatedCard = viewModel.uiState.value.cards.first { it.uid == firstCard.uid }
        assertTrue(updatedCard.isFaceUp)
        assertFalse(updatedCard.isMatched)
        assertEquals(0, viewModel.uiState.value.moves)
    }

    @Test
    fun flip_matchingCardsStayFaceUpAndIncrementMoves() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        val cards = viewModel.uiState.value.cards

        // Find two cards with matching itemId
        val firstCard = cards[0]
        val matchingSecond = cards.drop(1).first { it.itemId == firstCard.itemId }

        viewModel.onCardClicked(firstCard.uid)
        viewModel.onCardClicked(matchingSecond.uid)

        val updatedCards = viewModel.uiState.value.cards
        val c1 = updatedCards.first { it.uid == firstCard.uid }
        val c2 = updatedCards.first { it.uid == matchingSecond.uid }

        assertTrue(c1.isFaceUp)
        assertTrue(c1.isMatched)
        assertTrue(c2.isFaceUp)
        assertTrue(c2.isMatched)
        assertEquals(1, viewModel.uiState.value.moves)
        assertEquals(1, viewModel.uiState.value.matchedPairs)
    }

    @Test
    fun flip_mismatchFlipsBackAfterDelay() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        val cards = viewModel.uiState.value.cards

        // Find two cards with different itemIds
        val firstCard = cards[0]
        val mismatchSecond = cards.drop(1).first { it.itemId != firstCard.itemId }

        viewModel.onCardClicked(firstCard.uid)
        viewModel.onCardClicked(mismatchSecond.uid)

        // Immediately after mismatch: both cards are face-up, moves = 1
        assertEquals(1, viewModel.uiState.value.moves)
        assertTrue(viewModel.uiState.value.cards.first { it.uid == firstCard.uid }.isFaceUp)
        assertTrue(viewModel.uiState.value.cards.first { it.uid == mismatchSecond.uid }.isFaceUp)

        // Advance past the 900ms mismatch delay
        advanceTimeBy(1000)

        // Both cards should now be flipped back face-down
        assertFalse(viewModel.uiState.value.cards.first { it.uid == firstCard.uid }.isFaceUp)
        assertFalse(viewModel.uiState.value.cards.first { it.uid == mismatchSecond.uid }.isFaceUp)
        assertFalse(viewModel.uiState.value.cards.first { it.uid == firstCard.uid }.isMatched)
    }

    @Test
    fun timer_incrementsElapsedSecWhenRunning() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        assertEquals(0, viewModel.uiState.value.elapsedSec)

        viewModel.startTimer()
        advanceTimeBy(3050)
        assertEquals(3, viewModel.uiState.value.elapsedSec)

        viewModel.stopTimer()
    }

    @Test
    fun restart_resetsGameCardsAndMoves() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        val firstCard = viewModel.uiState.value.cards.first()
        viewModel.onCardClicked(firstCard.uid)

        viewModel.restart()

        val state = viewModel.uiState.value
        assertEquals(0, state.moves)
        assertEquals(0, state.elapsedSec)
        assertEquals(0, state.matchedPairs)
        assertFalse(state.isPaused)
        assertTrue(state.cards.all { !it.isFaceUp && !it.isMatched })

        viewModel.stopTimer()
    }

    @Test
    fun togglePause_updatesPausedState() = runTest(testDispatcher) {
        val viewModel = GameViewModel("zoo", GameLevel.EASY, packRepository, startTimerImmediately = false)
        assertFalse(viewModel.uiState.value.isPaused)

        viewModel.togglePause()
        assertTrue(viewModel.uiState.value.isPaused)

        viewModel.togglePause()
        assertFalse(viewModel.uiState.value.isPaused)
    }

    @Test
    fun win_smallDeckCompletesToWin_triggersWonPhaseAndShowsWinDialog() = runTest(testDispatcher) {
        val customDeck = listOf(
            Card(uid = 0, itemId = "lion"),
            Card(uid = 1, itemId = "lion")
        )
        val viewModel = GameViewModel(
            packId = "zoo",
            level = GameLevel.EASY,
            packRepository = packRepository,
            progressRepository = progressRepository,
            customDeck = customDeck,
            startTimerImmediately = false
        )

        viewModel.onCardClicked(0)
        viewModel.onCardClicked(1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(GamePhase.WON, state.phase)
        assertTrue(state.showWinDialog)
        assertEquals(3, state.starsEarned)
        assertEquals(1, state.moves)
        assertEquals(1, state.matchedPairs)
        assertTrue(state.isNewBest)
    }

    @Test
    fun win_starsCalculatedCorrectlyPerFormula() = runTest(testDispatcher) {
        // Deck with 2 pairs (P = 2):
        // 3★ <= 3 moves; 2★ <= 4 moves; 1★ >= 5 moves
        val customDeck = listOf(
            Card(uid = 0, itemId = "lion"),
            Card(uid = 1, itemId = "lion"),
            Card(uid = 2, itemId = "tiger"),
            Card(uid = 3, itemId = "tiger")
        )
        val viewModel = GameViewModel(
            packId = "zoo",
            level = GameLevel.EASY,
            packRepository = packRepository,
            progressRepository = progressRepository,
            customDeck = customDeck,
            startTimerImmediately = false
        )

        // 3 mismatches: 3 moves
        repeat(3) {
            viewModel.onCardClicked(0)
            viewModel.onCardClicked(2)
            advanceTimeBy(1000)
        }

        // Match pair 1: card 0 & card 1 (move 4)
        viewModel.onCardClicked(0)
        viewModel.onCardClicked(1)

        // Match pair 2: card 2 & card 3 (move 5 -> total moves = 5, Win!)
        viewModel.onCardClicked(2)
        viewModel.onCardClicked(3)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(GamePhase.WON, state.phase)
        assertTrue(state.showWinDialog)
        assertEquals(5, state.moves)
        assertEquals(1, state.starsEarned)
    }

    @Test
    fun win_savesGameResultToProgressRepository_andUpdatesBestOnlyWhenImproved() = runTest(testDispatcher) {
        val deck1 = listOf(
            Card(uid = 0, itemId = "lion"),
            Card(uid = 1, itemId = "lion")
        )
        // First win: 1 move -> 3 stars
        val vm1 = GameViewModel(
            packId = "zoo",
            level = GameLevel.EASY,
            packRepository = packRepository,
            progressRepository = progressRepository,
            customDeck = deck1,
            startTimerImmediately = false
        )
        vm1.onCardClicked(0)
        vm1.onCardClicked(1)
        advanceUntilIdle()

        assertTrue(vm1.uiState.value.isNewBest)
        val p1 = progressRepository.getProgress("zoo", GameLevel.EASY)
        assertEquals(3, p1.stars)
        assertEquals(1, p1.bestMoves)

        // Second game: more moves -> isNewBest = false
        val deck2 = listOf(
            Card(uid = 0, itemId = "lion"),
            Card(uid = 1, itemId = "lion"),
            Card(uid = 2, itemId = "tiger"),
            Card(uid = 3, itemId = "tiger")
        )
        val vm2 = GameViewModel(
            packId = "zoo",
            level = GameLevel.EASY,
            packRepository = packRepository,
            progressRepository = progressRepository,
            customDeck = deck2,
            startTimerImmediately = false
        )
        repeat(3) {
            vm2.onCardClicked(0)
            vm2.onCardClicked(2)
            advanceTimeBy(1000)
        }
        vm2.onCardClicked(0)
        vm2.onCardClicked(1)
        vm2.onCardClicked(2)
        vm2.onCardClicked(3)
        advanceUntilIdle()

        assertFalse(vm2.uiState.value.isNewBest)
        val p2 = progressRepository.getProgress("zoo", GameLevel.EASY)
        // Best should still be 3 stars and 1 move!
        assertEquals(3, p2.stars)
        assertEquals(1, p2.bestMoves)
    }
}
