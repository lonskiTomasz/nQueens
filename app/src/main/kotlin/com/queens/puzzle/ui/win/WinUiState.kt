package com.queens.puzzle.ui.win

import com.queens.puzzle.model.WinSummary

sealed interface WinUiState {

    data object Loading : WinUiState

    /** The solve could not be found — the history was cleared before the screen loaded it. */
    data object Missing : WinUiState

    data class Solved(val summary: WinSummary) : WinUiState
}
