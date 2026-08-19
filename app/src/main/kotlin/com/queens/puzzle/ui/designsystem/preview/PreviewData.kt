package com.queens.puzzle.ui.designsystem.preview

import com.queens.puzzle.common.time.RelativeDay
import com.queens.puzzle.domain.rules.BoardEvaluator
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.WinSummary
import com.queens.puzzle.ui.besttimes.BestTimesUiState
import com.queens.puzzle.ui.besttimes.SolveRow
import com.queens.puzzle.ui.board.BoardSquareState
import com.queens.puzzle.ui.game.GameUiState
import com.queens.puzzle.ui.home.HomeUiState

val PreviewQueens: Set<Position> = setOf(
    Position(0, 0),
    Position(1, 4),
    Position(2, 7),
    Position(3, 5),
    Position(4, 6),
)

val PreviewSolvedQueens: Set<Position> = setOf(
    Position(0, 1),
    Position(1, 3),
    Position(2, 0),
    Position(3, 2),
)

fun previewSquares(
    boardSize: BoardSize,
    queens: Set<Position>,
    showAttackLines: Boolean = true,
): List<BoardSquareState> {
    val evaluation = BoardEvaluator.evaluate(boardSize, queens)

    return boardSize.positions().map { position ->
        BoardSquareState(
            position = position,
            hasQueen = position in queens,
            isConflicting = evaluation.isConflicting(position),
            isAttacked = showAttackLines && evaluation.isAttacked(position),
        )
    }
}

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

fun previewHomeUiState(
    selectedSize: Int = 8,
    bestTimes: List<BestTime> = PreviewBestTimes,
    resumableSize: Int? = null,
): HomeUiState = HomeUiState(
    selectedSize = BoardSize(selectedSize),
    bestTimes = bestTimes,
    resumableSize = resumableSize?.let(::BoardSize),
)

val PreviewBestTimes: List<BestTime> = listOf(
    BestTime(BoardSize(12), bestMillis = 483_000, solveCount = 1),
    BestTime(BoardSize(10), bestMillis = 312_000, solveCount = 2),
    BestTime(BoardSize(8), bestMillis = 161_000, solveCount = 12),
    BestTime(BoardSize(6), bestMillis = 65_000, solveCount = 7),
    BestTime(BoardSize(4), bestMillis = 12_000, solveCount = 4),
)

fun previewSolve(
    id: Long = 1L,
    boardSize: Int = 8,
    durationMillis: Long = 107_000,
    taps: Int = 27,
    undos: Int = 2,
): Solve = Solve(
    id = id,
    boardSize = BoardSize(boardSize),
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = 1_787_054_400_000L,
)

fun previewWinSummary(
    isNewBest: Boolean = true,
    improvementMillis: Long? = 54_000,
    solveCountForSize: Int = 12,
): WinSummary = WinSummary(
    solve = previewSolve(),
    isNewBest = isNewBest,
    improvementMillis = improvementMillis,
    solveCountForSize = solveCountForSize,
)

fun previewBestTimesUiState(
    selectedFilter: Int? = null,
    rows: List<SolveRow> = PreviewSolveRows,
): BestTimesUiState = BestTimesUiState(
    filters = listOf(4, 5, 6, 8, 10, 12).map(::BoardSize),
    selectedFilter = selectedFilter?.let(::BoardSize),
    rows = rows,
)

val PreviewSolveRows: List<SolveRow> = listOf(
    SolveRow(
        solve = previewSolve(id = 1, boardSize = 8, durationMillis = 161_000),
        isBestForSize = true,
        deltaMillis = null,
        occurred = RelativeDay.Today,
    ),
    SolveRow(
        solve = previewSolve(id = 2, boardSize = 6, durationMillis = 65_000),
        isBestForSize = false,
        deltaMillis = -3_000,
        occurred = RelativeDay.Yesterday,
    ),
    SolveRow(
        solve = previewSolve(id = 3, boardSize = 5, durationMillis = 34_000),
        isBestForSize = false,
        deltaMillis = -11_000,
        occurred = RelativeDay.DaysAgo(2),
    ),
    SolveRow(
        solve = previewSolve(id = 4, boardSize = 4, durationMillis = 12_000),
        isBestForSize = true,
        deltaMillis = null,
        occurred = RelativeDay.LastWeek,
    ),
    SolveRow(
        solve = previewSolve(id = 5, boardSize = 10, durationMillis = 312_000),
        isBestForSize = false,
        deltaMillis = -80_000,
        occurred = RelativeDay.WeeksAgo(2),
    ),
)
