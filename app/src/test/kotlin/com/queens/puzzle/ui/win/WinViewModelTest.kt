package com.queens.puzzle.ui.win

import com.queens.puzzle.domain.usecase.ObserveWinSummaryUseCase
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val solveRepository = TestSolveRepository()
    private val timeProvider = TestTimeProvider()

    @Test
    fun `the solve is loaded from its id alone`() = runTest {
        val id = solveRepository.record(
            Solve(0L, BoardSize(8), durationMillis = 107_000, taps = 27, undos = 2, completedAtMillis = 1_000)
        )

        val viewModel = viewModel(id)
        observe(viewModel)

        val state = viewModel.uiState.value as WinUiState.Solved
        assertEquals(107_000L, state.summary.solve.durationMillis)
        assertEquals(27, state.summary.solve.taps)
        assertTrue(state.summary.isNewBest)
    }

    @Test
    fun `an id with no solve behind it reports as missing`() = runTest {
        val viewModel = viewModel(404L)
        observe(viewModel)

        assertEquals(WinUiState.Missing, viewModel.uiState.value)
    }

    @Test
    fun `nothing is claimed before the solve has loaded`() = runTest {
        assertEquals(WinUiState.Loading, viewModel(1L).uiState.value)
    }

    private fun viewModel(solveId: Long) =
        WinViewModel(solveId, timeProvider, ObserveWinSummaryUseCase(solveRepository))

    private fun TestScope.observe(viewModel: WinViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }
}
