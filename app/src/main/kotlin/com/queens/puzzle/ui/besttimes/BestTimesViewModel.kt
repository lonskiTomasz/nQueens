package com.queens.puzzle.ui.besttimes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.core.util.time.RelativeDayCalculator
import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.data.util.TimeProvider
import com.queens.puzzle.model.BoardSize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class BestTimesViewModel @Inject constructor(
    private val solveRepository: SolveRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val selectedFilter = MutableStateFlow<BoardSize?>(null)

    val uiState: StateFlow<BestTimesUiState> = combine(
        solveRepository.observeSolves(),
        selectedFilter,
    ) { solves, filter ->
        val bestByModeAndSize = solves
            .groupBy { it.puzzleType to it.boardSize }
            .mapValues { (_, forModeAndSize) -> forModeAndSize.minOf { it.durationMillis } }

        val now = timeProvider.nowMillis()

        BestTimesUiState(
            filters = solves.map { it.boardSize }.distinct().sortedBy { it.value },
            selectedFilter = filter,
            rows = solves
                .filter { filter == null || it.boardSize == filter }
                .map { solve ->
                    val best = bestByModeAndSize.getValue(solve.puzzleType to solve.boardSize)
                    val isBest = solve.durationMillis == best

                    SolveRow(
                        solve = solve,
                        isBestForSize = isBest,
                        deltaMillis = if (isBest) null else best - solve.durationMillis,
                        occurred = RelativeDayCalculator.of(solve.completedAtMillis, now),
                    )
                },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = BestTimesUiState(),
    )

    fun onFilterSelected(boardSize: BoardSize?) {
        selectedFilter.value = boardSize
    }

    fun onClearHistory() {
        viewModelScope.launch {
            solveRepository.clearHistory()
            selectedFilter.value = null
        }
    }
}
