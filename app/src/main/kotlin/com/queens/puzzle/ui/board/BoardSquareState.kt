package com.queens.puzzle.ui.board

import com.queens.puzzle.model.Position

/**
 * One square, fully derived.
 *
 * Everything the square draws is already decided here, so [BoardGrid] renders and never
 * computes — which is what lets the same grid appear on the game, win and history screens.
 */
data class BoardSquareState(
    val position: Position,
    val hasQueen: Boolean = false,
    val isConflicting: Boolean = false,
    val isAttacked: Boolean = false,
) {
    /** The alternating colour, from the square's own coordinates. */
    val isDarkSquare: Boolean get() = (position.row + position.column) % 2 == 1
}
