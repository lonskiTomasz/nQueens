package com.queens.puzzle.domain.game

import com.queens.puzzle.model.Position

sealed interface GameAction {

    /** Toggles a square: occupied removes, empty places. */
    data class TapSquare(val position: Position) : GameAction

    data object Undo : GameAction

    data object Reset : GameAction
}
