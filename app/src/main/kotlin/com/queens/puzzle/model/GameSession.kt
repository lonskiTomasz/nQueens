package com.queens.puzzle.model

/**
 * The complete state of one game in progress.
 *
 * Holds only what the player did. Conflicts and solved-ness are derived from [queens] by
 * `BoardEvaluator`, and elapsed time is tracked outside the session.
 */
data class GameSession(
    val boardSize: BoardSize,
    val queens: Set<Position> = emptySet(),
    val moves: List<Move> = emptyList(),
    val taps: Int = 0,
    val undos: Int = 0,
) {
    init {
        require(queens.all { it in boardSize }) { "Queen outside $boardSize board" }
        require(queens.size <= boardSize.value) { "More queens than the board allows" }
    }

    /** Queens still to be placed. Never negative; the reducer refuses the (n + 1)th placement. */
    val queensRemaining: Int get() = boardSize.value - queens.size

    val canUndo: Boolean get() = moves.isNotEmpty()

    /** True while the board is untouched: no queens placed and nothing on the undo stack. */
    val isPristine: Boolean get() = queens.isEmpty() && moves.isEmpty()

    fun hasQueenAt(position: Position): Boolean = position in queens
}
