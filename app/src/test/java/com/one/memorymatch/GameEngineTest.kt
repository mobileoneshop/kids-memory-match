package com.one.memorymatch

import com.one.memorymatch.data.model.CardItem
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.GamePhase
import com.one.memorymatch.data.repository.PackRepository
import com.one.memorymatch.game.FlipResult
import com.one.memorymatch.game.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.random.Random

class GameEngineTest {

    private lateinit var repository: PackRepository
    private lateinit var zooItems: List<CardItem>
    private lateinit var allItems: List<CardItem>

    @Before
    fun setUp() {
        val openAsset: (String) -> InputStream = { path ->
            val file = File("src/main/assets", path)
            if (file.exists()) {
                FileInputStream(file)
            } else {
                File("../app/src/main/assets", path).inputStream()
            }
        }
        repository = PackRepository(openAsset)
        zooItems = repository.getItemsForPack("zoo")
        allItems = repository.allItems
    }

    @Test
    fun newDeck_zooEasy_twelveCardsAndExactlySixPairs() {
        val cards = GameEngine.newDeck(zooItems, GameLevel.EASY)
        assertEquals("EASY deck must contain 12 cards", 12, cards.size)

        // Verify each itemId appears exactly twice (6 pairs)
        val frequency = cards.groupingBy { it.itemId }.eachCount()
        assertEquals("EASY deck must have exactly 6 distinct items", 6, frequency.size)
        frequency.forEach { (itemId, count) ->
            assertEquals("Item $itemId must appear exactly twice", 2, count)
        }

        // Verify initial state
        cards.forEach { card ->
            assertFalse("Card must start face down", card.isFaceUp)
            assertFalse("Card must start unmatched", card.isMatched)
        }
    }

    @Test
    fun newDeck_zooHard_twentyFourCardsAndUsesAllTwelveItems() {
        val cards = GameEngine.newDeck(zooItems, GameLevel.HARD)
        assertEquals("HARD deck must contain 24 cards", 24, cards.size)

        val frequency = cards.groupingBy { it.itemId }.eachCount()
        assertEquals("HARD deck must use all 12 items", 12, frequency.size)
        frequency.forEach { (itemId, count) ->
            assertEquals("Item $itemId must appear exactly twice", 2, count)
        }
    }

    @Test
    fun deck_isShuffledDifferently() {
        var differences = 0
        for (seed in 1..100) {
            val deck1 = GameEngine.newDeck(zooItems, GameLevel.EASY, Random(seed))
            val deck2 = GameEngine.newDeck(zooItems, GameLevel.EASY, Random(seed + 9999))
            val order1 = deck1.map { it.itemId }
            val order2 = deck2.map { it.itemId }
            if (order1 != order2) {
                differences++
            }
        }
        // Statistically, virtually all pairs of different seeds must have different card orders
        assertTrue("Decks with different seeds must differ (>90% of the time)", differences > 90)
    }

    @Test
    fun flip_matchingPair_marksBothMatchedAndIncrementsPairs() {
        val cards = GameEngine.newDeck(zooItems, GameLevel.EASY)
        val engine = GameEngine(GameLevel.EASY, cards)

        // Find two cards with matching itemIds
        val firstItemId = engine.uiState.cards[0].itemId
        val matchingIndices = engine.uiState.cards.mapIndexedNotNull { index, card ->
            if (card.itemId == firstItemId) index else null
        }
        assertEquals(2, matchingIndices.size)

        val firstUid = engine.uiState.cards[matchingIndices[0]].uid
        val secondUid = engine.uiState.cards[matchingIndices[1]].uid

        val result1 = engine.flip(firstUid)
        assertTrue("First flip must return FirstCard", result1 is FlipResult.FirstCard)
        assertEquals(firstUid, (result1 as FlipResult.FirstCard).card.uid)
        assertTrue(engine.uiState.cards.first { it.uid == firstUid }.isFaceUp)
        assertEquals(0, engine.moves)
        assertEquals(0, engine.matchedPairs)
        assertFalse(engine.isLocked)

        val result2 = engine.flip(secondUid)
        assertTrue("Matching second flip must return Match", result2 is FlipResult.Match)
        assertEquals(1, engine.moves)
        assertEquals(1, engine.matchedPairs)
        assertFalse(engine.isLocked)

        val updatedFirst = engine.uiState.cards.first { it.uid == firstUid }
        val updatedSecond = engine.uiState.cards.first { it.uid == secondUid }
        assertTrue(updatedFirst.isMatched)
        assertTrue(updatedFirst.isFaceUp)
        assertTrue(updatedSecond.isMatched)
        assertTrue(updatedSecond.isFaceUp)
    }

