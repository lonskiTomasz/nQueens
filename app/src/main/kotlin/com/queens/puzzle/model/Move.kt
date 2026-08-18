package com.queens.puzzle.model

/**
 * One entry on the undo stack.
 *
 * Records what happened rather than the state before it, so undoing inverts the move.
 */
sealed interface Move {

    val position: Position

    data class Place(override val position: Position) : Move

    data class Remove(override val position: Position) : Move
}
