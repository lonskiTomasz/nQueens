# N-Queens

[![Build](https://github.com/lonskiTomasz/nQueens/actions/workflows/build.yml/badge.svg)](https://github.com/lonskiTomasz/nQueens/actions/workflows/build.yml)

An N-Queens puzzle game for Android — Kotlin, Jetpack Compose, Material 3.

Place `n` queens on an `n × n` board so no two share a row, column or diagonal, against a
running clock. Board sizes 4–12, live conflict highlighting, undo and reset, a resumable
board, a history of solve times with bests per size, and a light/dark theme toggle.

## Demo

https://github.com/user-attachments/assets/f5056e70-2850-40fa-87a1-c12e22d3ef95

## Requirements

* JDK 17+ (21 is used here)
* Android SDK with **compileSdk 37**, pointed at by `local.properties` (`sdk.dir=…`)
* minSdk 23 — no Gradle install needed, the wrapper handles it

## Build, test, run

```bash
./gradlew :app:testDebugUnitTest         # JVM unit tests — the primary gate
./gradlew :app:assembleDebug             # build the debug APK
./gradlew :app:installDebug              # install on a connected device or emulator
./gradlew :app:lintDebug                 # Android Lint
./gradlew :app:connectedDebugAndroidTest # Compose, DAO and end-to-end tests — needs a device
```

## Architecture

One Gradle module. Layering follows the
[Android architecture guide](https://developer.android.com/topic/architecture) —
dependencies point one way, downwards:

```
ui ──→ domain ──→ data ──→ model
```

```
com.queens.puzzle
├── model/     BoardSize, Position, GameSession, Solve … the shared vocabulary, no Android imports
├── core/      designsystem (theme, components) · util (duration and date formatting)
├── domain/    game (GameReducer — the pure state machine) · rules (BoardEvaluator) · usecase
├── data/      repository (interfaces + Default* impls) · local (Room, DataStore) · mapper · di
└── ui/        home · game (+ board, feedback) · win · besttimes · navigation
```

Two things that are commonly the other way round: repository **interfaces live in `data`**
beside their implementations, not in `domain`; and **`ui` may import `data`** — a ViewModel
injecting a repository is normal, what it must not do is reach past one to a DAO.

### One tap, end to end

```
tap ─→ GameViewModel ─→ GameSession.reduce(TapSquare)   pure, no Android
                     ─→ BoardEvaluator.evaluate()       pure, O(n) conflicts
                     │
                     ├─→ GameUiState ────────────────→  recompose
                     ├─→ SessionRepository.save()       resumable board
                     └─→ GameEffect ─────────────────→  haptics · sound · navigate
                           (Channel: fires exactly once)
```

`GameUiState` is fully derived — each square carries its own `hasQueen` / `isConflicting` /
`isAttacked`, so the composable renders and never computes. The attack overlay is the one
part that costs more than a linear pass, so `evaluate` only covers those squares when the
setting that draws them is on.

## Architecture decisions

| Decision                                             | Why                                                                                                                                                                                                                                                                                |
|------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Single module, layered packages                      | The app is one feature. Packages mirror what modules would be, so extracting them later is a move, not a rewrite.                                                                                                                                                                  |
| Android guide layering, **not** Clean Architecture   | Ports in `domain` cost a second interface per repository, to buy substitutability that `data` already has by declaring the interface at all.                                                                                                                                       |
| The game is a pure reducer                           | `GameSession.reduce(action): GameSession` — a rules test is one call and one assertion. New mechanics are a new `GameAction` branch.                                                                                                                                               |
| Conflicts are derived, never stored                  | Recomputed from the queens on every change, so there is no second source of truth to drift. Counting per line makes conflicts O(n), not the naive pairwise O(n²). Covering the attacked squares is the exception at O(n²), so it is computed only when the overlay is switched on. |
| The solver lives in **test** sources                 | Nothing in the app calls it. It is an independent oracle proving `BoardEvaluator` against a second implementation.                                                                                                                                                                 |
| Use cases only where logic exists                    | "New best, −54s" earns a tested home outside the ViewModel; pass-through reads go ViewModel → repository directly.                                                                                                                                                                 |
| Every solve in Room, bests derived in SQL            | A history screen with dates and deltas is impossible from best-only rows. Settings and the resumable session go to DataStore — different shape, different lifetime.                                                                                                                |
| One-shot effects on a `Channel`                      | Feedback and navigation must fire once. A boolean in `UiState` replays on every configuration change.                                                                                                                                                                              |
| A design system package, tokens named for their role | Colour, type and spacing have one owner in `core/designsystem`, and nothing in it knows the game rules. Dynamic colour stays off: the board shades and the conflict accent are the product's identity.                                                                             |
| Hand-written fakes, no mocking framework             | They compile against the real interfaces, so changing one breaks its double at compile time.                                                                                                                                                                                       |

## Testing

The pyramid is deliberately bottom-heavy: the rules are pure functions, so most of the value
comes from JVM tests that run in seconds, need no emulator.

Those cover the model, the rules and the reducer — placing and removing, undo, reset, and
conflicts on rows, columns and both diagonals — with the backtracking solver in test sources
acting as an independent oracle: it generates real solutions, and the tests assert that
`BoardEvaluator` calls every one of them solved and stops doing so the moment a queen moves.
That checks the rules against a second implementation of the problem rather than against
fixtures, which is where hand-written rule tests usually go wrong. Above them sit the use
cases, the repositories and the session serializer, and the ViewModels — state transitions,
one-shot effects, settings and session round-trips, and the timer, driven on a virtual clock
so elapsed time is asserted exactly rather than waited for.

The rest needs a device. Compose tests drive each screen through its stateless overload —
tapping a square, the queens-left counter, the settings sheet, the reset dialog, the win
screen — including the layouts at forced window sizes; the DAO runs against real SQLite; and
one end-to-end test solves a board from launch to the win screen over the real database and
stores.

The payoff is that everything worth getting right is provable without hardware: a broken rule,
a wrong state transition or a mishandled resume fails in seconds, and the device-only
tests are left to confirm what only a device can — that the pixels, the queries and the wiring
agree with them.

Doubles are hand-written and named after the interface they stand in for. They compile against
the real interfaces, so changing one breaks its double at compile time.
