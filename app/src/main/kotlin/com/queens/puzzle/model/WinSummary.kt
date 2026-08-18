package com.queens.puzzle.model

/**
 * A finished solve, read back with everything the win screen states about it.
 *
 * [improvementMillis] is positive when the solve beat the previous best, negative when it was
 * slower, and null when the size had never been solved before.
 */
data class WinSummary(
    val solve: Solve,
    val isNewBest: Boolean,
    val improvementMillis: Long?,
    val solveCountForSize: Int,
)
