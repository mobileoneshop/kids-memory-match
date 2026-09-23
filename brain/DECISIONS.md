# DECISIONS — Kids Memory Match

Log of product/technical decisions. Newest first. Format: date — decision — why.

- **2026-09-22 — App icon v3 approved by normathi.** YouTube-Kids-thumbnail style:
  giant glossy 3D lion face on gold card, green "?" card behind, rainbow/confetti
  background. Saved at `assets/icon/app-icon-512.webp` (resize to 512×512 for Play).
- **2026-09-22 — 12 items per pack (not 8).** Levels need 6/8/12 pairs; 12 items
  covers Hard (6×4) exactly. 8 packs × 12 = 96 card images for v1.
- **2026-09-22 — 8 packs in v1, 6 packs in Phase 2.** normathi approved: v1 ships
  faster; Phase-2 packs become "NEW PACKS!" update marketing.
- **2026-09-22 — Item names spoken via on-device TTS (not recorded audio).**
  Avoids recording 96+ voice clips; English en-US, pitch 1.1. Real SFX only where
  easy (animal sounds, horns).
- **2026-09-22 — No INTERNET permission.** Simplifies Play Families compliance and
  Data Safety ("no data collected"); game is fully offline by design.
- **2026-09-22 — DataStore (Preferences) instead of Room.** Progress data is tiny
  flat key-values; Room is overkill. Revisit if schema grows.
- **2026-09-22 — Paid $1.99, no ads, no IAP.** Parents buy safety; ADMOB.md stays
  empty for v1.
- **2026-09-22 — Jetpack Compose (not XML).** Matches normathi's existing Compose
  experience (Speed Meter project); faster UI iteration for kid-friendly design.
- **2026-09-22 — Manual DI (not Hilt).** App is small; Hilt adds build complexity
  for little gain.
- **2026-09-22 — English only in v1.** More languages later; all strings already in
  strings.xml and manifest is language-ready.
- **2026-09-22 — Parent gate = math question (sums ≤ 20).** Simple, no accounts,
  kid-proof enough for 3–7.
- **2026-09-22 — Single `:app` module.** No multi-module overhead for this size.
