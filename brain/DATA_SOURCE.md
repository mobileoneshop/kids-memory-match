# DATA SOURCE — Kids Memory Match

All content is bundled in the APK/AAB. No network, no backend, no scraping.

## 1. Asset tree (`src/main/assets/`)
```
assets/
  packs.json                 # manifest (see DATA_MODEL.md §4)
  cards/<pack>/<item>.webp   # 512×512 card art
  sounds/<pack>/<item>.mp3  # optional per-item sound (animal sound, horn…)
  sounds/ui/flip.mp3
  sounds/ui/match.mp3
  sounds/ui/mismatch.mp3
  sounds/ui/win.mp3
  sounds/ui/click.mp3
  sounds/ui/star.mp3
  music/bg_loop.mp3          # cheerful loop, ~60–90s, normalized -14 LUFS
```

## 2. Naming conventions
- All lowercase `snake_case`: `sea_animals` pack → id `sea`; item `clownfish`.
- Image: `<item>.webp`; sound: `<item>.mp3`. Same stem, same folder.
- If an item sound is missing, loader sets `soundAsset = null` → TTS fallback speaks
  the name. Game must never break on a missing sound.

## 3. Art spec (for whoever generates the images — AI image tool)
- 512×512 px, WebP quality ~85.
- Style: flat cute vector, thick outlines, big friendly eyes, solid pastel background
  circle per pack color. Consistent style across ALL packs (same prompt seed/style).
- Subject centered, fills 70% of frame. No text in the image (names come from manifest).
- Pack tile icon: one representative item on the pack's primary color.

## 4. Audio spec
- Item sounds: real recordings preferred (lion roar, car horn), ≤ 2s, mp3 128kbps.
  If unavailable for an item → omit file, TTS covers it.
- UI sounds: soft, non-startling (kids!). mismatch = gentle "boing", never a buzzer.
- Music: cheerful ukulele/marimba loop, no lyrics, fades in/out on screen changes.

## 5. Adding a new pack (Phase 2 or later) — no code change
1. Generate 12 images → `assets/cards/<newpack>/`.
2. (Optional) item sounds → `assets/sounds/<newpack>/`.
3. Append pack entry to `assets/packs.json`.
4. Add `pack_<newpack>` string to `strings.xml` (name must come from resources for
   future localization; manifest `name` is the v1 English default).
5. Run the manifest validation test (TESTING.md).

## 6. strings.xml
Every user-visible string lives here, English v1. Item names: prefer manifest-driven
(`CardItem.speakText`) so new packs don't need code; strings.xml holds UI chrome
(buttons, dialogs, settings labels).
