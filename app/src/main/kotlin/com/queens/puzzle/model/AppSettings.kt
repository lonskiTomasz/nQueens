package com.queens.puzzle.model

/**
 * [lastBoardSize] and [lastPuzzleType] are what the home screen preselects.
 */
data class AppSettings(
    val theme: ThemePreference = ThemePreference.System,
    val lastBoardSize: BoardSize = BoardSize.Default,
    val lastPuzzleType: PuzzleType = PuzzleType.Queens,
)
