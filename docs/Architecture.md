# Architecture

N-Queens puzzle game for Android. Kotlin, Jetpack Compose, Material 3.

This document is the plan of record: it describes the structure, the reasoning behind
each choice, and what was deliberately left out.

---

## 1. What the app does

A single-player puzzle. The player places `n` queens on an `n × n` board so that no two
share a row, column, or diagonal, against a running clock.

| Area       | Feature                                                                                       |
|------------|-----------------------------------------------------------------------------------------------|
| Board      | Seven sizes between 4 and 12 (no solution exists below 4); tap a square to place or remove    |
| Validation | Conflicts marked live on the squares at fault, with a banner while any remain                 |
| Progress   | Queens-left counter, elapsed timer                                                            |
| Actions    | Undo, reset (confirmed)                                                                       |
| Options    | Crossing out attacked squares, haptics, and sound — in a settings sheet                       |
| Appearance | Follows the system scheme until the player picks light or dark, then stores that (§7)         |
| Continuity | An unfinished board is resumable after leaving the app                                        |
| Completion | Win screen: the time, the delta against the previous best, per-solve stats, the next size up  |
| History    | Every solve stored; best time per board size                                                  |

**Engineering goals**

| Goal                                 | How it is met                                                              |
|--------------------------------------|----------------------------------------------------------------------------|
| Game logic provable without a device | `model` and `domain` carry no Android imports, so the rules run on the JVM |
| UI is a function of state            | Unidirectional data flow; ViewModels expose immutable data                 |
| Cheap to extend                      | Game rules are a pure state machine; a new rule or action touches one file |
| Strong coverage where it matters     | ~100% on rules/solver/reducer; ViewModel state transitions; UI smoke tests |
| Readable in one sitting              | Single Gradle module, strict package layering, no framework ceremony       |

**Non-goals:** multiplayer, accounts, cloud sync, i18n beyond English string extraction.

---

## 2. Layering

The app follows the layering of the [Android architecture guide][guide]: three layers in one
Gradle module, with dependencies pointing one way, downwards:

```
   ui  ───→  domain  ───→  data  ───→  model
                  └──────────┘
              (both may skip a layer)
```

* `model` — the shared vocabulary. Plain data classes, no dependencies at all.
* `data` — repositories: the interface and its implementation together, plus the Room and
  DataStore sources behind them. The single source of truth for anything that outlives a
  screen.
* `domain` — the rules of the game and the use cases that combine repositories. Optional by
  the guide's own framing, and used here only where it earns its place (§4.4).
* `ui` — Compose screens and ViewModels. ViewModels read repositories directly for
  pass-through work, and go through a use case where there is real logic to hold.

`core` sits outside that stack: the design system and the formatting helpers, imported by `ui`
and depending only on `model`. It is where the multi-module version of this app would put
`core:designsystem` and `core:util` (decision log row 1).

Wiring is Hilt: constructor injection throughout, with `@Binds` modules in `data/di` binding each
repository interface to its implementation. The instrumented tests run against the real graph
rather than substituting doubles into it.

Two consequences worth stating, because they are the ones people expect to be otherwise:

**Repository interfaces live in `data`, beside their implementations** — not in `domain`.
Callers still depend on the interface, so Room stays swappable and tests substitute an
in-memory implementation, but there is no separate port layer mirroring every repository.

**`ui` may import `data`.** A ViewModel injecting `SolveRepository` is the normal case, not a
leak. What it must not do is reach past the repository to a DAO or a DataStore.

This is deliberately *not* Clean Architecture, and the difference is worth naming because the
two are often conflated. The dependency-inversion variant — ports in the domain, adapters in
data, the domain depending on nothing — is coherent, but it costs a second interface per
repository to buy a substitutability that `data` already provides by declaring the interface at
all. Matching the platform guidance the rest of the Android ecosystem is written against is
worth more here.

`model` stays free of Android types because every other layer imports it, and `domain` happens
to be Android-free too, which is why the rules and the reducer test on the JVM in milliseconds.
Neither is enforced by a build gate.

[guide]: https://developer.android.com/topic/architecture

---

## 3. Package layout

Root package `com.queens.puzzle`. Sources are under `src/main/kotlin`.

