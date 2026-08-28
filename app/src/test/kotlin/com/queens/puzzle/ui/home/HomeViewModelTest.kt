package com.queens.puzzle.ui.home

import com.queens.puzzle.domain.usecase.ObserveBestTimesUseCase
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestAppSettingsRepository
import com.queens.puzzle.testing.repository.TestSessionRepository
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val appSettingsRepository = TestAppSettingsRepository()
    private val sessionRepository = TestSessionRepository()
    private val solveRepository = TestSolveRepository()
    private val timeProvider = TestTimeProvider()

    @Test
    fun `the last played size is preselected`() = runTest {
        appSettingsRepository.setLastBoardSize(BoardSize(10))

        val viewModel = viewModel()
        observe(viewModel)

        assertEquals(BoardSize(10), viewModel.uiState.value.selectedSize)
    }

    @Test
    fun `selecting a size remembers it straight away`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onSizeSelected(BoardSize(6))

        assertEquals(BoardSize(6), appSettingsRepository.current.lastBoardSize)
        assertEquals(BoardSize(6), viewModel.uiState.value.selectedSize)
    }

    @Test
    fun `selecting a puzzle type remembers it and re-sources best times`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))
        solveRepository.record(
            solve(boardSize = 8, puzzleType = PuzzleType.Knights, durationMillis = 40_000),
        )

        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onPuzzleTypeSelected(PuzzleType.Knights)

        assertEquals(PuzzleType.Knights, appSettingsRepository.current.lastPuzzleType)
        assertEquals(PuzzleType.Knights, viewModel.uiState.value.puzzleType)
        assertEquals(40_000L, viewModel.uiState.value.bestTimes.single().bestMillis)
    }

    @Test
    fun `choosing a theme stores it`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onThemeSelected(ThemePreference.Dark)

        assertEquals(ThemePreference.Dark, appSettingsRepository.current.theme)
        assertEquals(ThemePreference.Dark, viewModel.uiState.value.theme)
    }

    @Test
    fun `there is nothing to resume without a stored board`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        assertFalse(viewModel.uiState.value.canResume)
        assertNull(viewModel.uiState.value.resumable)
    }

    @Test
    fun `a stored board offers a resume at its own size`() = runTest {
        sessionRepository.save(
            gameId = 1L,
            puzzleType = PuzzleType.Queens,
            session = GameSession(BoardSize(12)),
            elapsedMillis = 5_000,
        )

        val viewModel = viewModel()
        observe(viewModel)

        assertTrue(viewModel.uiState.value.canResume)
        assertEquals(BoardSize(12), viewModel.uiState.value.resumable?.boardSize)
    }

    @Test
    fun `a stored board of the other puzzle type does not offer a resume`() = runTest {
        sessionRepository.save(
            gameId = 1L,
            puzzleType = PuzzleType.Knights,
            session = GameSession(BoardSize(12)),
            elapsedMillis = 5_000,
        )

        val viewModel = viewModel()
        observe(viewModel)

        assertFalse(viewModel.uiState.value.canResume)
    }

    @Test
    fun `best times are the fastest per size, largest board first`() = runTest {
        solveRepository.record(solve(boardSize = 6, durationMillis = 60_000))
        solveRepository.record(solve(boardSize = 6, durationMillis = 30_000))
        solveRepository.record(solve(boardSize = 10, durationMillis = 120_000))

        val viewModel = viewModel()
        observe(viewModel)

        val bests = viewModel.uiState.value.bestTimes
        assertEquals(listOf(BoardSize(10), BoardSize(6)), bests.map { it.boardSize })
        assertEquals(30_000L, bests.last().bestMillis)
        assertEquals(2, bests.last().solveCount)
    }

    @Test
    fun `a new game is identified apart from the board already stored`() = runTest {
        sessionRepository.save(
            gameId = 1L,
            puzzleType = PuzzleType.Queens,
            session = GameSession(BoardSize(8)),
            elapsedMillis = 5_000,
        )
        val viewModel = viewModel()
        observe(viewModel)

        assertNotEquals(viewModel.uiState.value.resumable?.gameId, viewModel.newGameId())
    }

    @Test
    fun `every selectable size is offered`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        assertEquals(BoardSize.selectable, viewModel.uiState.value.sizes)
    }

    private fun viewModel() = HomeViewModel(
        appSettingsRepository = appSettingsRepository,
        timeProvider = timeProvider,
        sessionRepository = sessionRepository,
        observeBestTimes = ObserveBestTimesUseCase(solveRepository),
    )

    private fun solve(
        boardSize: Int,
        durationMillis: Long,
        puzzleType: PuzzleType = PuzzleType.Queens,
    ) = Solve(
        id = 0L,
        boardSize = BoardSize(boardSize),
        puzzleType = puzzleType,
        durationMillis = durationMillis,
        taps = 10,
        undos = 0,
        completedAtMillis = 1_000,
    )

    private fun TestScope.observe(viewModel: HomeViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }
}
