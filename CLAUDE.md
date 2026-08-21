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
strategy, and a decision log explaining why each choice was made over its
alternatives.

Layering follows the [Android architecture guide](https://developer.android.com/topic/architecture).
Dependencies point one way, downwards:

```
ui ──→ domain ──→ data ──→ model
```

* `model` is the shared vocabulary and depends on nothing; keep it free of Android types.
* Repository **interfaces live in `data`**, beside their implementations — not in `domain`.
* `ui` may import `data`: a ViewModel injecting a repository is normal. What it must not do
  is reach past a repository to a DAO or DataStore directly.
* `domain` holds the game rules and the use cases that combine repositories; it is optional
  by the guide's framing.

## Build and test

```bash
./gradlew :app:testDebugUnitTest         # JVM unit tests — the primary gate
./gradlew :app:assembleDebug             # build the debug APK
./gradlew :app:installDebug              # install on a connected device or emulator
./gradlew :app:lintDebug                 # Android lint
./gradlew :app:connectedDebugAndroidTest # instrumented + Compose UI tests
```

## Conventions

* Declare dependencies in `gradle/libs.versions.toml`.
* User-facing text goes in `res/values/strings.xml`, not in composables.
* Conventional Commits, linear history, every commit builds green.