```
com.queens.puzzle
├── NQueensApplication.kt
│
├── model/                        ← shared vocabulary. Depends on nothing.
│   ├── BoardSize.kt              value class, validates 4..12; owns the selectable ladder
│   ├── Position.kt               row/column, plus its two diagonal identities
│   ├── Move.kt                   Place | Remove — the undo stack element
│   ├── GameSession.kt            immutable game state
│   ├── BoardEvaluation.kt        derived: conflicts, attacked squares, solved
│   ├── ConflictKind.kt           Row | Column | Diagonal — evaluator output
│   ├── Solve.kt                  a completed solve + BestTime, SolveOutcome, SolveSizeSummary
│   ├── WinSummary.kt             a solve plus its new-best verdict and delta
│   ├── GameSettings.kt           attack lines, haptics, sound — options that affect play
│   ├── AppSettings.kt            theme, last board size — app-wide preferences
│   └── ThemePreference.kt        System | Light | Dark
│
├── core/                         ← design system and formatting. Imported by ui; sees only model.
│   ├── util/time/                DurationFormatter ("02:14", "-54s"), RelativeDayCalculator
│   └── designsystem/
│       ├── theme/                Color, Type, Spacing, Dimens, Theme (QueensTheme)
│       ├── component/            QueenGlyph, AttackGlyph, AlertBadge, SizeChip,
│       │                         PiecePips, TimerChip, ThemeToggle
│       └── preview/              themed preview wrapper + shared sample state
│
├── domain/
│   ├── rules/QueenRules.kt       line-counting conflict + attack computation
│   ├── game/                     GameAction (sealed intents), GameReducer
│   │                             pure (GameSession, GameAction) -> GameSession
│   └── usecase/
│       ├── RecordSolveUseCase.kt        insert + compute new-best and delta
│       ├── GetWinSummaryUseCase.kt      rebuild the win comparison from a solve id
│       └── ObserveBestTimesUseCase.kt   orders the per-size bests for the home card
│
├── data/
│   ├── repository/               interface + implementation, side by side
│   │                             Solve / GameSettings / AppSettings / Session
│   ├── local/
│   │   ├── database/             AppDatabase, SolveEntity, SolveDao, BestTimeRow,
│   │   │                         SolveWithSizeSummary (the win-screen projection)
│   │   └── datastore/            SettingsDataSource, SessionDataSource,
│   │                             SavedSession, SessionSerializer
│   ├── mapper/                   SolveMapper, SessionMapper — entity <-> model
│   ├── util/                     TimeProvider + SystemTimeProvider —
│   │                             wall-clock and monotonic time, injected
│   └── di/                       DatabaseModule, DataStoreModule, RepositoryModule,
│                                 DispatchersModule, Dispatcher (the qualifier)
│
└── ui/
    ├── MainActivity.kt
    ├── MainViewModel.kt          resolves the stored theme before the first frame
    ├── navigation/               NavKeys.kt, QueensNavHost.kt
    ├── home/                     HomeScreen, HomeViewModel, HomeUiState
    ├── game/                     GameScreen, GameViewModel, GameUiState, GameEffect,
    │   │                         GameSettingsSheet, ResetConfirmDialog
    │   ├── board/                BoardGrid, BoardSquare, BoardSquareState — the board widget
    │   └── feedback/             GameFeedback (haptics) and GameSound (SoundPool),
    │                             each an interface with a silent no-op implementation
    ├── win/                      WinScreen, WinViewModel, WinUiState
    └── besttimes/                BestTimesScreen, BestTimesViewModel, BestTimesUiState
```

Each screen also carries a `*PreviewProvider.kt` holding the states its previews render.

Test doubles live under `src/test/kotlin/com/queens/puzzle/testing/`, named after the interface
they stand in for and grouped the way the production code is.

---

## 4. Domain design

### 4.1 The game as a pure state machine

`GameSession` holds the board size, the pieces placed so far, the undo stack and the tap and
undo counters.
`GameAction` is three intents — `TapSquare`, `Undo`, `Reset` — and `reduce` maps one session to
the next: pure, total, free of I/O, a refused action returning the receiver unchanged. So a rules
test is one call and one assertion, and a new mechanic is a new branch the UI picks up unchanged.

`TapSquare` toggles place/remove and refuses to place once `n` pieces are down, so
`piecesRemaining` cannot go negative; the ViewModel turns the refusal into feedback.

### 4.2 Evaluation is derived, never stored

`QueenRules.evaluate` returns the conflicting positions, the kinds of conflict, the attacked
squares and whether the board is solved — recomputed from the pieces on every change, so there is
no second source of truth to drift. Counting occupancy per row, column and both diagonals makes
the conflict pass O(n) rather than a pairwise O(n²). Covering the attacked squares is the O(n²)
half, so it sits behind a flag and is paid for only when the overlay is on.

### 4.3 Solver — a test oracle, not production code

