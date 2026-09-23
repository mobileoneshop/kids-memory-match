# SECURITY — Kids Memory Match

This is a children's app (ages 3–7). Privacy is a feature, not a checkbox.

## 1. Data minimization (COPPA / Play Designed for Families)
- **Collect zero personal data.** No names, no emails, no accounts, no device IDs.
- **No network:** the app must not declare the INTERNET permission. Verify in every
  release build (`aapt dump permissions`). If any library tries to add it via manifest
  merging, remove the library.
- **No analytics, no crash SDKs, no ad SDKs** in v1. (If ever added: families-compliant
  SDKs only + DECISIONS.md entry + updated Data Safety form.)
- All progress/settings stay in local DataStore. Nothing leaves the device.

## 2. Platform hardening
- `targetSdk 36`; keep dependencies patched (check quarterly).
- All activities `exported=false` except the launcher activity.
- No WebViews, no deep links, no file providers needed.
- `allowBackup=true` is fine (no sensitive data stored).
- Release builds: `minifyEnabled=true`, `shrinkResources=true`; keep crash logs
  out of release (no logcat PII — there is no PII anyway).

## 3. Parent gate
- Settings is the only "grown-up" surface; it sits behind the math gate (UI_SPEC).
- Reset-progress requires passing the gate twice (open + confirm).

## 4. Content safety
- Every bundled image/sound is human-reviewed before release (no AI-generation
  artifacts like extra limbs, no scary imagery).
- Item names: simple, positive English words only.

## 5. Play Data Safety form (answers for release)
- "Does your app collect or share any user data?" → **No.**
- Target audience includes children → complete the Families self-certification
  honestly; ad SDK declarations must stay empty while ADMOB.md is empty.
