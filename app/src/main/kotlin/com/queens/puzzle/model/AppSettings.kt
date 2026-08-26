package com.queens.puzzle.model

/**
 * [lastBoardSize] is the size the home screen preselects.
 */
data class AppSettings(
    val theme: ThemePreference = ThemePreference.System,
    val lastBoardSize: BoardSize = BoardSize.Default,
)
