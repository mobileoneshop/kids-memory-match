# AGENT INSTRUCTIONS — Kids Memory Match

You are building "Kids Memory Match" (package com.one.memorymatch), a kids'
memory card game in Kotlin + Jetpack Compose. The brain/ folder holds 19 spec
files — they are the source of truth.

## Operating rules (never break these)

1. Read before you act. Before ANY work, read brain/README.md,
   brain/MASTER_RULES.md, and the spec files relevant to the task.
2. Only the named microtask. Work ONLY on the microtask the user names
   (e.g. "do M4" from brain/MICROTASKS.md). Never start another microtask on your own.
3. No scope creep. NEVER modify files outside the current microtask's scope.
   No drive-by refactors, no "improvements" to unrelated code, no renaming things
   you weren't asked to touch.
4. No new dependencies. NEVER add a library without asking the user first AND
   logging it in brain/DECISIONS.md.
5. Locked decisions are locked. App name, package com.one.memorymatch, price
   $1.99, 8 v1 packs, levels (Easy 4x3 / Medium 4x4 / Hard 6x4), star formula,
   no ads, no internet permission, English-only — do NOT change any of these
   without the user's explicit approval.
6. Spec conflicts. If the user's request conflicts with a spec file, STOP and
   flag the conflict. Do not silently override the spec.
7. Finish clean, then stop. After a microtask: compile, run its tests, verify the
   acceptance criteria in brain/MICROTASKS.md, append a brain/CHANGELOG.md entry,
   then STOP and report. Wait for the user before starting the next microtask.
8. Ask before deleting any file or code.
9. Short reports. Say what you changed, which tests passed, and how the user can
   verify it. No long essays.
10. Change requests: modify ONLY what was asked; summarize the diff.
11. Test requests: run the relevant tests from brain/TESTING.md and report
    pass/fail honestly. Never claim tests passed without running them.

## How the project flows
M1 → M2 → … → M16, one microtask at a time, each approved by the user before the
next begins.
