# UI SPEC — Kids Memory Match

## 1. Design system
- **Font:** Baloo 2 (bundled in `res/font/`), weights 600/700 for headings, 500 for body.
- **Shapes:** cards 24dp rounded corners; buttons 32dp full-rounded (pill).
- **Colors:** bright kid palette. Each pack screen uses its pack's `primaryColor` →
  `darkColor` gradient background. Home background: sky gradient (#B3E5FC → #E1F5FE)
  with floating clouds.
- **Touch targets:** minimum 64dp for all kid-tappable elements (MASTER_RULES).
- **No dark theme v1** — kids app, always bright.

## 2. Screens

### Splash
Logo (smiling brain + cards) bounces in, app name pops letter by letter, then
auto-navigates to Home after ~1.2s. Preloads SoundPool + packs.json during splash.

### Home — pack select
- Title "Pick a Pack!" with mascot wave.
- Grid of pack tiles (2 columns phone, 3–4 tablet): pack icon art, name, best stars
  earned across its levels (0–9 stars shown as 3 star rows? simpler: total stars / 9).
- Last tile: **All Mix** (rainbow tile, shuffle icon).
- Top-right: gear icon → ParentGate → Settings.

### Level Select (per pack)
- Pack-themed header with big pack art.
- 3 chunky cards: EASY (4×3, "6 pairs"), MEDIUM (4×4, "8 pairs"), HARD (6×4, "12 pairs"),
  each showing best stars + best moves/time for that level.
- Back button prominent.

### Game
- HUD top: moves count (paw icon), timer (clock icon), pause button, restart button,
  home button. All ≥ 48dp, kid-proof spacing.
- Card grid: adaptive — cards fill width, square, 8dp gaps. Face-down = pack-colored
  pattern with big "?" / paw print. Tap → 3D flip (rotationY, 300ms), soft pop sound.
- Match: cards glow + star burst particles, TTS speaks name ("Lion!"), match jingle.
- Mismatch: gentle "boing", both flip back after 900ms. Text never says "wrong".
- Pause: overlay with Resume / Restart / Home (big buttons).
- Win: confetti + fanfare → Win dialog: stars earned (animated 1-2-3), moves, time,
  "New best!" badge if applicable, buttons: Play Again / Choose Pack.

### Settings (behind ParentGate)
- Sound on/off, Music on/off (big toggle switches).
- Language: "English" selected, other languages greyed "coming soon".
- Reset progress (double-confirm: "Are you sure?" → gate again).
- About: version, "Made with love", no links.

### ParentGate
Full-screen: "Grown-ups only!" + random addition (sums ≤ 20) with 3 big numeric
options. Correct → open Settings. Wrong → gentle shake, new question.

## 3. Components
- `MemoryCard(item, faceUp, matched, onClick)` — the flip card.
- `StarRow(stars)` — 3 stars, earned ones filled gold with pop animation.
- `PackTile(pack, totalStars, onClick)`.
- `LevelCard(level, best, onClick)`.
- `BigButton(text, onClick)` — pill, 64dp height, Baloo 700.
- `ParentGate(onPass)`.
- `WinDialog(...)`, `PauseOverlay(...)`.

## 4. Kid-UX laws
- Only positive language: "Try again!", "You did it!", "Great matching!".
- No countdown timers that create pressure; timer counts UP only.
- Every screen reachable back to Home in ≤ 2 taps.
- No text smaller than 16sp for kid-facing content.
- All buttons give immediate feedback (scale bounce 0.95 → 1.0).
- Confetti/particles: cap counts on low-RAM devices (see ERROR_HANDLING.md).

## 5. States
- Loading: cute spinner (spinning star) while packs.json parses (< 300ms expected).
- Empty: n/a (content bundled).
- Error: manifest parse fail → friendly error screen "Oops! Something went wrong."
  + Retry button (see ERROR_HANDLING.md).
