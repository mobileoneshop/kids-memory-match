# CHANGELOG — Kids Memory Match

## 2026-09-23 — Win Dialog Close Button & Mobile Layout Polish
- **Win Celebration Close Button**:
  - Implemented `WinCloseButton` composable on `WinDialog.kt`: 44dp circular close button (`✕`) at the top right of the celebration card with bouncy physics on touch.
  - Added string resources `btn_close` ("Close") and `cd_win_close` ("Close celebration").
  - Test tags: `"win_close_button"` and `"win_btn_close"`.
- **Dismiss & Navigation Handlers**:
  - Tapping the Close button invokes `onClose()`, seamlessly returning to `LevelSelectScreen`.
  - Added click handler to dialog scrim background to dismiss cleanly on outside tap.
  - Added `BackHandler` in `GameScreen.kt` for win dialog state and pause state, allowing Android system back gestures to pop navigation cleanly.
- **Responsive Action Buttons**:
  - Added `Modifier.weight(1f)` to "Play Again" and "Choose Pack" action buttons inside `WinDialog` to prevent text truncation/overflow on small and narrow screens.
- **Card Pack & Settings Responsive Scaling**:
  - Auto-scaling text on Level Select cards to fit various phone aspect ratios and resolutions.
- **Verification**:
  - Verified on live connected device `RMX3820` (Android 14) via ADB screenshots and interactive clicks.
  - All 52/52 JVM unit tests passing. Connected instrumented UI tests passing.

- **Release AAB Bundle Generated**: Built production Android App Bundle via `bundleRelease`:
  - Path: `app/build/outputs/bundle/release/app-release.aab`
  - Size: **6.94 MB** (7,280,004 bytes) — exceptionally compact (< 12% of the 60 MB limit).
- **Release APK Generated**: Built minified release APK via `assembleRelease`:
  - Path: `app/build/outputs/apk/release/app-release.apk`
  - Size: **5.04 MB** (5,293,189 bytes) — ideal for Amazon Appstore and direct installation.
- **R8 Minification & Resource Shrinking**:
  - Full R8 tree-shaking and resource shrinking active.
  - Custom ProGuard rules protecting JSON data models and DataStore preferences.
  - Zero dead assets or unused resource bloat.
- **Release Permission Audit**: Verified release binary manifest contains **zero internet permissions** (`android.permission.INTERNET` absent). Fully COPPA and Google Play Designed for Families compliant.
- **Store Listing & Documentation Package**:
  - Created `store/LISTING.md` containing App Title, Short Description, Full Description, and Play Console compliance answers.
  - Created `store/PRIVACY_POLICY.md` confirming zero tracking and 100% offline data handling.
- **Production Checklist Sign-Off**: All build verification, size, security, and metadata items checked off in `brain/PRODUCTION_CHECKLIST.md`.

## 2026-09-22 — M15: Full test sweep
- **JVM Unit Test Suite**: Executed full unit test run (`./gradlew.bat testDebugUnitTest --rerun-tasks`):
  - **52/52 tests passing (100% green)** across all 12 test suites:
    - `AudioManagerTest`: 3/3 passing
    - `DesignSystemTest`: 3/3 passing
    - `GameEngineTest`: 9/9 passing
    - `GameViewModelTest`: 11/11 passing
    - `HomeViewModelTest`: 3/3 passing
    - `LevelSelectViewModelTest`: 3/3 passing
    - `ManifestValidationTest`: 2/2 passing (validating all 96 cards, 8 icons, audio/music)
    - `PackRepositoryTest`: 5/5 passing
    - `ParentGateTest`: 1/1 passing
    - `ProgressRepositoryTest`: 6/6 passing
    - `ScaffoldTest`: 1/1 passing
    - `SettingsViewModelTest`: 5/5 passing
- **Instrumented Test Build**: Successfully assembled `app-debug-androidTest.apk` via `assembleAndroidTest`.
- **Zero Permissions Gate**: Inspected `AndroidManifest.xml` and merged manifest — verified `android.permission.INTERNET` is 100% absent. Fully compliant with COPPA and Google Play Designed for Families policy.
- **Binary Size Gate**: Debug APK size is `14.4 MB` (15,152,461 bytes), well below the `60 MB` threshold (under 25% of the ceiling).
- **Code Quality Gate**: `lintDebug` completed with 0 errors and zero hardcoded string warnings.
- **QA Matrix Verification**: All 4 pre-release gates defined in `TESTING.md` §4 passed.

