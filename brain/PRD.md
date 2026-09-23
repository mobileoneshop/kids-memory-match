# PRD — Kids Memory Match

## 1. Vision
A cheerful, fully-offline memory card-match game for kids aged 3–7. Kid picks a card
pack (animals, fruits, vehicles…), picks a difficulty, flips cards to find pairs.
Every match celebrates the child; nothing punishes.

## 2. Audience
- Primary: kids 3–7 (big touch targets, spoken feedback, zero reading required to play).
- Buyer: parents (paid upfront $1.99, no ads, no in-app purchases, no data collection —
  the pitch is "safe").

## 3. Card packs
### v1 (8 packs + All Mix)
| pack_id | Name | Theme colors | Items (12 each) |
|---|---|---|---|
| zoo | Zoo Animals | green | lion, elephant, monkey, zebra, giraffe, panda, tiger, hippo, kangaroo, bear, crocodile, penguin |
| farm | Farm Animals | amber | cow, horse, sheep, pig, chicken, goat, duck, donkey, rabbit, dog, cat, rooster |
| sea | Sea Animals | blue | shark, dolphin, octopus, turtle, crab, whale, seahorse, starfish, jellyfish, clownfish, seal, lobster |
| birds | Birds | purple | parrot, sparrow, owl, peacock, eagle, pigeon, duck, flamingo, penguin*, woodpecker, hummingbird, crow |
| fruits | Fruits | red/pink | apple, banana, mango, orange, grapes, strawberry, watermelon, pineapple, cherry, kiwi, peach, lemon |
| vegetables | Vegetables | light green | carrot, tomato, potato, onion, peas, corn, cabbage, spinach, pumpkin, cucumber, radish, broccoli |
| vehicles | Vehicles | orange | car, bus, truck, airplane, train, ship, bicycle, motorcycle, ambulance, fire truck, tractor, helicopter |
| shapes_colors | Shapes & Colors | teal | red circle, blue square, yellow triangle, green star, purple heart, orange diamond, pink oval, brown rectangle, black crescent, white cloud, rainbow, gray hexagon |

*Avoid duplicating penguin in birds if also in zoo — final item lists live in `assets/packs.json`.

### All Mix mode
Virtual pack `allmix`: deck is sampled from all v1 packs. Same 3 levels.

### Phase 2 (post-launch update)
dinosaurs, insects, flowers, space, music, sports — 12 items each. Adding a pack =
new folder + `packs.json` entry only (no code change).

## 4. Levels (per pack)
| Level | Grid | Pairs | Uses items |
|---|---|---|---|
| EASY | 4 × 3 | 6 | 6 random of the 12 |
| MEDIUM | 4 × 4 | 8 | 8 random of the 12 |
| HARD | 6 × 4 | 12 | all 12 |

## 5. Game rules
- Tap a face-down card → flips with animation + soft pop sound.
- Two cards up: match → both glow, item name spoken via TTS ("Lion!"), cheerful sound,
  cards stay face-up with a star burst. Mismatch → gentle sound, cards flip back after
  ~900ms. Never show "wrong".
- Win → confetti + fanfare + Win dialog (stars, moves, time, best records).
- Moves counter counts pairs attempted (one move = 2 flips). Timer counts up.

## 6. Scoring — stars (per pack + level, best saved)
- pairs P, moves M:
  - 3 stars: M ≤ ceil(P × 1.5)
  - 2 stars: M ≤ ceil(P × 2.0)
  - 1 star: otherwise (finishing always earns ≥ 1 star)
- Best moves and best time saved per pack+level. Progress screen shows stars per pack.

## 7. Screens
Splash → Home (pack grid + All Mix tile) → Level Select → Game → Win dialog.
Settings (behind parent gate): sound on/off, music on/off, reset progress, language
(English only in v1, selector disabled with "coming soon").

## 8. Monetization
Paid app, **$1.99** one-time. No ads, no IAP in v1. (ADMOB.md intentionally empty.)

## 9. Non-goals (v1)
Multiplayer, online leaderboards, backend/accounts, ads, IAP, Urdu/other languages
(Phase 2), Phase-2 packs, tablet-specific layouts beyond responsive grid.

## 10. Success metrics
10k downloads in 6 months, ≥ 4.5 Play rating, < 1% crash rate, day-7 retention ≥ 20%.
