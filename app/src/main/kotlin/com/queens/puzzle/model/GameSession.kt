package com.queens.puzzle.model

/**
 * The complete state of one game in progress.
 *
 * Holds only what the player did. Conflicts and solved-ness are derived from [pieces] by
 * `QueenRules`, and elapsed time is tracked outside the session.
 */
data class GameSession(
    val boardSize: BoardSize,
    val pieces: Set<Position> = emptySet(),
    val moves: List<Move> = emptyList(),
    val taps: Int = 0,
    val undos: Int = 0,
) {
    init {
        require(pieces.all { it in boardSize }) { "Piece outside $boardSize board" }
        require(pieces.size <= boardSize.value) { "More pieces than the board allows" }
    }

    /** Pieces still to be placed. Never negative; the reducer refuses the (n + 1)th placement. */
    val piecesRemaining: Int get() = boardSize.value - pieces.size

    val canUndo: Boolean get() = moves.isNotEmpty()

    /** True while the board is untouched: no pieces placed and nothing on the undo stack. */
    val isPristine: Boolean get() = pieces.isEmpty() && moves.isEmpty()

    fun hasPieceAt(position: Position): Boolean = position in pieces
}
