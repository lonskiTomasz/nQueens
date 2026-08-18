package com.queens.puzzle.model

/** Options that change how a game is played and read on the board. */
data class GameSettings(
    val showAttackLines: Boolean = true,
    val hapticsEnabled: Boolean = false,
)