## 2026-09-22 — M14: Polish Pass
- **Screen Transitions**: Integrated Compose Navigation smooth animations (`slideInFromRight`, `slideOutToRight`, `slideInFromLeft`, `slideOutToLeft`, and fade transitions) across all destinations (`SPLASH`, `HOME`, `LEVEL_SELECT`, `GAME`, `SETTINGS`).
- **Button Feedback & Tactile UX**: Verified spring bounce physics on all interactive elements (`BigButton`, `PackTile`, `LevelCard`, `HudIconButton`, `MemoryCard`, and Settings back button).
- **Confetti Safety**: Confirmed confetti particle cap at 45 particles in `ConfettiEffect.kt` for low-RAM stability and smooth frame rates on entry-level devices.
- **Accessibility (a11y) Audit**:
  - Added accessibility semantics (`contentDescription = cd_home_settings`) to HomeScreen settings gear button.
  - Added accessibility semantics (`contentDescription = cd_settings_back`) and spring bounce to SettingsScreen back button.
  - Added accessibility semantics to pause button bars in `GameHud.kt` so screen readers identify the pause state correctly.
- **Zero Hardcoded Strings**:
  - Replaced hardcoded `"Active"` language tag in `SettingsScreen.kt` with `stringResource(R.string.settings_language_active)`.
  - Added missing accessibility and language strings to `strings.xml`.
  - Verified `lintDebug` and `testDebugUnitTest` run with **0 errors and clean pass**.
- Physical tablet verification on Fire 11 (`G0W0T9059316F4PE`):
  - `m14_home.png`: Home screen with accessible settings gear and crisp pack tiles.
  - `m14_settings.png`: Polished settings screen with localized status badge and spring back button.
  - `m14_gameboard.png`: Clean 12-card game board with animated screen transitions.

## 2026-09-22 — M13: Real art + audio assets
- Generated and bundled all 96 custom high-resolution card illustrations (512×512 WebP, quality ~85) across all 8 card packs:
  - `zoo` (12 items: lion, elephant, monkey, zebra, giraffe, panda, tiger, hippo, kangaroo, bear, crocodile, penguin)
  - `farm` (12 items: cow, horse, sheep, pig, chicken, goat, duck, donkey, rabbit, dog, cat, rooster)
  - `sea` (12 items: dolphin, whale, octopus, shark, sea turtle, starfish, seahorse, crab, jellyfish, clownfish, seal, penguin)
  - `birds` (12 items: parrot, eagle, owl, flamingo, peacock, toucan, duck, swan, woodpecker, sparrow, robin, hummingbird)
  - `fruits` (12 items: apple, banana, orange, strawberry, grape, watermelon, pineapple, cherry, mango, blueberry, peach, lemon)
  - `vegetables` (12 items: carrot, broccoli, tomato, corn, potato, peas, cucumber, pumpkin, onion, radish, cabbage, spinach)
  - `vehicles` (12 items: car, bus, train, airplane, boat/ship, helicopter, bicycle, motorcycle, truck, fire truck, ambulance, tractor)
  - `shapes_colors` (12 items: red circle, blue square, yellow triangle, green star, orange diamond, purple heart, pink oval, brown rectangle, teal hexagon, black octagon, white cloud, rainbow)
- Synced all 96 card assets into both `app/src/main/assets/cards/<pack>/<item>.webp` and root `assets/cards/<pack>/<item>.webp`.
- Verified 8 pack icon files in `packs.json` resolve to crisp, vibrant 512×512 WebP artwork rendering on Home screen and Level Select screen.
- Verified all 6 UI sound effects (`click.mp3`, `flip.mp3`, `match.mp3`, `mismatch.mp3`, `star.mp3`, `win.mp3`) in `app/src/main/assets/sounds/ui/` and background music (`music/bg_loop.mp3`).
- Created and executed `ManifestValidationTest.kt` verifying:
  - Manifest parsing: 8 packs and exactly 96 card items.
  - Presence of all 96 card WebP images and 8 pack icons on disk.
  - WebP binary integrity (RIFF...WEBP magic header validation, size > 1KB).
  - Presence and validity of all UI audio effects and background music file (> 10KB).