`NQueensSolver` (backtracking with column and diagonal bitmasks) has no production caller — no
hint, no auto-solve — so it lives in test sources rather than `domain/rules/`. It generates real
solutions, which lets the rules tests check `QueenRules` against a second implementation of
the problem instead of against fixtures that could encode the same misunderstanding twice.
Promoting it is a file move if a hint ever lands.

### 4.4 Use cases

Only where the logic belongs to neither the ViewModel nor a repository.

Pass-through calls go straight from ViewModel to repository: a use case that forwards one call
adds a file, an indirection and a test, and buys nothing.

---

## 5. Data design

### 5.1 Room — solve history

One table, `solves`: `board_size`, `duration_millis`, `taps`, `undos`,
`completed_at`, with a composite index on `(board_size, duration_millis)` — the pair every
best-time query orders by. Every solve is stored rather than a best per size, because a history
with dates and deltas cannot be rebuilt from best-only rows; bests are a query. `SolveDao`
aggregates in SQL, including the win screen's projection — one solve plus its size's count and
best-excluding-itself — in a single statement. Schemas are exported to `app/schemas` and committed,
so migrations are reviewable.

### 5.2 Preferences DataStore — settings

One store behind two interfaces: `GameSettingsRepository`
(attack lines, haptics, sound) and `AppSettingsRepository` (theme, last board size). The split is
in what each caller can reach, not in the storage.

### 5.3 Typed DataStore — resumable session

`SavedSession(gameId, boardSize, pieces, moves, taps,
undos, elapsedMillis)`, serialized with kotlinx-serialization: written on each committed move —
conflated, so a tap burst is one write — and cleared on solve or reset. Its own shape rather than
the `model` types, which keeps `model` dependency-free and puts a format change in one file and its
mapper. `moves` is the undo stack, without which a resumed board comes back unable to undo.
`gameId` is what the game screen matches against its nav key to decide whether the stored board is
*this* game (§6.2).

---

## 6. Presentation design

### 6.1 Unidirectional data flow

One ViewModel per screen: a `StateFlow` of an immutable `UiState`
and one way in — the game screen takes the sealed `GameAction` the reducer already speaks, the
quieter screens take lambdas. `GameUiState` is fully derived, each square carrying its own
`hasPiece`, `isConflicting` and `isAttacked`, so the composable renders and never computes.
One-shot signals — feedback, navigation — go over a `Channel`, because a boolean in state replays
on every configuration change.

### 6.2 Navigation

Navigation 3, with four `@Serializable` keys: `HomeKey`, `GameKey(boardSize,
gameId)`, `WinKey(solveId)` and `BestTimesKey`. `gameId` is an identity, not an instruction: the
back stack is restored after process death, so a key reading "start fresh" would be obeyed a second
time and wipe the board it should have resumed, whereas "does the stored board belong to this
game?" reads the same on every construction. Arguments reach their ViewModel as typed constructor
parameters through assisted injection. The reset dialog and the settings sheet are
transient UI inside `GameScreen`, not destinations.

### 6.3 Timer

`TimeProvider` is injected, so tests run the clock in virtual time. Elapsed time is
*banked* — `accumulatedMillis` plus a nullable `runningSince`, stopped on `ON_STOP` — because one
subtraction against a monotonic clock keeps accruing in the background, and the banked total
survives a process killed there. The tick is its own `StateFlow<Long>`, read through a `() -> Long`
so it does not recompose the board behind it.

### 6.4 Haptics and sound

`GameFeedback` (haptics) and `GameSound` (a `SoundPool`) are interfaces resolved by a
`remember*(enabled)` helper to the real implementation or a silent no-op, so the player's toggle is
honoured at construction rather than at every call site. The ViewModel only emits a `GameEffect`
and `GameScreen` turns it into a buzz or a click, which keeps the unit tests off Android APIs.

---

## 7. Design system

The palette maps to Material 3 roles almost one-to-one:

| Role                          | M3 slot                  | Light                 | Dark                  |
|-------------------------------|--------------------------|-----------------------|-----------------------|
| Primary (app bar, buttons)    | `primary`                | `#2E7D32`             | `#7BC96C`             |
| Celebration (win accent)      | `tertiary`               | `#C9A227`             | `#E3C158`             |
| Conflict (warning, not alarm) | `error`                  | `#E08E45`             | `#F0A868`             |
| Background / Surface          | `background` / `surface` | `#FAF9F6` / `#FFFFFF` | `#171614` / `#1F1E1B` |

