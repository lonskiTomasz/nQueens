package com.queens.puzzle.ui.besttimes

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.queens.puzzle.common.time.RelativeDay
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.ui.designsystem.preview.PreviewState
import com.queens.puzzle.ui.designsystem.preview.previewSolve

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

class BestTimesScreenPreviewProvider :
    CollectionPreviewParameterProvider<PreviewState<BestTimesUiState>>(
        listOf(
            PreviewState(previewBestTimesUiState(), "full history, every date bucket"),
            PreviewState(
                previewBestTimesUiState(
                    selectedFilter = 8,
                    rows = PreviewSolveRows.filter { it.solve.boardSize.value == 8 },
                ),
                "filtered to one size",
            ),
            PreviewState(BestTimesUiState(), "empty, nothing solved yet"),
        ),
    )
