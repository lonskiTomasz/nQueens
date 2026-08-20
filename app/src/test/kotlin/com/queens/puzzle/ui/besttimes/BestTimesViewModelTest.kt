package com.queens.puzzle.ui.besttimes

import com.queens.puzzle.core.util.time.RelativeDay
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val DAY = 24L * 60 * 60 * 1_000

class BestTimesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val solveRepository = TestSolveRepository()
    private val timeProvider = TestTimeProvider()

    @Test
    fun `an empty history says so`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        assertTrue(viewModel.uiState.value.isEmpty)
        assertEquals(emptyList<BoardSize>(), viewModel.uiState.value.filters)
    }

    @Test
    fun `the fastest solve of a size is marked best with no delta`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 160_000))
        solveRepository.record(solve(boardSize = 8, durationMillis = 106_000))

        val viewModel = viewModel()
        observe(viewModel)

        val rows = viewModel.uiState.value.rows.associateBy { it.solve.durationMillis }
        assertTrue(rows.getValue(106_000L).isBestForSize)
        assertNull(rows.getValue(106_000L).deltaMillis)
    }

    @Test
    fun `a slower solve is measured against its size's best`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 106_000))
        solveRepository.record(solve(boardSize = 8, durationMillis = 160_000))

        val viewModel = viewModel()
        observe(viewModel)

        val slower = viewModel.uiState.value.rows.single { it.solve.durationMillis == 160_000L }
        assertEquals(-54_000L, slower.deltaMillis)
    }

    @Test
    fun `only played sizes are offered as filters`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))
        solveRepository.record(solve(boardSize = 4, durationMillis = 10_000))
        solveRepository.record(solve(boardSize = 8, durationMillis = 90_000))

        val viewModel = viewModel()
        observe(viewModel)

        assertEquals(listOf(BoardSize(4), BoardSize(8)), viewModel.uiState.value.filters)
    }

    @Test
    fun `filtering narrows the rows to one size`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))
        solveRepository.record(solve(boardSize = 4, durationMillis = 10_000))

        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onFilterSelected(BoardSize(4))

        assertEquals(listOf(BoardSize(4)), viewModel.uiState.value.rows.map { it.solve.boardSize })
    }

    @Test
    fun `filtering does not change what best means`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))
        solveRepository.record(solve(boardSize = 8, durationMillis = 160_000))

        val viewModel = viewModel()
        observe(viewModel)
        viewModel.onFilterSelected(BoardSize(8))

        val slower = viewModel.uiState.value.rows.single { it.solve.durationMillis == 160_000L }
        assertEquals(-60_000L, slower.deltaMillis)
    }

    @Test
    fun `rows carry how long ago the solve was`() = runTest {
        solveRepository.record(
            solve(boardSize = 8, durationMillis = 100_000, completedAt = TestTimeProvider.FIXED_NOW - 3 * DAY)
        )

        val viewModel = viewModel()
        observe(viewModel)

        assertEquals(RelativeDay.DaysAgo(3), viewModel.uiState.value.rows.single().occurred)
    }

    @Test
    fun `clearing the history empties the screen and drops the filter`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))

        val viewModel = viewModel()
        observe(viewModel)
        viewModel.onFilterSelected(BoardSize(8))

        viewModel.onClearHistory()

        assertTrue(viewModel.uiState.value.isEmpty)
        assertNull(viewModel.uiState.value.selectedFilter)
    }

    private fun viewModel() = BestTimesViewModel(solveRepository, timeProvider)

    private fun solve(
        boardSize: Int,
        durationMillis: Long,
        completedAt: Long = TestTimeProvider.FIXED_NOW,
    ) = Solve(
        id = 0L,
        boardSize = BoardSize(boardSize),
        durationMillis = durationMillis,
        taps = 20,
        undos = 0,
        completedAtMillis = completedAt,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun TestScope.observe(viewModel: BestTimesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }
}
