package com.queens.puzzle.ui.game

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.ui.board.BoardSquareState

/**
 * Everything the game screen renders.
 *
 * Fully derived: the squares already carry their own flags, so the composable draws and never
 * computes.
 */
data class GameUiState(
    val boardSize: BoardSize = BoardSize.Default,
    val squares: List<BoardSquareState> = emptyList(),
    val queensPlaced: Int = 0,
    val elapsedMillis: Long = 0L,
    val conflictKinds: Set<ConflictKind> = emptySet(),
    val canUndo: Boolean = false,
    val isSolved: Boolean = false,
    val settings: GameSettings = GameSettings(),
    val isResetDialogVisible: Boolean = false,
    val isSettingsSheetVisible: Boolean = false,
) {
    val queensRemaining: Int get() = boardSize.value - queensPlaced

    val hasConflicts: Boolean get() = conflictKinds.isNotEmpty()

    /** A board with nothing placed has nothing to reset. */
    val canReset: Boolean get() = queensPlaced > 0 || canUndo
}
