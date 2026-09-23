# TESTING — Kids Memory Match

## 1. Unit tests (JVM, `game/` + ViewModel logic) — required
- `GameEngineTest`
  - `newDeck(zoo, EASY)` → 12 cards, exactly 6 pairs (each itemId appears twice).
  - `newDeck(zoo, HARD)` → 24 cards, 12 pairs, uses all 12 items.
  - Deck is shuffled (two decks differ — statistical, run 100x with fixed seeds).
  - `flip` matching pair → both `isMatched`, `matchedPairs` increments, returns MATCH.
  - `flip` mismatch → returns MISMATCH; engine locks input until `resolveMismatch()`.
  - Flipping same card twice / matched card → NO-OP.
  - `starsFor(moves, pairs)`: boundary checks (e.g. P=6: 9 moves → 3★, 10 → 2★, 13 → 1★).
  - All-mix deck: no duplicate itemIds, draws from ≥ 2 packs.
- `PackRepositoryTest` (fake AssetManager): manifest parses; missing image → item still
  loads with `imageAsset` present but loader flags missing at runtime (test the flag).
- `ProgressRepositoryTest`: save/load stars, best-moves only improves, corrupt data →
  defaults.

## 2. Compose UI tests (instrumented)
- Tap two matching cards → both stay face-up, moves = 1.
- Tap two non-matching → flip back after delay, moves = 1.
- Complete an EASY game (test hook: small debug deck) → Win dialog shows stars.
- Parent gate: wrong answer → stays; right answer → Settings opens.
- Settings toggles persist across recreation.

## 3. Manual QA matrix (every release)
| Area | Check |
|---|---|
| All 8 packs × 3 levels | complete one game each; art loads, TTS speaks, sounds play |
| All Mix × 3 levels | deck variety sane |
| Rotation | mid-game rotate on phone + tablet; state intact |
| Audio | silent mode, call interruption, toggles off |
| Offline | airplane mode — full playthrough |
| Kid test | a real 4–6 year old plays 10 min; note confusion points |
| Devices | small phone (5"), tablet 10", Fire HD 10 (Amazon build) |
| Performance | flip animation 60fps, cold start < 2s |

## 4. Pre-release gates
- 0 crashes in QA matrix, unit + UI tests green, `lintDebug` clean,
  AAB < 60MB, no INTERNET permission in manifest dump.