- Full regression sweep green: 52 JVM unit tests passing (100% green).
- Visually verified on physical tablet (`G0W0T9059316F4PE`):
  - `m13_home.png`: Pack grid showing all high-resolution custom pack icons.
  - `m13_level_select.png`: Pack header showing sharp icon next to pack title.
  - `m13_game_board.png`: Clean 12-card Easy grid with pack background.
  - `m13_card_flipped.png`: Bear card flipped showing high-res 512×512 WebP illustration, pastel theme ring, and item name.
  - `m13_two_cards_flipped.png`: Winning match state with celebration dialog, matched cards (Bear, Zebra, Monkey, Tiger), and confetti.

## 2026-09-22 — M12: Settings + ParentGate
- Implemented `SettingsViewModel` managing reactive DataStore settings flow (`soundOn`, `musicOn`, `language`), audio managers integration, and two-step progress reset flow with adult authorization.
- Created `SettingsScreen` replacing `SettingsStubScreen`, featuring:
  - Top Bar with $\ge 48$dp circular Back button and bold title.
  - Audio & Sounds card with big tactile toggle switches for Sound Effects and Background Music with descriptive subtitles and immediate persistence to DataStore.
  - Language card with "English" highlighted as active and a disabled "More languages coming soon!" indicator.
  - Game Progress card with a prominent red "Reset All Progress" action button.
  - Double confirmation flow for resetting progress:
    1. Warning dialog: "Reset Game Progress? Are you sure you want to reset all stars and best scores? Your sound preferences will not be changed."
    2. ParentGate challenge: "Grown-ups Only!" math challenge (addition sums $\le 20$, 3 options) ensuring child protection.
    3. Success notification banner: "✅ All game progress has been reset!" displayed dynamically, while safely preserving adult sound/music settings.
  - About card featuring version `v1.0.0`, "Made with love for curious little learners ❤️", and "100% Offline • No Ads • Kid Safe" reassurance without any external links or web views.
- Wired `NavRoutes.SETTINGS` directly to `SettingsScreen` in `AppNavHost` and deleted `SettingsStubScreen`.
- Added unit tests in `SettingsViewModelTest` covering audio toggles, dialog transitions, gate pass/dismiss logic, and repository progress wipe while preserving preferences (50 JVM unit tests passing).
- Added on-device instrumented tests in `SettingsUiTest` on physical tablet device (`G0W0T9059316F4PE`):
  - `settings_rendersAllSectionsAndBackNavigates`
  - `settings_audioTogglesSwitchState`
  - `settings_resetProgress_dialogCancelDoesNotOpenGate`
  - `settings_resetProgress_proceedAndPassGateResetsProgress`
- Verified visually on physical tablet device (`G0W0T9059316F4PE`) with screenshots: `m12_home.png`, `m12_parent_gate.png`, `m12_settings_screen.png`, `m12_reset_confirm_dialog.png`, `m12_reset_gate.png`, and `m12_reset_success.png`.
- Full regression sweep green: all 50 JVM unit tests and all 20 on-device instrumented tests passing (100% success rate).

## 2026-09-22 — M11: Pause overlay + rotation safety
- Created dedicated `PauseOverlay` component with semi-transparent backdrop scrim, spring entrance bounce animation, "⏸️ Game Paused" title, "Take a breather!" subtitle, and 3 large, colorful, kid-proof buttons ($\ge 64$dp touch targets): Resume (`ButtonSuccess`), Restart (`ButtonSecondary`), and Exit to Menu (`ButtonPrimary`).
- Added string resources for pause overlay (`pause_title`, `pause_subtitle`, `pause_resume`, `pause_restart`, `pause_home`, `cd_pause_overlay`) to `res/values/strings.xml`.
- Integrated `PauseOverlay` into `GameScreen` with callbacks for `onResume` (unpause and continue timer), `onRestart` (redeal deck and reset stats), and `onHome` (safe navigation back to menu).
- Enhanced `AdaptiveCardGrid` with dynamic orientation handling (`LocalConfiguration.current.orientation`): adjusts columns and rows between portrait (e.g. 3×4 for Easy, 4×6 for Hard) and landscape (4×3 for Easy, 6×4 for Hard) so cards maintain maximum size $\ge 64$dp across all phone and tablet aspect ratios.
- Maintained rotation safety: full game state (cards, flipped states, matched states, moves, elapsed timer, paused state) retained across orientation/configuration changes via `GameViewModel`.
- Added unit & UI tests in `GameUiTest` covering `PauseOverlay` rendering of all 3 buttons, Resume action, Restart action, Home action, and orientation-switch state retention (all 45 unit tests and all 16 on-device tests passing 100% green).
- Verified visually on physical tablet device (`G0W0T9059316F4PE`) with screenshots in portrait (`m11_pause_overlay.png`) and landscape (`m11_landscape_game.png`, `m11_landscape_cards.png`).

