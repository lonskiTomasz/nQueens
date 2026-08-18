package com.queens.puzzle.ui.win

import com.queens.puzzle.model.WinSummary

/** Everything the win screen renders. */
sealed interface WinUiState {

    data object Loading : WinUiState

    /** The solve could not be found — the history was cleared while the screen was open. */
    data object Missing : WinUiState

    data class Solved(val summary: WinSummary) : WinUiState
}
