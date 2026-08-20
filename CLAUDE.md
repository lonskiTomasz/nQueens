# CLAUDE.md

## Project

N-Queens puzzle game for Android — Kotlin, Jetpack Compose, Material 3.

The player places `n` queens on an `n × n` board so no two share a row, column, or
diagonal, against a running clock. Board sizes 4–12, live conflict highlighting, undo and
reset, a player-chosen light/dark theme, resumable sessions, and a stored history of solve
times.

## Architecture

**Read `docs/Architecture.md` before making structural changes.** It is the plan of
record: layering, package layout, domain design, data model, presentation, testing
strategy, and a decision log explaining why each choice was made over its alternatives.

Layering follows the [Android architecture guide](https://developer.android.com/topic/architecture).
Dependencies point one way, downwards:

```
ui ──→ domain ──→ data ──→ model
```

* `model` is the shared vocabulary and depends on nothing; keep it free of Android types.
* Repository **interfaces live in `data`**, beside their implementations — not in `domain`.
* `ui` may import `data`: a ViewModel injecting a repository is normal. What it must not do
  is reach past a repository to a DAO or DataStore directly.
* `domain` holds the game rules and the use cases that combine repositories. It is optional
  by the guide's framing — do not add a use case that only forwards one call.

This is deliberately not Clean Architecture; see §2 and decision log rows 2, 13 and 14 in
`docs/Architecture.md`. There is no architecture-enforcement test — the layering is held by
convention and review.

## Build and test

```bash
./gradlew :app:testDebugUnitTest         # JVM unit tests — the primary gate
./gradlew :app:assembleDebug             # build the debug APK
./gradlew :app:installDebug              # install on a connected device or emulator
./gradlew :app:lintDebug                 # Android lint
./gradlew :app:connectedDebugAndroidTest # instrumented + Compose UI tests
```

`connectedDebugAndroidTest` needs a running emulator or an attached device; without one it
cannot run, so it does not gate local development. See §11 of `docs/Architecture.md` for
the plan to move those tests onto the JVM.

## Conventions

* Declare dependencies in `gradle/libs.versions.toml`; never inline coordinates in
  `build.gradle.kts`.
* User-facing text goes in `res/values/strings.xml`, not in composables.
* Spacing and sizes shared across screens come from `Spacing` / `Dimens` in
  `ui/designsystem/theme`. A value one composable owns stays with it — a named `val` if it is
  reused or needs explaining, an inline `.dp` literal if the call site already reads clearly.
* Tests use hand-written doubles named `Test*` after the interface they stand in for, under
  `src/test/kotlin/com/queens/puzzle/testing/` — no mocking framework.
* Conventional Commits, linear history, every commit builds green.
* Minimum board size is 4; below that the puzzle has no solution.
