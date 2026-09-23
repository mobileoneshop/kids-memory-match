package com.one.memorymatch.game

import com.one.memorymatch.data.model.Card
import com.one.memorymatch.data.model.CardItem
import com.one.memorymatch.data.model.GameLevel
import com.one.memorymatch.data.model.GamePhase
import com.one.memorymatch.data.model.GameUiState
import kotlin.math.ceil
import kotlin.random.Random

sealed class FlipResult {
    data class FirstCard(val card: Card) : FlipResult()
    data class Match(val firstCard: Card, val secondCard: Card) : FlipResult()
    data class Mismatch(val firstCard: Card, val secondCard: Card) : FlipResult()
    data class Win(val firstCard: Card, val secondCard: Card, val stars: Int) : FlipResult()
    data object NoOp : FlipResult()
}

class GameEngine(
    val level: GameLevel,
    initialCards: List<Card>
) {
    private var cards: MutableList<Card> = initialCards.map { it.copy() }.toMutableList()
    private var firstFlippedCard: Card? = null
    private var pendingMismatch: Pair<Card, Card>? = null

    var isLocked: Boolean = false
        private set

    var moves: Int = 0
        private set

    var matchedPairs: Int = 0
        private set

    var phase: GamePhase = GamePhase.PLAYING
        private set

    val totalPairs: Int = if (initialCards.isNotEmpty()) initialCards.size / 2 else level.pairs

    val uiState: GameUiState
        get() = GameUiState(
            cards = cards.toList(),
            moves = moves,
            elapsedSec = 0,
            matchedPairs = matchedPairs,
            totalPairs = totalPairs,
            phase = phase,
            starsEarned = if (phase == GamePhase.WON) starsFor(moves, totalPairs) else 0
        )

    fun flip(cardUid: Int): FlipResult {
        if (isLocked || phase == GamePhase.WON) {
            return FlipResult.NoOp
        }

        val cardIndex = cards.indexOfFirst { it.uid == cardUid }
        if (cardIndex == -1) return FlipResult.NoOp

        val targetCard = cards[cardIndex]
        if (targetCard.isFaceUp || targetCard.isMatched) {
            return FlipResult.NoOp
        }

        val currentFirst = firstFlippedCard
        if (currentFirst == null) {
            // First card of pair
            val updated = targetCard.copy(isFaceUp = true)
            cards[cardIndex] = updated
            firstFlippedCard = updated
            return FlipResult.FirstCard(updated)
        } else {
            // Second card of pair
            val updatedSecond = targetCard.copy(isFaceUp = true)
            cards[cardIndex] = updatedSecond
            moves++

            return if (currentFirst.itemId == updatedSecond.itemId) {
                // Match
                val matchedFirst = currentFirst.copy(isMatched = true)
                val matchedSecond = updatedSecond.copy(isMatched = true)

                val firstIndex = cards.indexOfFirst { it.uid == currentFirst.uid }
                if (firstIndex != -1) cards[firstIndex] = matchedFirst
                cards[cardIndex] = matchedSecond

                matchedPairs++
                firstFlippedCard = null

                if (matchedPairs >= totalPairs) {
                    phase = GamePhase.WON
                    val stars = starsFor(moves, totalPairs)
                    FlipResult.Win(matchedFirst, matchedSecond, stars)
                } else {
                    FlipResult.Match(matchedFirst, matchedSecond)
                }
            } else {
                // Mismatch
                isLocked = true
                pendingMismatch = Pair(currentFirst, updatedSecond)
                FlipResult.Mismatch(currentFirst, updatedSecond)
            }
        }
    }

    fun resolveMismatch() {
        val mismatch = pendingMismatch ?: return
        val firstIndex = cards.indexOfFirst { it.uid == mismatch.first.uid }
        val secondIndex = cards.indexOfFirst { it.uid == mismatch.second.uid }

        if (firstIndex != -1) {
            cards[firstIndex] = cards[firstIndex].copy(isFaceUp = false)
        }
        if (secondIndex != -1) {
            cards[secondIndex] = cards[secondIndex].copy(isFaceUp = false)
        }

        pendingMismatch = null
        firstFlippedCard = null
        isLocked = false
    }

    companion object {
        fun starsFor(moves: Int, pairs: Int): Int {
            if (pairs <= 0) return 1
            val threeStarMoves = ceil(pairs * 1.5).toInt()
            val twoStarMoves = ceil(pairs * 2.0).toInt()
            return when {
                moves <= threeStarMoves -> 3
                moves <= twoStarMoves -> 2
                else -> 1
            }
        }

        fun newDeck(
            items: List<CardItem>,
            level: GameLevel,
            random: Random = Random.Default
        ): List<Card> {
            require(items.isNotEmpty()) { "Cannot create deck from empty items" }
            val pairsNeeded = level.pairs

            // Ensure distinct items by itemId
            val distinctItems = items.distinctBy { it.id }
            require(distinctItems.size >= pairsNeeded) {
                "Not enough distinct items (${distinctItems.size}) for level requiring $pairsNeeded pairs"
            }

            // Sample distinct items
            val sampledItems = sampleItems(distinctItems, pairsNeeded, random)

            // Create 2 cards per sampled item
            val cards = mutableListOf<Card>()
            var uidCounter = 0
            for (item in sampledItems) {
                cards.add(Card(uid = uidCounter++, itemId = item.id, isFaceUp = false, isMatched = false))
                cards.add(Card(uid = uidCounter++, itemId = item.id, isFaceUp = false, isMatched = false))
            }

            return cards.shuffled(random)
        }

        private fun sampleItems(
            items: List<CardItem>,
            count: Int,
            random: Random
        ): List<CardItem> {
            val distinctPacks = items.map { it.packId }.distinct()
            if (distinctPacks.size <= 1) {
                return items.shuffled(random).take(count)
            }

            // For All-Mix (items from multiple packs): guarantee drawing from >= 2 packs
            var attempts = 0
            while (attempts < 100) {
                val sampled = items.shuffled(random).take(count)
                val packsDrawn = sampled.map { it.packId }.distinct()
                if (packsDrawn.size >= 2) {
                    return sampled
                }
                attempts++
            }
            return items.shuffled(random).take(count)
        }
    }
}
