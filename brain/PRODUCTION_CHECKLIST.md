# PRODUCTION CHECKLIST — Kids Memory Match

Complete in order before every store release.

## 1. Versioning
- [x] Bump `versionCode` (1) and `versionName` ("1.0.0") in `app/build.gradle.kts`.
- [x] CHANGELOG.md entry with date + highlights.

## 2. Build verification
- [x] `./gradlew lintDebug` clean (0 errors), all 52 unit tests green (`testDebugUnitTest`).
- [x] Release AAB built via `bundleRelease` (`app/build/outputs/bundle/release/app-release.aab`, 6.94 MB).
- [x] Release APK built via `assembleRelease` (`app/build/outputs/apk/release/app-release.apk`, 5.04 MB).
- [x] Permission verification on release manifest: **NO** `android.permission.INTERNET`.
- [x] AAB size < 60MB (Actual: 6.94 MB — under 12% of ceiling).
- [x] QA matrix from TESTING.md §3 fully green (8 packs × 3 levels, rotation, audio, offline, physical Amazon Fire tablet verified).

## 3. Play Console — Designed for Families
- [x] Target audience: "5 and under" + "Ages 6–8" documented in `store/LISTING.md`.
- [x] Data Safety form: "No data collected or shared" (documented in `store/LISTING.md` & `store/PRIVACY_POLICY.md`).
- [x] Content rating questionnaire: Everyone (PEGI 3 / USK 0).
- [x] No ad SDKs declared (100% ad-free).

## 4. Store listing (Play + Amazon Appstore)
- [x] Title: "Kids Memory Match: Brain Game" (29 chars, ≤ 30 chars).
- [x] Short description (≤ 80 chars): "Fun animal & picture memory matching card game for kids! 100% offline & safe."
- [x] Full description: documented in `store/LISTING.md` (mentions 8 packs, 3 difficulties, offline, no ads, ages 2–8).
- [x] Icon 512×512, feature graphic, screenshots captured (`m14_home.png`, `m14_gameboard.png`, `m14_settings.png`, etc.).
- [x] Category: Educational → Puzzle (Play); Kids (Amazon).
- [x] Price: $1.99 USD, no ads flag.
- [x] Privacy policy URL & text: created at `store/PRIVACY_POLICY.md` (states zero data collection).

## 5. Release
- [ ] Upload AAB to Play Console (internal → closed → production) and APK to Amazon Appstore.
- [ ] Rollout: 20% staged for 48h, monitor vitals, then 100%.
- [ ] Tag release in git: `v1.0.0`; attach AAB to GitHub release.

## 6. Post-release (48h)
- [ ] Crash rate < 1%, ANR < 0.5% in Play vitals.
- [ ] Read first 50 reviews; log issues to CHANGELOG/DECISIONS as follow-ups.
