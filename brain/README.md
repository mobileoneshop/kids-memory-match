# Kids Memory Match — AI Brain

Spec folder for the **Kids Memory Match** Android game (native Kotlin + Jetpack Compose,
package `com.one.memorymatch`). Any AI coding agent building this app must read these files
first, in this order:

1. `MASTER_RULES.md` — non-negotiable rules for every change.
2. `PRD.md` — what the product is.
3. `TRD.md` — the technical stack and constraints.
4. `ARCHITECTURE.md` — how the code is organized.
5. `DATA_MODEL.md` — the data classes and persistence.
6. `DATA_SOURCE.md` — where card images/sounds come from and how they are named.
7. `UI_SPEC.md` — screens, components, animations, kid-UX rules.
8. `ERROR_HANDLING.md`, `SECURITY.md` — edge cases and kids-privacy rules.
9. `TESTING.md`, `GITHUB_ACTIONS.md`, `PRODUCTION_CHECKLIST.md` — quality and release.
10. `MICROTASKS.md` — the build broken into small tasks (work through in order).
11. `DECISIONS.md`, `CHANGELOG.md` — why things are the way they are.

Placeholders (intentionally empty for v1):
- `ADMOB.md` — paid app, no ads in v1.
- `API_CONTRACT.md` — no backend in v1.
- `SCRAPING_SPEC.md` — no scraping needed (all assets bundled).

## Locked decisions (2026-09-22)
- App name: **Kids Memory Match** · package: `com.one.memorymatch` · paid **$1.99**
- Language: English only (more later) · Age: 3–7 · minSdk 21 / targetSdk 36
- v1 packs (8): Zoo, Farm, Sea, Birds, Fruits, Vegetables, Vehicles, Shapes & Colors + All Mix
- Phase 2 packs (6): Dinosaurs, Insects, Flowers, Space, Music, Sports
- 12 items per pack · Levels: Easy 4x3, Medium 4x4, Hard 6x4
