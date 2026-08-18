package com.queens.puzzle.ui.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.domain.usecase.ObserveWinSummaryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

/**
 * The win screen.
 *
 * Takes the solve id as an assisted constructor parameter rather than reading a nullable
 * `SavedStateHandle` key, so a missing or misspelled argument is a compile error (§12, row 8).
 */
@HiltViewModel(assistedFactory = WinViewModel.Factory::class)
class WinViewModel @AssistedInject constructor(
    @Assisted private val solveId: Long,
    observeWinSummary: ObserveWinSummaryUseCase,
) : ViewModel() {

    val uiState: StateFlow<WinUiState> = observeWinSummary(solveId)
        .map { summary -> if (summary == null) WinUiState.Missing else WinUiState.Solved(summary) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = WinUiState.Loading,
        )

    @AssistedFactory
    interface Factory {
        fun create(solveId: Long): WinViewModel
    }
}