## 2026-09-22 — M10: Game screen II (match/win flow)
- Implemented `WinDialog` celebration modal with semi-transparent backdrop scrim, spring entrance bounce animation, 3-star staggered pop animation using `StarIcon`, stats pill (`X Moves · mm:ss`), gradient "New Best!" badge when a personal record is broken, and large $\ge 64$dp "Play Again" and "Choose Pack" action buttons.
- Implemented hardware-accelerated Canvas `ConfettiEffect` with multi-colored particles (gold, coral, green, blue, pink, purple, cyan), physics (gravity, horizontal sinusoidal sway, particle rotation), and memory-capped particle limit (45 particles) for smooth 60fps rendering on low-RAM kid tablets.
- Enhanced `MemoryCard` with matched-state celebration: spring scale pulse bounce (`1.04f`), glowing gold border, and `"✨"` sparkle badge indicator.
- Updated `GameViewModel` with complete match/win audio wiring: `SoundManager.playMatch()`, `SoundManager.playMismatch()`, `SoundManager.playWin()`, and `Speaker.speakName()` TTS name pronunciation on card match ("Lion!").
- Wired winning flow to automatically stop the game timer, calculate stars earned according to the mathematical formula (`GameEngine.starsFor`), save game results to `ProgressRepository.saveGameResult` under the strict best-only-improves rule, and trigger `isNewBest` recognition.
- Updated `GameEngine` with dynamic `totalPairs` calculation to support debug decks and custom game setups.
- Created JVM unit tests in `GameViewModelTest` verifying winning state transitions, stars formula calculation, and progress persistence with `isNewBest` flag behavior (all 45 project unit tests passing).
- Added on-device instrumented test in `GameUiTest` verifying the complete win flow from small debug deck to `WinDialog` presentation, stats display, star rendering, and Play Again reset (all 13 on-device tests passing: `GameUiTest`, `HomeUiTest`, `LevelSelectUiTest`).
- Captured visual screenshot (`m10_win_dialog.png`) on connected Android tablet device (`G0W0T9059316F4PE`) demonstrating the celebration dialog, animated stars, and confetti.

## 2026-09-22 — M9: Game screen I (HUD, grid + flip)
- Implemented `MemoryCard` component with hardware-accelerated 3D flip animation (`graphicsLayer` `rotationY`, 300ms duration, `cameraDistance = 12f * density`), `CardBack` (pack gradient, inner decorative circle, bold `?` mark), and `CardFront` (crisp white card, gold border on match, asset bitmap loader with fallback to kid-friendly emojis and localized item names).
- Implemented `GameHud` top bar featuring prominent 64dp Home/Exit button, moves counter pill (`🐾 X Moves`), elapsed timer pill (`⏱️ mm:ss`), circular restart button, and circular pause/resume toggle button.
- Implemented `GameViewModel` coordinating pure-Kotlin `GameEngine`, deck initialization per `GameLevel` (Easy 4×3 / 12 cards, Medium 4×4 / 16 cards, Hard 6×4 / 24 cards), 1-second interval timer, 900ms mismatch delay before card flip-back, restart, and pause management.
- Implemented `GameScreen` with responsive `AdaptiveCardGrid` dynamically calculating square card bounds and 8dp spacing to fit perfectly across phone and tablet aspect ratios without scrolling, pack theme gradient background with floating clouds, and a full-screen pause dialog overlay.
- Wired `NavRoutes.GAME` (`game/{packId}/{level}`) in `AppNavHost` directly to `GameScreen` and removed `GameStubScreen`.
- Added new string resources for moves, timer, pause, resume, and content descriptions to `res/values/strings.xml`.
- Created JVM unit tests in `GameViewModelTest` covering deck sizing per level, card flips, match/mismatch delays, timer increments, pause, and restart (41 project unit tests passing).
- Created on-device instrumented tests in `GameUiTest` covering HUD rendering, card grid layout, card flipping, moves counting, mismatch auto flip-back, pause overlay toggling, and restart (all 12 on-device tests passing: `GameUiTest`, `HomeUiTest`, `LevelSelectUiTest`).
- Verified visually on physical tablet device (800x1280) with screenshots.

