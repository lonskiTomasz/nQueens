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

## Design source

The UI spec is not in this repo. It lives in a Claude Design project:

<https://claude.ai/design/p/be69d229-833b-4d13-8e28-4e23109364ca>

Read it with the design MCP (`https://api.anthropic.com/v1/design/mcp`, authenticate via
`/design-login`) — `DesignSync` `get_file` against project
`be69d229-833b-4d13-8e28-4e23109364ca`, path `N-Queens Design.dc.html`.

That one file is the entire spec, and it is the design's source rather than a picture of
it: colour and type tokens for both schemes (with the Kotlin for `Color.kt`, `Type.kt` and
`Theme.kt` spelled out), all five screens as markup with exact sizes, spacing and copy,
plus animation timings and touch-target rules. Every value is stated, so nothing needs to
be measured or eyedropped — no exported images are required to build the UI.

Each screen is an `<x-import>` block; `sc-for` / `sc-if` are template directives whose
sample data sits in the `<script type="text/x-dc">` block at the end of the file. The
project's two other files, `android-frame.jsx` and `support.js`, are canvas scaffolding —
a device bezel and the template renderer. They contain no app design; the bezel only draws
system chrome that Android provides at runtime.

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

* Root package `com.queens.puzzle`.
* Declare dependencies in `gradle/libs.versions.toml`; never inline coordinates in
  `build.gradle.kts`.
* User-facing text goes in `res/values/strings.xml`, not in composables.
* Sources live in `src/main/kotlin` (and `src/test/kotlin`), not `src/main/java`.
* Tests use hand-written doubles named `Test*` after the interface they stand in for, under
  `src/test/kotlin/com/queens/puzzle/testing/` — no mocking framework.
* Conventional Commits, linear history, every commit builds green.
* Minimum board size is 4; below that the puzzle has no solution.
