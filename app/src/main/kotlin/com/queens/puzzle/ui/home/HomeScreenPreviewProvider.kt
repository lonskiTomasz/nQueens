package com.queens.puzzle.ui.home

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.core.designsystem.preview.PreviewState

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

class HomeScreenPreviewProvider :
    CollectionPreviewParameterProvider<PreviewState<HomeUiState>>(
        listOf(
            PreviewState(previewHomeUiState(bestTimes = emptyList()), "first run, no history"),
            PreviewState(
                previewHomeUiState(selectedSize = 10, resumableSize = 6),
                "returning player, board to resume",
            ),
        ),
    )
