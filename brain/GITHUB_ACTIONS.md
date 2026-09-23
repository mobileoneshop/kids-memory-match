# GITHUB ACTIONS — Kids Memory Match

Repo layout: standard Android project at repo root, workflows in `.github/workflows/`.

## 1. `ci.yml` — on every push / PR
```yaml
name: CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew lintDebug
      - run: ./gradlew testDebugUnitTest
      - run: ./gradlew assembleDebug
      - run: ./gradlew connectedCheck   # only if an emulator job is added (see below)
      - uses: actions/upload-artifact@v4
        with: { name: debug-apk, path: app/build/outputs/apk/debug/*.apk }
```

## 2. Emulator UI tests (nightly or on-demand)
- Separate workflow `uitest.yml` (`workflow_dispatch` + nightly cron) running an
  API-29 emulator (low-end profile, closest to minSdk 21 that emulators support well)
  and executing `./gradlew connectedDebugAndroidTest`.
- Compose UI tests: flip-two-cards flow, win flow, parent gate flow.

## 3. `release.yml` — manual dispatch only
```yaml
name: Release
on: workflow_dispatch
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17 }
      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_B64 }}" | base64 -d > keystore.jks
      - run: ./gradlew bundleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      - uses: actions/upload-artifact@v4
        with: { name: release-aab, path: app/build/outputs/bundle/release/*.aab }
```
Required secrets: `KEYSTORE_B64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
Keystore itself is NEVER committed. Also verify the AAB declares no INTERNET
permission before upload (add a check step: `aapt dump permissions`).

## 4. Rules
- `main` branch protection: CI must pass before merge.
- Release workflow never auto-runs; a human taps "Run workflow" after completing
  PRODUCTION_CHECKLIST.md.
