package com.queens.puzzle.ui.game

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.queens.puzzle.domain.rules.KnightRules
import com.queens.puzzle.domain.rules.QueenRules
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.core.designsystem.preview.PreviewState
import com.queens.puzzle.ui.game.board.PreviewQueens
import com.queens.puzzle.ui.game.board.PreviewSolvedQueens
import com.queens.puzzle.ui.game.board.previewSquares

fun previewGameUiState(
    boardSize: Int = 8,
    pieces: Set<Position> = PreviewQueens,
    puzzleType: PuzzleType = PuzzleType.Queens,
    settings: GameSettings = GameSettings(),
    isResetDialogVisible: Boolean = false,
    isSettingsSheetVisible: Boolean = false,
): GameUiState {
    val size = BoardSize(boardSize)
    val rules = when (puzzleType) {
        PuzzleType.Queens -> QueenRules
        PuzzleType.Knights -> KnightRules
    }
    val evaluation = rules.evaluate(size, pieces, includeAttackedSquares = false)

    return GameUiState(
        boardSize = size,
        puzzleType = puzzleType,
        squares = previewSquares(size, pieces, settings.showAttackLines),
        piecesPlaced = pieces.size,
        conflictKinds = evaluation.conflictKinds,
        canUndo = pieces.isNotEmpty(),
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
                previewGameUiState(pieces = emptySet()),
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
                    pieces = setOf(Position(0, 0), Position(2, 5), Position(7, 11)),
                ),
                "largest board, 12x12",
            ),
            PreviewState(
                previewGameUiState(boardSize = 4, pieces = PreviewSolvedQueens),
                "solved, smallest board",
            ),
            PreviewState(
                previewGameUiState(
                    boardSize = 6,
                    pieces = setOf(Position(0, 0), Position(1, 2), Position(3, 3)),
                    puzzleType = PuzzleType.Knights,
                ),
                "knights, two of them a knight's move apart",
            ),
        ),
    )
