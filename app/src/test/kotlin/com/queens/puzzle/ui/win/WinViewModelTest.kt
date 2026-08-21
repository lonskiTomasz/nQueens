package com.queens.puzzle.ui.win

import com.queens.puzzle.domain.usecase.GetWinSummaryUseCase
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val solveRepository = TestSolveRepository()
    private val timeProvider = TestTimeProvider()

    @Test
    fun `the solve is loaded from its id alone`() = runTest {
        val id = solveRepository.record(
            Solve(0L, BoardSize(8), durationMillis = 107_000, taps = 27, undos = 2, completedAtMillis = 1_000)
        )

        val viewModel = viewModel(id)
        advanceUntilIdle()

        val state = viewModel.uiState.value as WinUiState.Solved
        assertEquals(107_000L, state.summary.solve.durationMillis)
        assertEquals(27, state.summary.solve.taps)
        assertTrue(state.summary.isNewBest)
    }

    @Test
    fun `an id with no solve behind it reports as missing`() = runTest {
        val viewModel = viewModel(404L)
        advanceUntilIdle()

        assertEquals(WinUiState.Missing, viewModel.uiState.value)
    }

    @Test
    fun `nothing is claimed before the solve has loaded`() = runTest {
        val id = solveRepository.record(
            Solve(0L, BoardSize(8), durationMillis = 107_000, taps = 27, undos = 2, completedAtMillis = 1_000)
        )

        val viewModel = viewModel(id)
        assertEquals(WinUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is WinUiState.Solved)
    }

    private fun viewModel(solveId: Long) =
        WinViewModel(solveId, timeProvider, GetWinSummaryUseCase(solveRepository))
}