    @Test
    fun flip_mismatch_locksInputUntilResolved() {
        val cards = GameEngine.newDeck(zooItems, GameLevel.EASY)
        val engine = GameEngine(GameLevel.EASY, cards)

        // Pick two cards with different itemIds
        val firstCard = engine.uiState.cards[0]
        val secondCard = engine.uiState.cards.first { it.itemId != firstCard.itemId }

        val res1 = engine.flip(firstCard.uid)
        assertTrue(res1 is FlipResult.FirstCard)

        val res2 = engine.flip(secondCard.uid)
        assertTrue("Mismatch flip must return Mismatch", res2 is FlipResult.Mismatch)
        assertEquals(1, engine.moves)
        assertEquals(0, engine.matchedPairs)
        assertTrue("Engine must be locked on mismatch", engine.isLocked)

        // Any further tap while locked must be NoOp
        val thirdCard = engine.uiState.cards.first { it.uid != firstCard.uid && it.uid != secondCard.uid }
        val lockedRes = engine.flip(thirdCard.uid)
        assertEquals("Flip while locked must be NoOp", FlipResult.NoOp, lockedRes)

        // Resolve mismatch
        engine.resolveMismatch()
        assertFalse("Engine must unlock after resolveMismatch", engine.isLocked)
        assertFalse(engine.uiState.cards.first { it.uid == firstCard.uid }.isFaceUp)
        assertFalse(engine.uiState.cards.first { it.uid == secondCard.uid }.isFaceUp)
    }

    @Test
    fun flip_sameCardTwiceOrMatchedCard_isNoOp() {
        val cards = GameEngine.newDeck(zooItems, GameLevel.EASY)
        val engine = GameEngine(GameLevel.EASY, cards)

        val cardUid = engine.uiState.cards[0].uid
        engine.flip(cardUid)

        // Flipping same card again
        val duplicateFlip = engine.flip(cardUid)
        assertEquals(FlipResult.NoOp, duplicateFlip)
        assertEquals(0, engine.moves)

        // Match the pair
        val matchingUid = engine.uiState.cards.first { it.itemId == engine.uiState.cards[0].itemId && it.uid != cardUid }.uid
        engine.flip(matchingUid)
        assertEquals(1, engine.matchedPairs)

        // Try flipping matched card
        val flipMatched = engine.flip(cardUid)
        assertEquals(FlipResult.NoOp, flipMatched)
    }

    @Test
    fun starsFor_boundaryChecks() {
        // Pairs P = 6 (Easy)
        // 3★: M <= ceil(6 * 1.5) = 9
        // 2★: M <= ceil(6 * 2.0) = 12
        // 1★: M >= 13
        assertEquals(3, GameEngine.starsFor(moves = 6, pairs = 6))
        assertEquals(3, GameEngine.starsFor(moves = 9, pairs = 6))
        assertEquals(2, GameEngine.starsFor(moves = 10, pairs = 6))
        assertEquals(2, GameEngine.starsFor(moves = 12, pairs = 6))
        assertEquals(1, GameEngine.starsFor(moves = 13, pairs = 6))
        assertEquals(1, GameEngine.starsFor(moves = 30, pairs = 6))

        // Pairs P = 8 (Medium)
        // 3★: M <= ceil(8 * 1.5) = 12
        // 2★: M <= ceil(8 * 2.0) = 16
        // 1★: M >= 17
        assertEquals(3, GameEngine.starsFor(moves = 12, pairs = 8))
        assertEquals(2, GameEngine.starsFor(moves = 13, pairs = 8))
        assertEquals(2, GameEngine.starsFor(moves = 16, pairs = 8))
        assertEquals(1, GameEngine.starsFor(moves = 17, pairs = 8))

        // Pairs P = 12 (Hard)
        // 3★: M <= ceil(12 * 1.5) = 18
        // 2★: M <= ceil(12 * 2.0) = 24
        // 1★: M >= 25
        assertEquals(3, GameEngine.starsFor(moves = 18, pairs = 12))
        assertEquals(2, GameEngine.starsFor(moves = 19, pairs = 12))
        assertEquals(2, GameEngine.starsFor(moves = 24, pairs = 12))
        assertEquals(1, GameEngine.starsFor(moves = 25, pairs = 12))
    }

    @Test
    fun allMixDeck_noDuplicateItemIdsAndDrawsFromAtLeastTwoPacks() {
        for (i in 1..20) {
            val cards = GameEngine.newDeck(allItems, GameLevel.EASY)
            val distinctItemIds = cards.map { it.itemId }.distinct()
            assertEquals("Easy level must have 6 distinct item IDs", 6, distinctItemIds.size)

            val sampledPackIds = distinctItemIds.mapNotNull { itemId ->
                allItems.firstOrNull { it.id == itemId }?.packId
            }.distinct()

            assertTrue(
                "All-Mix deck must draw from at least 2 distinct packs, drew from: $sampledPackIds",
                sampledPackIds.size >= 2
            )
        }
    }

    @Test
    fun fullGame_completesToWinWithStars() {
        val cards = GameEngine.newDeck(zooItems, GameLevel.EASY)
        val engine = GameEngine(GameLevel.EASY, cards)

        val pairsByItem = cards.groupBy { it.itemId }
        assertEquals(6, pairsByItem.size)

        var lastResult: FlipResult? = null
        pairsByItem.values.forEach { pairCards ->
            engine.flip(pairCards[0].uid)
            lastResult = engine.flip(pairCards[1].uid)
        }

        assertTrue("Final flip must return Win", lastResult is FlipResult.Win)
        val winResult = lastResult as FlipResult.Win
        assertEquals(3, winResult.stars)
        assertEquals(6, engine.moves)
        assertEquals(6, engine.matchedPairs)
        assertEquals(GamePhase.WON, engine.phase)
        assertEquals(3, engine.uiState.starsEarned)
    }
}
