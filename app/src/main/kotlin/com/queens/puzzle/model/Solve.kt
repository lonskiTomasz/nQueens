package com.queens.puzzle.model

/** A completed game, as stored in the solve history. */
data class Solve(
    val id: Long,
    val boardSize: BoardSize,
    val durationMillis: Long,
    val taps: Int,
    val undos: Int,
    val completedAtMillis: Long,
)

/** The fastest recorded solve for one board size, with how many times that size was solved. */
data class BestTime(
    val boardSize: BoardSize,
    val bestMillis: Long,
    val solveCount: Int,
)

/**
 * The result of recording a solve.
 *
 * [improvementMillis] is positive when this solve beat the previous best, negative when it
 * was slower, and null when the size had no previous best.
 */
data class SolveOutcome(
    val solveId: Long,
    val isNewBest: Boolean,
    val improvementMillis: Long?,
)

/**
 * A solve alongside how it compares to the rest of its size: how many times that size has
 * been solved, and the fastest of the *other* solves — so a solve is never compared against
 * itself.
 */
data class SolveSizeSummary(
    val solve: Solve,
    val solveCount: Int,
    val bestMillisExcludingSelf: Long?,
)
