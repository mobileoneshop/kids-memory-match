# MASTER RULES — Kids Memory Match

These rules override everything else. If a task conflicts with a rule, follow the rule
and flag the conflict.

1. **Stack is fixed:** Kotlin + Jetpack Compose, single-activity, MVVM. No XML layouts for
   new screens, no Flutter/React Native, no new frameworks without a DECISIONS.md entry.
2. **SDK range:** minSdk 21, targetSdk 36, compileSdk 36. Never use APIs above 21 without
   a runtime version guard (`Build.VERSION.SDK_INT` check) or AndroidX backport.
3. **Offline-first, always:** the game must work fully offline. Do NOT add the INTERNET
   permission. No analytics SDKs, no crash-reporting SDKs, no ad SDKs in v1.
4. **Kids-privacy:** this is a 3–7 kids app (Play "Designed for Families"). Collect zero
   personal data. Nothing leaves the device. See SECURITY.md.
5. **No hard-coded user-visible strings.** Every string goes in `res/values/strings.xml`
   (English, ready for future localization).
6. **No magic numbers for game config.** Grid sizes, pair counts, star thresholds live in
   `GameConfig` / `GameLevel` — never inline in UI code.
7. **Game logic is pure Kotlin.** `game/` package must not import Android classes so it is
   unit-testable on JVM. Android stuff (TTS, SoundPool) lives behind interfaces.
8. **Kid-UX is law (see UI_SPEC.md):** touch targets ≥ 64dp, only positive language
   (never "wrong" — say "try again!"), big rounded shapes, no tiny text, no timers that
   punish, no dead-ends (every screen has a visible back/exit).
9. **Parent gate:** Settings screen opens only after passing the math gate. Never expose
   it directly.
10. **Assets by convention:** images `assets/cards/<pack_id>/<item_id>.webp`,
    sounds `assets/sounds/<pack_id>/<item_id>.mp3`, driven by `assets/packs.json`.
    Never hard-code asset paths in Kotlin — load via the manifest.
11. **Missing asset = graceful fallback,** never a crash. Placeholder card + log. See
    ERROR_HANDLING.md.
12. **Dependencies are minimal.** Before adding any library: check TRD.md's approved list.
    New dependency needs a DECISIONS.md entry with why.
13. **Every microtask ends with:** code compiles, related unit tests pass, and the
    acceptance criteria in MICROTASKS.md are met. Update CHANGELOG.md per task.
14. **Commit style:** `M<n>: <short description>` matching MICROTASKS.md ids.
