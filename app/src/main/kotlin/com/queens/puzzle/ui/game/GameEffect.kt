package com.queens.puzzle.ui.game

sealed interface GameEffect {

    data object HapticPlace : GameEffect

    data object HapticConflict : GameEffect

    /** A tap refused because every queen is already down. */
    data object BoardFull : GameEffect

    data object CelebrateWin : GameEffect

    data class NavigateToWin(val solveId: Long) : GameEffect
}
