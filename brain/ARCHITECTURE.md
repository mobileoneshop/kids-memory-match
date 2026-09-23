# ARCHITECTURE — Kids Memory Match

## 1. Pattern
MVVM + unidirectional data flow. Pure-Kotlin game engine; Android framework only at
the edges (audio, storage, assets).

```
┌─────────────┐     events      ┌──────────────┐    calls    ┌─────────────┐
│  Compose UI │ ─────────────▶ │  ViewModels  │ ──────────▶ │ Repositories│
│  (screens)  │ ◀───────────── │  (UiState)   │ ◀────────── │  + Engine   │
└─────────────┘    StateFlow    └──────────────┘   results   └─────────────┘
                                            ▲                       │
                                            │   ┌───────────────────┘
                                            │   ▼
                                     ┌──────────────┐  ┌──────────────┐
                                     │  GameEngine  │  │ Audio (SFX/  │
                                     │ (pure kotlin)│  │ TTS / Music) │
                                     └──────────────┘  └──────────────┘
```

## 2. Navigation routes
- `splash` → `home`
- `home` → `level_select/{packId}` → `game/{packId}/{level}` → win dialog (overlay)
- `home` → `settings` (behind parent gate — gate UI lives on the settings entry point)

## 3. Key classes
| Class | Package | Role |
|---|---|---|
| `MainActivity` | root | single activity, hosts NavHost |
| `HomeViewModel` / `HomeScreen` | ui.home | pack grid, loads packs via repository |
| `LevelSelectViewModel` / `LevelSelectScreen` | ui.levels | 3 level cards + best stars display |
| `GameViewModel` | ui.game | holds `GameUiState`, forwards taps to engine |
| `GameEngine` | game | `newDeck(pack, level)`, `flip(cardId) → FlipResult`, `starsFor(moves, pairs)` |
| `PackRepository` | data | loads `packs.json` from assets, exposes packs/items |
| `ProgressRepository` | data | DataStore: stars/best per pack+level, settings |
| `SoundManager` | audio | SoundPool SFX |
| `MusicManager` | audio | looped bg music, audio-focus aware |
| `Speaker` | audio | TTS wrapper (`speakName(item)`, `celebrate()`) |
| `ParentGate` | ui.settings | math-challenge composable |

## 4. State model
`GameViewModel` exposes `StateFlow<GameUiState>`:
`cards: List<Card>`, `moves: Int`, `elapsedSec: Int`, `matchedPairs: Int`,
`phase: PLAYING | WON`, `lastFlip: FlipResult?` (consumed by UI for sound/TTS effects).

Side-effects (sounds, TTS, confetti) are triggered by the UI observing state changes,
never inside the engine. Engine is a pure state machine — fully unit-testable.

## 5. Rules
- ViewModels never touch `android.content.*` directly — go through repositories/managers.
- `game/` has zero `android.*` imports (enforced by code review; JVM unit tests).
- One ViewModel per screen; shared data via repositories, not shared ViewModels.
- All user-visible text via `stringResource` (strings.xml).
