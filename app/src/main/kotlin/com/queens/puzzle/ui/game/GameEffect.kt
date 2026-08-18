package com.queens.puzzle.ui.game

/**
 * One-shot signals.
 *
 * These ride a channel rather than sitting in state: feedback and navigation must fire exactly
 * once, and a boolean in state fires again on every configuration change (§12, row 7).
 */
sealed interface GameEffect {

    data object HapticPlace : GameEffect

    data object HapticConflict : GameEffect

    /** A tap refused because every queen is already down. */
    data object BoardFull : GameEffect

    data object CelebrateWin : GameEffect

    data class NavigateToWin(val solveId: Long) : GameEffect
}