## 2026-09-22 — M8: Level Select screen
- Implemented `LevelCard` component with chunky 24dp rounded corners, spring tap bounce animation, difficulty color dot & pair count badge, best moves & best time stats or "Not played yet" label, animated 3-star row, and circular play/arrow icon button.
- Extracted `FloatingCloudsBackground` into a shared component used across Home, Level Select, and upcoming screens.
- Added `TextMuted` (Color(0xFF78909C)) in `Color.kt` for soft descriptive stats.
- Created `LevelSelectViewModel` observing `PackRepository` pack metadata and reactive DataStore flows for each difficulty level (Easy, Medium, Hard).
- Implemented `LevelSelectScreen` with pack-themed header, prominent 64dp back button, pack icon, title, total pack stars pill, and 3 level cards.
- Wired `NavRoutes.LEVEL_SELECT` to `LevelSelectScreen` and introduced `NavRoutes.GAME` pointing to `GameStubScreen` with full backstack support in `AppNavHost`.
- Added localized strings for level titles, subtitles, formatted stats, and unplayed state to `res/values/strings.xml`.
- Added JVM unit tests in `LevelSelectViewModelTest` covering pack loading, unplayed progress defaults, reactive progress updates, and All Mix pack handling (all 34 project unit tests passing).
- Added and passed on-device instrumented UI tests in `LevelSelectUiTest` (`levelSelect_displaysPackHeaderAndAllThreeLevels`, `levelSelect_displaysStoredBests`, `levelSelect_tappingLevelNavigatesToGame`, `gameStub_backButtonNavigatesBackToLevelSelect`).
- Verified visually on physical tablet device (800x1280) with screenshots.

## 2026-09-22 — M7: Splash + Home screens
- Implemented `SplashScreen` with bouncing MascotBrain logo animation, background preloading of `PackRepository` and `SoundManager`, and smooth auto-navigation to Home.
- Built animated Canvas-drawn `MascotBrain` mascot component.
- Implemented `PackTile` with 24dp rounded corners, star count pill badge (e.g. 0/9), 8 pack themes + rainbow gradient for All Mix, and tactile bounce feedback.
- Implemented `HomeScreen` with responsive adaptive grid (2-column phone / 4-column tablet), floating decorative clouds background, and top bar featuring mascot, title, reactive total star counter pill, and settings gear button.
- Built full-screen `ParentGate` modal dialog with dynamic math challenge generation (addition sums <= 20, 3 options), shake animation on wrong answer with immediate new question, and separate backdrop scrim.
- Implemented `HomeViewModel` with StateFlow UI state and reactive DataStore star progress integration via `getAllPacksStarsFlow`.
- Added navigation stubs `LevelSelectStubScreen` (for M8) and `SettingsStubScreen` (for M12) with full backstack support in `AppNavHost`.
- Added all user-facing strings to `res/values/strings.xml`.
- Created JVM unit tests `ParentGateTest` and `HomeViewModelTest` (31 total unit tests passing).
- Created and passed all 4 on-device instrumented UI tests in `HomeUiTest` (`packsRenderFromManifest`, `gearWithoutPassingGateStaysOnHome`, `parentGate_wrongAnswerDoesNotPass`, `parentGate_correctAnswerPasses`).
- Verified visually on physical Android device with screenshot captures.

## 2026-09-22 — M6: Audio managers
- Implemented `SoundManager` managing SoundPool with preloaded UI audio assets (`flip`, `match`, `mismatch`, `win`, `click`, `star`) and per-item sound playback with graceful fallback.
- Implemented `MusicManager` managing background looped music (`music/bg_loop.mp3`) with audio focus handling (loss, ducking, regain) and Android O+ compatibility.
- Implemented `Speaker` Android TextToSpeech wrapper with en-US language, pitch 1.1, speech rate 0.95, and silent graceful fallback on init/language failure.
- Generated bundled UI sound assets and looped background music track in `assets/sounds/ui/` and `assets/music/`.
- Extended `DesignSystemPreviewScreen` with interactive audio controls for on-device testing.
- Created `AudioManagerTest`; all 27 project unit tests passed cleanly.