What has no M3 slot — success green, the two board shades and the queen and attack marks that sit
on them, the conflict tint, the win screen's headline and gradient — is carried in
`QueensExtendedColors` through a `staticCompositionLocalOf`. The board keeps the same
colours in both schemes. Typography is serif for display and title, sans for body and label, and
monospace for times so digits do not shuffle as they tick.

Dynamic colour is **off** — the board and its conflict and celebration accents are the product's
identity, and Material You would repaint exactly the semantics the player reads. Light and dark
follow the system until the player uses the toggle, which stores an explicit choice from then on.

Spacing and dimensions are tokens in `theme/Spacing.kt` and `theme/Dimens.kt`, each named for the
relationship it expresses rather than its size — `LabelGap`, `PrimaryButtonHeight`. A
value one composable owns stays with it; corner radii are left to `MaterialTheme.shapes`.

**Squares a queen covers are crossed out, not shaded.** A scrim has to read over both square shades
at once, which caps it around 8% black — too faint to connect to the queen just placed.

`BoardGrid` takes square states and a click lambda, with no knowledge of the game, so it previews
at every size and is reused on the win screen. Every screen has a stateless overload taking
`UiState` plus lambdas, and it is that overload the previews and the Compose tests drive; board
fixtures run through the real `QueenRules`, so a preview cannot quietly disagree with the rules
it illustrates.

*Not yet done:* squares are sized by the grid alone, so on a 10×10 or 12×12 board the touch target
falls below `Dimens.MinTouchTarget` (48 dp) — which the size chips, the theme toggle and the
top-bar icon buttons all honour, leaving the board the one tappable thing that does not.

---

## 8. Screens

| Screen               | Contents                                                                                     |
|----------------------|----------------------------------------------------------------------------------------------|
| `HomeScreen`         | Size chips, light/dark toggle, start, resume-if-present, best-times card                     |
| `GameScreen`         | Board, queens-left pips, timer, conflict banner, undo/reset, gear opening the settings sheet |
| `GameSettingsSheet`  | Attack-mark, haptics and sound toggles; a bottom sheet inside `GameScreen`                   |
| `WinScreen`          | Time, new-best delta, per-solve stats, the next board size up, see-best-times, close-to-home |
| `ResetConfirmDialog` | Confirmation for the destructive reset; a dialog inside `GameScreen`                         |
| `BestTimesScreen`    | History with filters, per-row mini board, delta vs. best                                     |

---

## 9. Testing strategy

The pyramid is deliberately bottom-heavy: the interesting logic lives in pure functions,
so most of the value comes from fast JVM tests.

| Scope                             | Tests                                                                                                                         | Runner                       |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------|------------------------------|
| `QueenRules`                      | conflicts by row, column and both diagonals; solved detection; attacked squares on and off                                    | JUnit, JVM                   |
| `NQueensSolver` (test source)     | solution counts pinned to OEIS A000170, so a broken oracle cannot bless a broken evaluator; every solution confirmed solved, and not solved once a queen moves | JUnit, JVM                   |
| `GameReducer`                     | place/remove, undo unwinds one move, undo past empty is a no-op, reset, refusal at `n` queens, counters                       | JUnit, JVM                   |
| `model`, `core/util/time`         | `BoardSize` range and ladder, `Position` diagonals; duration and relative-day formatting                                      | JUnit, JVM                   |
| Use cases, repositories           | new-best and delta, no-previous-best, never compared against itself; mapping and aggregation over doubles                     | JUnit, JVM                   |
| `SessionSerializer`               | round trip; a corrupt or empty store reads back as no session rather than throwing                                            | JUnit, JVM                   |
| ViewModels                        | tap → state, conflict messaging, solving records and emits `NavigateToWin`, the clock in virtual time, a tap burst → one write, settings and session round-trips | JUnit + `coroutines-test`    |
| DAO                               | the win screen's aggregation: excluded from its own best, counted in its own total, ties; the rest covered by the end-to-end test | instrumented, in-memory Room |
| Compose                           | placing and marking queens, the counter, settings sheet, reset dialog, win screen, home selection, history rows, `BoardGrid`; `GameScreen` at four forced window sizes | instrumented                 |
| End-to-end                        | solve a board from launch to win screen over the real database and stores                                                     | instrumented                 |

Test doubles are hand-written, named after the interface they stand in for and kept under
`src/test/kotlin/.../testing/`. They compile against the real interfaces, so changing one breaks
its double at compile time rather than leaving a stub quietly returning null.

---

## 10. Decision log

