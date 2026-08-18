package com.queens.puzzle.model

/**
 * Everything derived from a set of queens: which of them are in conflict, why, which squares
 * they cover, and whether the board is solved.
 *
 * Produced by `BoardEvaluator`.
 */
data class BoardEvaluation(
    val conflicts: Set<Position> = emptySet(),
    val conflictKinds: Set<ConflictKind> = emptySet(),
    val attackedSquares: Set<Position> = emptySet(),
    val isSolved: Boolean = false,
) {
    val hasConflicts: Boolean get() = conflicts.isNotEmpty()

    fun isConflicting(position: Position): Boolean = position in conflicts

    fun isAttacked(position: Position): Boolean = position in attackedSquares
}
