# TRD — Kids Memory Match

## 1. Stack
| Layer | Choice |
|---|---|
| Language | Kotlin (JVM target 17) |
| UI | Jetpack Compose (BOM), Material3 |
| Navigation | navigation-compose, single Activity |
| Async | Kotlin coroutines + Flow |
| Persistence | DataStore (Preferences) — progress, stars, settings |
| Audio SFX | SoundPool |
| Background music | MediaPlayer (single looped mp3) |
| Speech (item names) | Android TextToSpeech, en-US, pitch 1.1, rate 0.95 |
| DI | Manual (no Hilt — small app; constructors + singletons) |
| Build | Gradle Kotlin DSL, version catalog |

## 2. SDK / build config
- `applicationId = "com.one.memorymatch"`
- `minSdk = 21`, `targetSdk = 36`, `compileSdk = 36`
- `versionCode = 1`, `versionName = "1.0.0"` (bump per release, see PRODUCTION_CHECKLIST.md)
- Permissions: **none** (no INTERNET). Vibrate not needed.
- `android:allowBackup="true"`, all activities `exported=false` except launcher.
- Target ~< 60MB AAB: WebP images, single music track, no unused resources
  (`shrinkResources=true`, `minifyEnabled=true` on release).

## 3. Approved dependencies (do not add others without DECISIONS.md entry)
- androidx.compose.bom, material3, navigation-compose
- datastore-preferences, lifecycle-viewmodel-compose, lifecycle-runtime-compose
- coroutines-android
- Test: junit4, coroutines-test, compose-ui-test-junit4, mockk (or fakes — prefer fakes)

## 4. Modules (single module `:app`)
```
com.one.memorymatch/
  ui/        # Compose screens, components, theme
  game/      # pure-Kotlin engine (NO android.* imports)
  data/      # repository, DataStore, asset manifest loader
  audio/     # SoundManager, MusicManager, Speaker (TTS)
  util/      # helpers
```

## 5. Audio pipeline
- SFX via SoundPool (flip, match, mismatch, win, click, star). Preload at app start.
- Item sounds: `assets/sounds/<pack>/<item>.mp3` if present; else fall back to TTS name.
- Music: `assets/music/bg_loop.mp3`, duck/pause on audio-focus loss.
- All sounds toggleable in Settings (persisted).

## 6. Asset pipeline
- Card art: 512×512 WebP, flat cute vector style, consistent across packs.
- Manifest: `assets/packs.json` lists packs and items → adding content needs no code change.
- Loader validates manifest at startup; missing file → placeholder card, never crash.

## 7. Performance budgets
- 60fps card flip animation; deck shuffle < 16ms for 24 cards.
- Cold start < 2s on a 2020 mid-range device.
- No jank on Fire HD 10 (Amazon Appstore target device).
