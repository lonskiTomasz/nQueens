package com.queens.puzzle.ui.game

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.queens.puzzle.domain.rules.BoardEvaluator
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.ui.designsystem.preview.PreviewQueens
import com.queens.puzzle.ui.designsystem.preview.PreviewSolvedQueens
import com.queens.puzzle.ui.designsystem.preview.PreviewState
import com.queens.puzzle.ui.designsystem.preview.previewSquares

fun previewGameUiState(
    boardSize: Int = 8,
    queens: Set<Position> = PreviewQueens,
    elapsedMillis: Long = 134_000,
    settings: GameSettings = GameSettings(),
    isResetDialogVisible: Boolean = false,
    isSettingsSheetVisible: Boolean = false,
): GameUiState {
    val size = BoardSize(boardSize)
    val evaluation = BoardEvaluator.evaluate(size, queens)

    return GameUiState(
        boardSize = size,
        squares = previewSquares(size, queens, settings.showAttackLines),
        queensPlaced = queens.size,
        elapsedMillis = elapsedMillis,
        conflictKinds = evaluation.conflictKinds,
        canUndo = queens.isNotEmpty(),
        isSolved = evaluation.isSolved,
        settings = settings,
        isResetDialogVisible = isResetDialogVisible,
        isSettingsSheetVisible = isSettingsSheetVisible,
    )
}

class GameScreenPreviewProvider :
    CollectionPreviewParameterProvider<PreviewState<GameUiState>>(
        listOf(
            PreviewState(
                previewGameUiState(queens = emptySet(), elapsedMillis = 0),
                "fresh board, nothing placed",
            ),
            PreviewState(
                previewGameUiState(),
                "mid-game, two queens on a diagonal",
            ),
            PreviewState(
                previewGameUiState(settings = GameSettings(showAttackLines = false)),
                "attack lines switched off",
            ),
            PreviewState(
                previewGameUiState(
                    boardSize = 12,
                    queens = setOf(Position(0, 0), Position(2, 5), Position(7, 11)),
                ),
                "largest board, 12x12",
            ),
            PreviewState(
                previewGameUiState(boardSize = 4, queens = PreviewSolvedQueens, elapsedMillis = 12_000),
                "solved, smallest board",
            ),
        ),
    )
