package com.queens.puzzle.ui.home

import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.ThemePreference

data class HomeUiState(
    val sizes: List<BoardSize> = BoardSize.selectable,
    val selectedSize: BoardSize = BoardSize.Default,
    val puzzleType: PuzzleType = PuzzleType.Queens,
    val theme: ThemePreference = ThemePreference.System,
    val bestTimes: List<BestTime> = emptyList(),
    /** The stored board, when there is one to carry on with. */
    val resumable: ResumableGame? = null,
) {
    /** The stored board only counts as resumable while it matches the selected piece mode. */
    val canResume: Boolean get() = resumable != null && resumable.puzzleType == puzzleType
}

data class ResumableGame(
    val gameId: Long,
    val boardSize: BoardSize,
    val puzzleType: PuzzleType,
)
