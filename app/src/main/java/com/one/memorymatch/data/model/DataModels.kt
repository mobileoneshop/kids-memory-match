package com.one.memorymatch.data.model

data class CardPack(
    val id: String,            // "zoo"
    val name: String,          // "Zoo Animals" (manifest default)
    val nameRes: Int = 0,      // R.string.pack_zoo ("Zoo Animals")
    val iconAsset: String,     // "cards/zoo/lion.webp" (pack tile art)
    val primaryColor: Long,    // 0xFF4CAF50
    val darkColor: Long,       // 0xFF2E7D32
    val itemIds: List<String>, // 12 ids
    val isVirtual: Boolean = false
)

data class CardItem(
    val id: String,            // "lion"
    val packId: String,        // "zoo"
    val name: String,          // "Lion"
    val nameRes: Int = 0,      // R.string.item_lion ("Lion")
    val imageAsset: String,    // "cards/zoo/lion.webp"
    val soundAsset: String?,   // "sounds/zoo/lion.mp3" (null → TTS fallback)
    val speakText: String      // "Lion!" (TTS text, English v1)
)

enum class GameLevel(val cols: Int, val rows: Int, val pairs: Int) {
    EASY(4, 3, 6),
    MEDIUM(4, 4, 8),
    HARD(6, 4, 12);
}

data class Card(
    val uid: Int,              // unique per deck instance
    val itemId: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

data class GameUiState(
    val cards: List<Card> = emptyList(),
    val moves: Int = 0,
    val elapsedSec: Int = 0,
    val matchedPairs: Int = 0,
    val totalPairs: Int = 0,
    val phase: GamePhase = GamePhase.PLAYING,
    val starsEarned: Int = 0
)

enum class GamePhase {
    PLAYING,
    WON
}

data class PackProgress(
    val packId: String,
    val level: GameLevel,
    val stars: Int,            // 0..3 best
    val bestMoves: Int,        // Int.MAX_VALUE = none yet
    val bestTimeSec: Int
)

data class AppSettings(
    val soundOn: Boolean = true,
    val musicOn: Boolean = true,
    val language: String = "en" // v1 fixed; selector disabled
)
