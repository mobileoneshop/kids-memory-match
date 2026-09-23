# ERROR HANDLING — Kids Memory Match

Principle: a kids app must **never crash or show a dead end**. Every failure degrades
gracefully with a friendly face.

## 1. Asset / content failures
| Case | Handling |
|---|---|
| `packs.json` missing or invalid | Show friendly error screen ("Oops!") + Retry. Log to logcat. If still failing, keep last-known-good? (v1: no cache — manifest is bundled, so this means corrupt install → advise reinstall via Play). |
| Card image missing | Render placeholder card (pack-colored with "?" art). Log warning with asset path. Game continues. |
| Item sound missing | Fall back to TTS speaking the item name. If TTS also unavailable → play generic match jingle. |
| Music file missing | Play silently (no music, no error shown). |

## 2. Audio failures
| Case | Handling |
|---|---|
| TTS engine missing / init fails | Disable speech features silently; SFX still work. Never block gameplay. |
| TTS language (en-US) data missing | Try `Locale.ENGLISH` fallback; else skip speech. |
| SoundPool load fails | Skip SFX; game continues. |
| Audio focus lost (call, another app) | Pause music; duck SFX. Resume music on focus gain only if musicOn. |

## 3. Storage failures
| Case | Handling |
|---|---|
| DataStore read throws / corrupt | Catch, delete corrupt file, recreate with defaults. Log. Progress resets (acceptable, rare). |
| DataStore write fails | Retry once; if still failing, keep in-memory state for session. |

## 4. Gameplay edge cases
- Rapid double-tap on same card → ignored (card already face-up).
- Tap during the 900ms mismatch flip-back → queued/ignored, never breaks state machine.
- Screen rotation mid-game → ViewModel retains state; grid re-lays out. Timer keeps running.
- Process death (OS kills app) → on return, restart at Home (acceptable v1; no mid-game restore).
- Low-RAM device → reduce confetti particle count, disable blur shadows.

## 5. Global
- Uncaught exceptions: Thread.UncaughtExceptionHandler logs; app restarts to Home
  (no crash dialog for kids — but logcat keeps the stacktrace for dev).
- All `catch` blocks must log with tag `KidsMemory` + context. No empty catches.
- Kid-facing copy for errors: always friendly ("Oops! Let's try again."), never
  technical, never blame the child.
