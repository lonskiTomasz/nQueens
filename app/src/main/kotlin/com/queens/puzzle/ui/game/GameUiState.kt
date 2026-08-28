package com.queens.puzzle.ui.game

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.ui.game.board.BoardSquareState

data class GameUiState(
    val boardSize: BoardSize = BoardSize.Default,
    val puzzleType: PuzzleType = PuzzleType.Queens,
    val squares: List<BoardSquareState> = emptyList(),
    val piecesPlaced: Int = 0,
    val conflictKinds: Set<ConflictKind> = emptySet(),
    val canUndo: Boolean = false,
    val isSolved: Boolean = false,
    val isRestoring: Boolean = false,
    val settings: GameSettings = GameSettings(),
    val isResetDialogVisible: Boolean = false,
    val isSettingsSheetVisible: Boolean = false,
) {
    val piecesRemaining: Int get() = boardSize.value - piecesPlaced

    val hasConflicts: Boolean get() = conflictKinds.isNotEmpty()

    val canReset: Boolean get() = piecesPlaced > 0 || canUndo

    val isBoardEnabled: Boolean get() = !isRestoring && !isSolved
}
