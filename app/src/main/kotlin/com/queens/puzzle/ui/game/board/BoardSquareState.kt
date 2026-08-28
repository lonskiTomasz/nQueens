package com.queens.puzzle.ui.game.board

import com.queens.puzzle.model.Position

data class BoardSquareState(
    val position: Position,
    val hasPiece: Boolean = false,
    val isConflicting: Boolean = false,
    val isAttacked: Boolean = false,
) {
    /** The alternating colour, from the square's own coordinates. */
    val isDarkSquare: Boolean get() = (position.row + position.column) % 2 == 1
}