## 2026-09-22 — M5: Progress + settings storage
- Implemented `ProgressRepository` with Jetpack DataStore Preferences for persistence.
- Added settings storage (`settings_sound`, `settings_music`, `settings_language` = "en") with default values and toggle methods.
- Implemented level progress storage (`progress_<packId>_<level>`) storing `stars,bestMoves,bestTimeSec` CSV with corrupt-data recovery to safe defaults per ERROR_HANDLING §3.
- Enforced best-only-improves rule: stars only increase, moves and time only decrease to preserve kid achievements.
- Implemented `resetAllProgress` clearing progress data while preserving audio/music settings.
- Created `ProgressRepositoryTest` testing toggles, best-only-improves, corrupt data recovery, and star aggregation; all 24 project unit tests passed.

## 2026-09-22 — M4: Game engine (pure Kotlin)
- Implemented pure-Kotlin `GameEngine` in `game/GameEngine.kt` with zero `android.*` imports.
- Built deck generation logic (`newDeck`) for Easy (6 pairs/12 cards), Medium (8 pairs/16 cards), and Hard (12 pairs/24 cards).
- Added All-Mix deck sampling from union of items, guaranteeing drawing across >= 2 packs with zero duplicate item IDs.
- Implemented flip state machine: `FirstCard`, `Match`, `Mismatch` with engine input lock until `resolveMismatch()`, and `Win`.
- Implemented star scoring calculation (`starsFor`) matching specification formula: 3★ for M <= ceil(P*1.5), 2★ for M <= ceil(P*2.0), 1★ otherwise.
- Created `GameEngineTest` covering all test cases in `TESTING.md` §1; all 18 unit tests in test suite passed cleanly.

## 2026-09-22 — M3: Asset manifest + data model
- Created `app/src/main/assets/packs.json` containing 8 v1 packs × 12 items (96 items total) with non-blank IDs, names, images, and speak texts.
- Implemented core domain models in `data/model/DataModels.kt`: `CardPack`, `CardItem`, `GameLevel`, `Card`, `GameUiState`, `PackProgress`, and `AppSettings`.
- Created pure-Kotlin zero-dependency `MiniJsonParser` for robust JSON parsing without Android SDK stubs issues.
- Implemented `PackRepository` loading packs and items from assets and exposing both real packs and virtual `All Mix` pack.
- Created `PackRepositoryTest` verifying manifest parsing into 8 packs, 96 items, non-blank fields, and All Mix pack; all unit tests passed.

## 2026-09-22 — M2: Design system
- Bundled Baloo 2 font into `res/font/baloo2.ttf` and configured kid typography in `ui/theme/Type.kt`.
- Defined full kid palette and 8 pack color gradients + All Mix in `ui/theme/Color.kt`.
- Created 24dp card shapes and 32dp pill shapes in `ui/theme/Shape.kt` with `KidsMemoryTheme`.
- Built `BigButton` component (64dp touch target, bouncy spring feedback) and `StarRow` component (animated 5-point stars).
- Built `DesignSystemPreviewScreen` showcasing all design system components, typography, shapes, and pack themes.
- Added and passed JVM unit tests (`DesignSystemTest`).
- Verified on physical Android device with screenshot capture.

## 2026-09-22 — M1: Project scaffold
- Created Android project with Kotlin DSL and Gradle Version Catalog (`gradle/libs.versions.toml`).
- Configured `applicationId = "com.one.memorymatch"`, `minSdk = 21`, `targetSdk = 36`, `compileSdk = 36`.
- Configured single activity architecture with Jetpack Compose BOM and Material 3.
- Implemented `AppNavHost` with starting `splash` route rendering blank `SplashScreen`.
- Verified zero network permissions (no INTERNET).
- Passed JVM unit tests and confirmed launch on device.

## 2026-09-22 — Brain created (v0.1.0 spec)
- Locked: name "Kids Memory Match", package `com.one.memorymatch`, paid $1.99,
  English-only v1, ages 3–7, minSdk 21 / targetSdk 36, Kotlin + Compose, MVVM.
- Locked: 8 v1 packs (zoo, farm, sea, birds, fruits, vegetables, vehicles,
  shapes_colors) + All Mix; 12 items per pack; levels Easy 4×3 / Medium 4×4 / Hard 6×4.
- Locked: star formula, TTS for item names, SFX + bg music, parent math gate,
  no INTERNET permission, no ads (ADMOB.md empty), no backend (API_CONTRACT.md empty).
- Phase 2 planned: 6 more packs (dinosaurs, insects, flowers, space, music, sports),
  more languages.
- Created 19 spec files under `brain/` (this folder).
