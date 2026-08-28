package com.queens.puzzle.ui.home

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.core.designsystem.preview.PreviewState

fun previewHomeUiState(
    selectedSize: Int = 8,
    puzzleType: PuzzleType = PuzzleType.Queens,
    bestTimes: List<BestTime> = PreviewBestTimes,
    resumableSize: Int? = null,
): HomeUiState = HomeUiState(
    selectedSize = BoardSize(selectedSize),
    puzzleType = puzzleType,
    bestTimes = bestTimes,
    resumable = resumableSize?.let {
        ResumableGame(gameId = 1L, boardSize = BoardSize(it), puzzleType = puzzleType)
    },
)

val PreviewBestTimes: List<BestTime> = listOf(
    BestTime(BoardSize(12), PuzzleType.Queens, bestMillis = 483_000, solveCount = 1),
    BestTime(BoardSize(10), PuzzleType.Queens, bestMillis = 312_000, solveCount = 2),
    BestTime(BoardSize(8), PuzzleType.Queens, bestMillis = 161_000, solveCount = 12),
    BestTime(BoardSize(6), PuzzleType.Queens, bestMillis = 65_000, solveCount = 7),
    BestTime(BoardSize(4), PuzzleType.Queens, bestMillis = 12_000, solveCount = 4),
)

class HomeScreenPreviewProvider :
    CollectionPreviewParameterProvider<PreviewState<HomeUiState>>(
        listOf(
            PreviewState(previewHomeUiState(bestTimes = emptyList()), "first run, no history"),
            PreviewState(
                previewHomeUiState(selectedSize = 10, resumableSize = 6),
                "returning player, board to resume",
            ),
            PreviewState(
                previewHomeUiState(puzzleType = PuzzleType.Knights, bestTimes = emptyList()),
                "knights mode, no history for it yet",
            ),
        ),
    )
