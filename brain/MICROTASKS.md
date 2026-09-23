# MICROTASKS — Kids Memory Match

Work in order. Each task: implement → compile → tests pass → acceptance criteria met →
CHANGELOG.md entry → commit `M<n>: <desc>`.

## M1 — Project scaffold
Create Android project (Kotlin DSL, version catalog): applicationId
`com.one.memorymatch`, minSdk 21, targetSdk/compileSdk 36, Compose BOM, single
`MainActivity`. Empty NavHost with `splash` route.
_Accept: app installs and launches to blank splash on API 21 emulator._

## M2 — Design system
`ui/theme/`: Color.kt (kid palette + 8 pack colors), Type.kt (Baloo 2 in res/font),
Shape.kt (24dp cards, pill buttons), `BigButton`, `StarRow` components.
_Accept: a preview screen renders all components; no hardcoded colors in screens._

## M3 — Asset manifest + data model
`assets/packs.json` (v1: 8 packs × 12 items — placeholder entries with real ids/names),
data classes from DATA_MODEL.md, `PackRepository` loading via AssetManager.
_Accept: unit test parses manifest → 8 packs, 96 items, all fields non-blank._

## M4 — Game engine (pure Kotlin)
`game/GameEngine.kt`: `newDeck`, `flip`, mismatch lock, `starsFor`, All-Mix sampling.
Zero `android.*` imports.
_Accept: GameEngineTest (all cases in TESTING.md §1) green._

## M5 — Progress + settings storage
`ProgressRepository` (DataStore): stars/best per pack+level, sound/music toggles,
language="en". Corruption → defaults.
_Accept: unit tests incl. best-only-improves and corrupt-data recovery._

## M6 — Audio managers
`SoundManager` (SoundPool, preload ui/*.mp3), `MusicManager` (looped mp3, audio-focus),
`Speaker` (TTS wrapper: `speakName`, init-fail → silent no-op).
_Accept: manual test on device — each sound plays; airplane+no-TTS-engine → no crash._

## M7 — Splash + Home screens
Splash (logo bounce → preload → Home). Home: pack grid (8 + All Mix tile), stars summary,
gear → ParentGate.
_Accept: UI test — packs render from manifest; gear without passing gate stays on Home._

## M8 — Level Select screen
Pack header, 3 level cards with best stars/moves/time, back button.
_Accept: shows stored bests; tapping level navigates to game route._

## M9 — Game screen I (grid + flip)
HUD (moves, timer, pause/restart/home), adaptive card grid, `MemoryCard` flip animation
(rotationY 300ms), ViewModel wiring to engine.
_Accept: UI test — flip two cards, moves=1; mismatch flips back._

## M10 — Game screen II (match/win flow)
Match glow + TTS name + jingle; mismatch gentle sound; win → confetti + WinDialog
(stars animated, moves/time, New Best badge, Play Again / Choose Pack).
_Accept: debug small-deck completes → dialog shows correct stars per formula._

## M11 — Pause overlay + rotation safety
Pause overlay (Resume/Restart/Home); rotation retains full game state via ViewModel.
_Accept: rotate mid-game on phone + tablet — cards/moves/timer intact._

## M12 — Settings + ParentGate
Math gate (sums ≤ 20, 3 options), toggles persist, language row disabled "coming soon",
reset progress (double confirm + gate again).
_Accept: UI test — gate blocks/fails/passes correctly; toggles survive restart._

## M13 — Real art + audio assets
Generate/copy final 96 card images (512px WebP, consistent style), UI sounds, bg music
into `assets/` per DATA_SOURCE.md; update packs.json icon paths.
_Accept: manifest validation test passes; visual review of all 96 cards._

## M14 — Polish pass
Button bounce feedback, screen transitions, confetti cap on low-RAM, content descriptions
for accessibility, all strings in strings.xml (lint check for hardcoded text).
_Accept: `lintDebug` zero hardcoded-string warnings; kid-playtest notes addressed._

## M15 — Full test sweep
Run TESTING.md §1–§3 completely (unit, UI, manual QA matrix, Fire HD 10).
_Accept: all gates in TESTING.md §4 green._

## M16 — Release build + store prep
Release AAB via release.yml, permission dump (no INTERNET), size check, store listing
assets (icon, feature graphic, screenshots), Families + Data Safety forms.
_Accept: PRODUCTION_CHECKLIST.md fully ticked; staged rollout done.
