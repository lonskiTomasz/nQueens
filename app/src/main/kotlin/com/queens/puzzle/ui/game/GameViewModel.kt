package com.queens.puzzle.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.data.repository.GameSettingsRepository
import com.queens.puzzle.data.repository.SessionRepository
import com.queens.puzzle.data.util.TimeProvider
import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.domain.game.reduce
import com.queens.puzzle.domain.rules.BoardEvaluator
import com.queens.puzzle.domain.usecase.RecordSolveUseCase
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.ui.board.BoardSquareState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How often the clock redraws. Fine enough to look live, coarse enough to be cheap. */
private const val TICK_MILLIS = 200L

/** Keeps the clock running across a configuration change without restarting the game. */
private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

/**
 * Orchestrates one game.
 *
 * Holds a [GameSession], applies the pure reducer, evaluates the board, maps the result to
 * state and persists it. All the interesting logic is in the pure functions underneath.
 */
@HiltViewModel(assistedFactory = GameViewModel.Factory::class)
class GameViewModel @AssistedInject constructor(
    @Assisted("boardSize") private val boardSizeValue: Int,
    @Assisted("resume") private val resume: Boolean,
    private val sessionRepository: SessionRepository,
    private val gameSettingsRepository: GameSettingsRepository,
    private val recordSolve: RecordSolveUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val boardSize = BoardSize(boardSizeValue)

    /** Everything except the clock, which ticks on its own. */
    private val boardState = MutableStateFlow(GameUiState(boardSize = boardSize))

    /**
     * The clock is folded in here rather than written into [boardState], so it only ticks
     * while something is watching — and a test that never collects never starts it.
     */
    val uiState: StateFlow<GameUiState> =
        combine(boardState, elapsedTicks()) { state, elapsed -> state.copy(elapsedMillis = elapsed) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
                initialValue = GameUiState(boardSize = boardSize),
            )

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var session = GameSession(boardSize)

    /**
     * Monotonic reading the clock started from. Elapsed is always `now - startedAt`, so the
     * timer cannot drift by accumulating ticks (§6.3).
     */
    private var startedAt = timeProvider.elapsedMillis()

    /** Set once the board is solved: the clock stops here and the game stops accepting input. */
    private var finalElapsedMillis: Long? = null

    /**
     * The board waiting to be written. A `StateFlow` conflates by nature, so a burst of taps
     * collapses into one write of the latest board rather than one write per tap (§5.2).
     */
    private val pendingSave = MutableStateFlow<SavedPoint?>(null)

    init {
        viewModelScope.launch { restore() }
        viewModelScope.launch { observeSettings() }
        viewModelScope.launch { drainSaves() }
    }

    fun onAction(action: GameAction) {
        if (finalElapsedMillis != null) return

        val before = session
        val after = before.reduce(action)

        if (after == before) {
            // The reducer refuses a placement once every queen is down; say so rather than
            // letting the tap look broken.
            if (action is GameAction.TapSquare && before.queensRemaining == 0) {
                emit(GameEffect.BoardFull)
            }
            return
        }

        session = after
        val evaluation = BoardEvaluator.evaluate(after)

        if (action is GameAction.TapSquare && after.hasQueenAt(action.position)) {
            emit(
                if (evaluation.isConflicting(action.position)) {
                    GameEffect.HapticConflict
                } else {
                    GameEffect.HapticPlace
                }
            )
        }

        if (evaluation.isSolved) {
            finalElapsedMillis = elapsedSinceStart()
            publish()
            viewModelScope.launch { finish() }
        } else {
            publish()
            pendingSave.value = SavedPoint(session, elapsedSinceStart())
        }
    }

    fun onResetRequested() = update { copy(isResetDialogVisible = true) }

    fun onResetDismissed() = update { copy(isResetDialogVisible = false) }

    fun onResetConfirmed() {
        update { copy(isResetDialogVisible = false) }
        onAction(GameAction.Reset)
    }

    fun onSettingsOpened() = update { copy(isSettingsSheetVisible = true) }

    fun onSettingsDismissed() = update { copy(isSettingsSheetVisible = false) }

    fun onShowAttackLinesChanged(enabled: Boolean) {
        viewModelScope.launch { gameSettingsRepository.setShowAttackLines(enabled) }
    }

    fun onHapticsChanged(enabled: Boolean) {
        viewModelScope.launch { gameSettingsRepository.setHapticsEnabled(enabled) }
    }

    /** Resumes the stored board when asked, and only when it is the same size. */
    private suspend fun restore() {
        val saved = sessionRepository.observeSavedSession().first()

        if (resume && saved != null && saved.session.boardSize == boardSize) {
            session = saved.session
            startedAt = timeProvider.elapsedMillis() - saved.elapsedMillis
        } else {
            sessionRepository.clear()
        }
        publish()
    }

    private suspend fun observeSettings() {
        gameSettingsRepository.observeGameSettings().collect { settings ->
            update { copy(settings = settings) }
            publish()
        }
    }

    private suspend fun drainSaves() {
        pendingSave.filterNotNull().collect { point ->
            sessionRepository.save(point.session, point.elapsedMillis)
        }
    }

    private suspend fun finish() {
        emit(GameEffect.CelebrateWin)
        sessionRepository.clear()

        val outcome = recordSolve(
            boardSize = boardSize,
            durationMillis = finalElapsedMillis ?: elapsedSinceStart(),
            taps = session.taps,
            undos = session.undos,
        )
        emit(GameEffect.NavigateToWin(outcome.solveId))
    }

    /** Emits the finished time once and stops; otherwise ticks until the collector goes away. */
    private fun elapsedTicks(): Flow<Long> = flow {
        while (true) {
            val finished = finalElapsedMillis
            if (finished != null) {
                emit(finished)
                return@flow
            }
            emit(elapsedSinceStart())
            delay(TICK_MILLIS)
        }
    }

    private fun elapsedSinceStart(): Long = timeProvider.elapsedMillis() - startedAt

    private fun publish() {
        val evaluation = BoardEvaluator.evaluate(session)
        val showAttackLines = boardState.value.settings.showAttackLines

        update {
            copy(
                boardSize = boardSize,
                squares = boardSize.positions().map { position ->
                    BoardSquareState(
                        position = position,
                        hasQueen = session.hasQueenAt(position),
                        isConflicting = evaluation.isConflicting(position),
                        isAttacked = showAttackLines && evaluation.isAttacked(position),
                    )
                },
                queensPlaced = session.queens.size,
                conflictKinds = evaluation.conflictKinds,
                canUndo = session.canUndo,
                isSolved = evaluation.isSolved,
            )
        }
    }

    private inline fun update(block: GameUiState.() -> GameUiState) {
        boardState.value = boardState.value.block()
    }

    private fun emit(effect: GameEffect) {
        _effects.trySend(effect)
    }

    private data class SavedPoint(val session: GameSession, val elapsedMillis: Long)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("boardSize") boardSize: Int,
            @Assisted("resume") resume: Boolean,
        ): GameViewModel
    }
}
