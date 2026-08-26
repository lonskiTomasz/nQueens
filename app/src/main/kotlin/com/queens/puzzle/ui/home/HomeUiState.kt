package com.queens.puzzle.ui.home

import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference

data class HomeUiState(
    val sizes: List<BoardSize> = BoardSize.selectable,
    val selectedSize: BoardSize = BoardSize.Default,
    val theme: ThemePreference = ThemePreference.System,
    val bestTimes: List<BestTime> = emptyList(),
    /** The stored board, when there is one to carry on with. */
    val resumable: ResumableGame? = null,
) {
    val canResume: Boolean get() = resumable != null
}

data class ResumableGame(
    val gameId: Long,
    val boardSize: BoardSize,
)
