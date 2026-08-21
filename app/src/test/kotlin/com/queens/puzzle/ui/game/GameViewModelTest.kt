package com.queens.puzzle.ui.game

import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.domain.usecase.RecordSolveUseCase
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestGameSettingsRepository
import com.queens.puzzle.testing.repository.TestSessionRepository
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionRepository = TestSessionRepository()
    private val gameSettingsRepository = TestGameSettingsRepository()
    private val solveRepository = TestSolveRepository()
    private val timeProvider = TestTimeProvider()

    @Test
    fun `tapping an empty square places a queen`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        val state = viewModel.uiState.value
        assertTrue(state.squares.single { it.position == Position(0, 0) }.hasQueen)
        assertEquals(1, state.queensPlaced)
        assertEquals(3, state.queensRemaining)
    }

    @Test
    fun `tapping an occupied square removes its queen`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))
        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        assertEquals(0, viewModel.uiState.value.queensPlaced)
    }

    @Test
    fun `placing a queen asks for the placement tick`() = runTest {
        val viewModel = viewModel()
        val effects = observeEffects(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        assertEquals(listOf(GameEffect.HapticPlace, GameEffect.SoundPlace), effects)
    }

    @Test
    fun `placing into a conflict asks for the conflict tick and names the line`() = runTest {
        val viewModel = viewModel()
        val effects = observeEffects(viewModel)
        observe(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))
        viewModel.onAction(GameAction.TapSquare(Position(1, 1)))

        assertEquals(
            listOf(GameEffect.HapticPlace, GameEffect.SoundPlace, GameEffect.HapticConflict, GameEffect.SoundPlace),
            effects,
        )
        assertEquals(setOf(ConflictKind.Diagonal), viewModel.uiState.value.conflictKinds)
        assertTrue(viewModel.uiState.value.hasConflicts)
    }

    @Test
    fun `a row conflict is named as a row`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))
        viewModel.onAction(GameAction.TapSquare(Position(0, 2)))

        assertEquals(setOf(ConflictKind.Row), viewModel.uiState.value.conflictKinds)
    }

    @Test
    fun `tapping a full board is refused and says so`() = runTest {
        val viewModel = viewModel()
        val effects = observeEffects(viewModel)
        observe(viewModel)

        // Four queens down the first column fills the board without solving it.
        repeat(4) { row -> viewModel.onAction(GameAction.TapSquare(Position(row, 0))) }
        effects.clear()

        viewModel.onAction(GameAction.TapSquare(Position(0, 3)))

        assertEquals(listOf(GameEffect.BoardFull), effects)
        assertEquals(4, viewModel.uiState.value.queensPlaced)
    }

    @Test
    fun `undo unwinds exactly one move`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))
        viewModel.onAction(GameAction.TapSquare(Position(2, 1)))
        viewModel.onAction(GameAction.Undo)

        val state = viewModel.uiState.value
        assertEquals(1, state.queensPlaced)
        assertTrue(state.squares.single { it.position == Position(0, 0) }.hasQueen)
    }

    @Test
    fun `confirming reset clears the board and closes the dialog`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)
        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        viewModel.onResetRequested()
        assertTrue(viewModel.uiState.value.isResetDialogVisible)

        viewModel.onResetConfirmed()

        val state = viewModel.uiState.value
        assertFalse(state.isResetDialogVisible)
        assertEquals(0, state.queensPlaced)
        assertFalse(state.canUndo)
    }

    @Test
    fun `confirming reset restarts the clock`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)
        timeProvider.advanceBy(30_000)
        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        viewModel.onResetRequested()
        viewModel.onResetConfirmed()

        assertEquals(0L, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun `dismissing reset leaves the board alone`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)
        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        viewModel.onResetRequested()
        viewModel.onResetDismissed()

        assertFalse(viewModel.uiState.value.isResetDialogVisible)
        assertEquals(1, viewModel.uiState.value.queensPlaced)
    }

    @Test
    fun `solving records the solve and navigates to the win screen`() = runTest {
        val viewModel = viewModel()
        val effects = observeEffects(viewModel)
        observe(viewModel)
        timeProvider.advanceBy(90_000)

        SOLUTION_4x4.forEach { viewModel.onAction(GameAction.TapSquare(it)) }

        assertTrue(viewModel.uiState.value.isSolved)
        assertTrue(effects.contains(GameEffect.CelebrateWin))

        val recorded = solveRepository.recorded.single()
        assertEquals(BoardSize(4), recorded.boardSize)
        assertEquals(90_000L, recorded.durationMillis)
        assertEquals(4, recorded.taps)
        assertEquals(GameEffect.NavigateToWin(recorded.id), effects.last())
    }

    @Test
    fun `solving stops the clock`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)
        timeProvider.advanceBy(30_000)

        SOLUTION_4x4.forEach { viewModel.onAction(GameAction.TapSquare(it)) }
        timeProvider.advanceBy(60_000)
        advanceTimeBy(1_000)

        assertEquals(30_000L, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun `a solved board stops accepting taps`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)
        SOLUTION_4x4.forEach { viewModel.onAction(GameAction.TapSquare(it)) }

        viewModel.onAction(GameAction.TapSquare(SOLUTION_4x4.first()))

        assertEquals(4, viewModel.uiState.value.queensPlaced)
        assertEquals(1, solveRepository.recorded.size)
    }

    @Test
    fun `the clock accumulates while the board is watched`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        timeProvider.advanceBy(2_000)
        advanceTimeBy(1_000)

        assertEquals(2_000L, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun `each move is written to the session store`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        val saved = sessionRepository.current!!
        assertEquals(setOf(Position(0, 0)), saved.session.queens)
    }

    @Test
    fun `resuming restores the queens and the elapsed time`() = runTest {
        sessionRepository.save(
            gameId = GAME_ID,
            session = GameSession(BoardSize(4), queens = setOf(Position(0, 0)), taps = 1),
            elapsedMillis = 45_000,
        )

        val viewModel = viewModel(gameId = GAME_ID)
        observe(viewModel)
        advanceTimeBy(1_000)

        val state = viewModel.uiState.value
        assertEquals(1, state.queensPlaced)
        assertEquals(45_000L, state.elapsedMillis)
    }

    @Test
    fun `starting a fresh game discards the board another game stored`() = runTest {
        sessionRepository.save(
            gameId = GAME_ID,
            session = GameSession(BoardSize(4), queens = setOf(Position(0, 0))),
            elapsedMillis = 45_000,
        )

        val viewModel = viewModel(gameId = GAME_ID + 1)
        observe(viewModel)

        assertEquals(0, viewModel.uiState.value.queensPlaced)
        assertNull(sessionRepository.current)
    }

    @Test
    fun `a stored board of another size is not resumed`() = runTest {
        sessionRepository.save(
            gameId = GAME_ID,
            session = GameSession(BoardSize(8), queens = setOf(Position(0, 0))),
            elapsedMillis = 45_000,
        )

        val viewModel = viewModel(boardSize = 4, gameId = GAME_ID)
        observe(viewModel)

        assertEquals(0, viewModel.uiState.value.queensPlaced)
    }

    @Test
    fun `solving clears the stored board so it cannot be resumed`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        SOLUTION_4x4.forEach { viewModel.onAction(GameAction.TapSquare(it)) }

        assertNull(sessionRepository.current)
    }

    @Test
    fun `attack lines follow the stored setting`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)
        viewModel.onAction(GameAction.TapSquare(Position(0, 0)))

        assertTrue(viewModel.uiState.value.squares.single { it.position == Position(0, 1) }.isAttacked)

        viewModel.onShowAttackLinesChanged(false)

        assertFalse(viewModel.uiState.value.squares.single { it.position == Position(0, 1) }.isAttacked)
        assertEquals(false, gameSettingsRepository.current.showAttackLines)
    }

    @Test
    fun `the haptics toggle round-trips through the repository`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onHapticsChanged(true)

        assertEquals(true, gameSettingsRepository.current.hapticsEnabled)
        assertEquals(GameSettings(hapticsEnabled = true), viewModel.uiState.value.settings)
    }

    @Test
    fun `the sound toggle round-trips through the repository`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onSoundChanged(true)

        assertEquals(true, gameSettingsRepository.current.soundEnabled)
        assertEquals(GameSettings(soundEnabled = true), viewModel.uiState.value.settings)
    }

    @Test
    fun `the settings sheet opens and closes`() = runTest {
        val viewModel = viewModel()
        observe(viewModel)

        viewModel.onSettingsOpened()
        assertTrue(viewModel.uiState.value.isSettingsSheetVisible)

        viewModel.onSettingsDismissed()
        assertFalse(viewModel.uiState.value.isSettingsSheetVisible)
    }

    private fun viewModel(boardSize: Int = 4, gameId: Long = GAME_ID) = GameViewModel(
        boardSizeValue = boardSize,
        gameId = gameId,
        sessionRepository = sessionRepository,
        gameSettingsRepository = gameSettingsRepository,
        recordSolve = RecordSolveUseCase(solveRepository, timeProvider),
        timeProvider = timeProvider,
    )

    private fun TestScope.observe(viewModel: GameViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
    }

    private fun TestScope.observeEffects(viewModel: GameViewModel): MutableList<GameEffect> {
        val effects = mutableListOf<GameEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.collect { effects += it }
        }
        return effects
    }
}

private const val GAME_ID = 1_700_000_000_000L

private val SOLUTION_4x4 = listOf(Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2))
