package com.queens.puzzle.model

/**
 * Preferences that apply to the whole app, independent of any game.
 *
 * [lastBoardSize] is the size the home screen preselects.
 */
data class AppSettings(
    val theme: ThemePreference = ThemePreference.Light,
    val lastBoardSize: BoardSize = BoardSize.Default,
)
