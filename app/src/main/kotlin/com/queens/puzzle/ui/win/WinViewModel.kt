package com.queens.puzzle.ui.win

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.data.util.TimeProvider
import com.queens.puzzle.domain.usecase.GetWinSummaryUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WinViewModel.Factory::class)
class WinViewModel @AssistedInject constructor(
    @Assisted private val solveId: Long,
    private val timeProvider: TimeProvider,
    private val getWinSummary: GetWinSummaryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WinUiState>(WinUiState.Loading)
    val uiState: StateFlow<WinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val summary = getWinSummary(solveId)
            _uiState.value = if (summary == null) WinUiState.Missing else WinUiState.Solved(summary)
        }
    }

    fun newGameId(): Long = timeProvider.nowMillis()

    @AssistedFactory
    interface Factory {
        fun create(solveId: Long): WinViewModel
    }
}
