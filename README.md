# N-Queens

An N-Queens puzzle game for Android — Kotlin, Jetpack Compose, Material 3.

Place `n` queens on an `n × n` board so no two share a row, column or diagonal, against a
running clock. Board sizes 4–12, live conflict highlighting, undo and reset, a resumable
board, a history of solve times with bests per size, and a light/dark theme toggle.

## Requirements

* JDK 17+ (21 is used here)
* Android SDK with **compileSdk 37**, pointed at by `local.properties` (`sdk.dir=…`)
* minSdk 23 — no Gradle install needed, the wrapper handles it

## Build, test, run

```bash
./gradlew :app:testDebugUnitTest         # 165 JVM unit tests — the primary gate
./gradlew :app:assembleDebug             # build the debug APK
./gradlew :app:installDebug              # install on a connected device or emulator
./gradlew :app:lintDebug                 # Android Lint
./gradlew :app:connectedDebugAndroidTest # 46 Compose + end-to-end tests — needs a device
```

There is no CI and no ktlint/detekt/coverage setup yet.

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
                     ─→ BoardEvaluator.evaluate()       pure, O(n)
                     │
                     ├─→ GameUiState ────────────────→  recompose
                     ├─→ SessionRepository.save()       resumable board
                     └─→ GameEffect ─────────────────→  haptics · sound · navigate
                           (Channel: fires exactly once)
```

`GameUiState` is fully derived — each square carries its own `hasQueen` / `isConflicting` /
`isAttacked`, so the composable renders and never computes.

## Architecture decisions

| Decision | Why                                                                                                                                                                                                    |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Single module, layered packages | The app is one feature. Packages mirror what modules would be, so extracting them later is a move, not a rewrite.                                                                                      |
| Android guide layering, **not** Clean Architecture | Ports in `domain` cost a second interface per repository, to buy substitutability that `data` already has by declaring the interface at all.                                                           |
| The game is a pure reducer | `GameSession.reduce(action): GameSession` — a rules test is one call and one assertion. New mechanics are a new `GameAction` branch.                                                                   |
| Conflicts are derived, never stored | Recomputed from the queens on every change, so there is no second source of truth to drift. Counting per line is O(n), not the naive pairwise O(n²).                                                   |
| The solver lives in **test** sources | Nothing in the app calls it. It is an independent oracle proving `BoardEvaluator` against a second implementation.                                                                                     |
| Use cases only where logic exists | "New best, −54s" earns a tested home outside the ViewModel; pass-through reads go ViewModel → repository directly.                                                                                     |
| Every solve in Room, bests derived in SQL | A history screen with dates and deltas is impossible from best-only rows. Settings and the resumable session go to DataStore — different shape, different lifetime.                                    |
| One-shot effects on a `Channel` | Feedback and navigation must fire once. A boolean in `UiState` replays on every configuration change.                                                                                                  |
| A design system package, tokens named for their role | Colour, type and spacing have one owner in `core/designsystem`, and nothing in it knows the game rules. Dynamic colour stays off: the board shades and the conflict accent are the product's identity. |
| Hand-written fakes, no mocking framework | They compile against the real interfaces, so changing one breaks its double at compile time.                                                                                                           |

## Testing

Deliberately bottom-heavy — the interesting logic is in pure functions, so most of the value
comes from fast JVM tests. **211 tests, 165 of which run without a device.**

| Scope | Tests |
|---|---|
| Models, rules, reducer, formatters | 66 |
| Use cases, repositories, serializer | 53 |
| ViewModels — state, effects, virtual-time clock | 46 |
| Compose UI, incl. forced window sizes | 45 |
| End-to-end over a real database and stores | 1 |

Doubles are hand-written and named after the interface they stand in for
(`TestSolveRepository`, `InMemoryDataStore`, `TestTimeProvider`) under
`app/src/test/kotlin/com/queens/puzzle/testing/`.

The known gap: the Compose and end-to-end tests need a device, so they cannot gate a build
without one. Robolectric is the fix, easy to add in the future.
