# DATA MODEL — Kids Memory Match

## 1. Domain classes (Kotlin)

```kotlin
data class CardPack(
    val id: String,            // "zoo"
    val nameRes: Int,          // R.string.pack_zoo ("Zoo Animals")
    val iconAsset: String,     // "cards/zoo/lion.webp" (pack tile art)
    val primaryColor: Long,    // 0xFF4CAF50
    val darkColor: Long,       // 0xFF2E7D32
    val itemIds: List<String>, // 12 ids
    val isVirtual: Boolean = false
)

data class CardItem(
    val id: String,            // "lion"
    val packId: String,        // "zoo"
    val nameRes: Int,          // R.string.item_lion ("Lion")
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
enum class GamePhase { PLAYING, WON }

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
```

## 2. All Mix (virtual pack)
`packId = "allmix"`, `isVirtual = true`. Deck sampling: take the union of all v1 packs'
items, shuffle, take `pairs` items. Never reuse the same item twice in one deck.

## 3. Persistence (DataStore Preferences)
Keys:
- `progress_<packId>_<level>` → `"stars,bestMoves,bestTimeSec"` (csv)
- `settings_sound`, `settings_music` → boolean
- `settings_language` → "en"
- `onboarding_done` → boolean (reserved)

No Room — data is tiny and flat. If progress schema ever grows, migrate to Proto
DataStore (decision logged in DECISIONS.md at that time).

## 4. Asset manifest — `assets/packs.json`
```json
{
  "version": 1,
  "packs": [
    {
      "id": "zoo",
      "name": "Zoo Animals",
      "icon": "cards/zoo/lion.webp",
      "primaryColor": "#4CAF50",
      "darkColor": "#2E7D32",
      "items": [
        {"id": "lion", "name": "Lion", "image": "cards/zoo/lion.webp",
         "sound": "sounds/zoo/lion.mp3", "speak": "Lion!"}
      ]
    }
  ]
}
```
Loader parses this at startup into `CardPack`/`CardItem`. `name`/`speak` are English
in v1; when new languages arrive, manifest gains per-language maps (no code change).

## 5. Star formula (also in PRD §6)
```
P = pairs, M = moves
3★ if M ≤ ceil(P*1.5); 2★ if M ≤ ceil(P*2.0); else 1★
```
