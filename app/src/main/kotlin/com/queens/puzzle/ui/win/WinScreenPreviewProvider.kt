package com.queens.puzzle.ui.win

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.queens.puzzle.model.WinSummary
import com.queens.puzzle.ui.designsystem.preview.PreviewState
import com.queens.puzzle.ui.designsystem.preview.previewSolve

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

class WinScreenPreviewProvider :
    CollectionPreviewParameterProvider<PreviewState<WinUiState>>(
        listOf(
            PreviewState(WinUiState.Solved(previewWinSummary()), "new best, improvement spelled out"),
            PreviewState(
                WinUiState.Solved(previewWinSummary(improvementMillis = null, solveCountForSize = 1)),
                "first solve of a size, nothing to have beaten",
            ),
            PreviewState(
                WinUiState.Solved(previewWinSummary(isNewBest = false, improvementMillis = -54_000)),
                "slower than best",
            ),
            PreviewState(WinUiState.Missing, "solve missing, history cleared mid-screen"),
        ),
    )