| #  | Decision                                                                                | Alternatives considered                                                                                                            | Why                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|----|-----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1  | Single Gradle module, layered packages                                                  | Full multi-module (`core:*` / `feature:*`); app/core/feature split                                                                 | The codebase is one feature. Modules would add a dozen build files and a `build-logic` project to police a boundary that convention holds at this size. Packages mirror what the modules would be, so extraction later is a move, not a rewrite.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 2  | Layering per the Android architecture guide                                             | Clean Architecture / ports and adapters: repository interfaces in `domain`, dependencies inverted                                  | The inversion costs a second interface per repository to buy substitutability that `data` already has by declaring the interface at all. Following the platform guidance keeps the code legible to anyone who already knows Android.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| 3  | Game state as a pure reducer                                                            | Mutable engine class; logic in the ViewModel                                                                                       | Testable without coroutines or mocks, trivially serializable for resume, and the obvious place to extend.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 4  | Line-counting evaluation                                                                | Naive O(n²) pairwise                                                                                                               | Same code size, O(n) for the conflict pass, and the conflict *kind* falls out of it — which the banner used to name. The banner now says one thing whatever the queens share, so `ConflictKind` is currently produced and not displayed.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 5  | Store every solve, derive bests in SQL                                                  | One row per board size holding the best                                                                                            | The history screen with dates and deltas is impossible from best-only rows. Bests are a query.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| 6  | Use cases only where logic exists                                                       | One per repository method; none at all                                                                                             | Avoids both extremes: no pass-through file-per-call ceremony, but new-best/delta logic gets a tested home outside the ViewModel.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 7  | One-shot effects on a `Channel`                                                         | Booleans in `UiState` with consumed-flags                                                                                          | Feedback and navigation must fire once. State-based signalling replays on every configuration change and needs manual clearing.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| 8  | Assisted-injected ViewModels for nav args                                               | `SavedStateHandle` lookups                                                                                                         | Arguments become typed constructor parameters; the compiler catches a missing or misspelled one.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 9  | Extended colours via `CompositionLocal`                                                 | Reuse unrelated M3 roles; hardcode                                                                                                 | Success, the board squares and what sits on them have no M3 slot. Overloading e.g. `surfaceTint` to carry them makes both harder to change.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 10 | Dynamic colour off                                                                      | Material You enabled                                                                                                               | The board and its conflict/celebration accents are the product's identity; wallpaper-derived colour would repaint exactly the semantics the player reads.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 11 | Hand-written fakes                                                                      | MockK / Mockito                                                                                                                    | Fakes compile against the real interfaces, read like the production code, and do not encode call-order assumptions that break on refactor.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 12 | Theme follows the system scheme by default; the toggle pins light or dark               | Store `Light` or `Dark` only, seeded from the system at first launch; a three-way System / Light / Dark control                    | Nothing keeps a stored seed in step with the device afterwards, so a player who never opens the toggle stays pinned to whatever the system happened to be on first launch. Resolving `System` against `isSystemInDarkTheme()` where the preference is read costs no extra control: the toggle still has two segments and writes an explicit `Light` or `Dark`, so using it pins the scheme and leaving it alone follows the device.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| 13 | No architecture-enforcement test                                                        | A source-scanning JUnit test; Konsist; a custom lint rule                                                                          | Layer boundaries here are a convention held by review. The multi-module version of this app would get the boundary from the compiler for free; a test that restates it in one module mostly restates the package tree. Konsist or a lint rule is the better tool if this ever needs teeth.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 14 | Models in a top-level `model` package, not inside `domain`                              | `domain/model/`; duplicate models per layer                                                                                        | `data` needs the same `Solve` and `BoardSize` the rules use. Keeping models below both layers avoids `data` depending on `domain` and `domain` depending on `data` at once.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 15 | Gameplay options and app-wide preferences are separate models and separate repositories | One `Settings` model behind one repository                                                                                         | The theme is not a property of a game: it is read before any board exists, by the root composable on the first frame, while attack lines and haptics matter only once a game is on screen. Merging them gives every caller reach over settings it has no business writing, and forces a test double to stub methods the test never calls.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 16 | Spacing and dimensions as role-named tokens                                             | A numeric t-shirt ramp (`Spacing.Medium`); raw `.dp` literals at call sites; `MaterialTheme` extension via `CompositionLocal`      | M3 has no spacing slot to reuse, so it had to be ours. A ramp dense enough for the 2 dp granularity these values actually use (3, 6, 10, 14, 18 all appear) enforces nothing and reads worse than the number it hides, whereas a role name says why the gap exists. Spacing does not vary by theme or window, so a `CompositionLocal` would buy ambient lookup nothing and cost every call site a composable context.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
