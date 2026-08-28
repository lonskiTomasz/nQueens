package com.queens.puzzle.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.data.repository.GameSettingsRepository
import com.queens.puzzle.data.repository.SessionRepository
import com.queens.puzzle.data.util.TimeProvider
import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.domain.game.reduce
import com.queens.puzzle.domain.rules.QueenRules
import com.queens.puzzle.domain.rules.evaluate
import com.queens.puzzle.domain.usecase.RecordSolveUseCase
import com.queens.puzzle.model.BoardEvaluation
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.ui.game.board.BoardSquareState
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * How often the clock redraws. The display is `mm:ss`, so anything finer would spend a
 * recomposition to draw the string it is already showing.
 */
private const val TICK_MILLIS = 1_000L

@HiltViewModel(assistedFactory = GameViewModel.Factory::class)
class GameViewModel @AssistedInject constructor(
    @Assisted("boardSize") private val boardSizeValue: Int,
    @Assisted("gameId") private val gameId: Long,
    private val sessionRepository: SessionRepository,
    private val gameSettingsRepository: GameSettingsRepository,
    private val recordSolve: RecordSolveUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val boardSize = BoardSize(boardSizeValue)

    private val positions = boardSize.positions()

    private var session = GameSession(boardSize)

    private var accumulatedMillis = 0L
    private var runningSince: Long? = timeProvider.elapsedMillis()
    private var finalElapsedMillis: Long? = null

    private val _uiState =
        MutableStateFlow(GameUiState(boardSize = boardSize, isRestoring = true))

    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val elapsedMillis: StateFlow<Long> = elapsedTicks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = 0L,
    )

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val pendingSave = MutableStateFlow<PendingSave?>(null)

    init {
        viewModelScope.launch { restore() }
        viewModelScope.launch { observeSettings() }
        viewModelScope.launch { writePendingSaves() }
    }

    fun onAction(action: GameAction) {
        if (finalElapsedMillis != null) return

        val before = session
        val after = before.reduce(action)

        if (after == before) {
            // The reducer refuses a placement once every queen is down; say so rather than
            // letting the tap look broken.
            if (action is GameAction.TapSquare && before.piecesRemaining == 0) {
                emit(GameEffect.BoardFull)
            }
            return
        }

        session = after
        if (action is GameAction.Reset) {
            setElapsed(0L)
        }

        val evaluation = evaluate(after)
        emitPlacementFeedback(action, after, evaluation)

        if (evaluation.isSolved) {
            finalElapsedMillis = elapsedSinceStart()
            publish(evaluation)
            finish()
        } else {
            publish(evaluation)
            saveSession(session, elapsedSinceStart())
        }
    }

    fun onScreenStarted() {
        if (finalElapsedMillis != null || runningSince != null) return
        runningSince = timeProvider.elapsedMillis()
    }

    fun onScreenStopped() {
        val startedAt = runningSince ?: return
        accumulatedMillis += timeProvider.elapsedMillis() - startedAt
        runningSince = null

        if (finalElapsedMillis != null) return
        if (_uiState.value.isRestoring) return

        if (session.isPristine) {
            pendingSave.value = null
            viewModelScope.launch { sessionRepository.clear() }
        } else {
            saveSession(session, accumulatedMillis)
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

    fun onSoundChanged(enabled: Boolean) {
        viewModelScope.launch { gameSettingsRepository.setSoundEnabled(enabled) }
    }

    private suspend fun restore() {
        val saved = sessionRepository.observeSavedSession().first()

        if (saved != null && saved.gameId == gameId && saved.session.boardSize == boardSize) {
            session = saved.session
            setElapsed(saved.elapsedMillis)
        } else {
            sessionRepository.clear()
        }
        update { copy(isRestoring = false) }
        publish(evaluation = evaluate(session))
    }

    private suspend fun observeSettings() {
        gameSettingsRepository.observeGameSettings().collect { settings ->
            update { copy(settings = settings) }
            publish(evaluation = evaluate(session))
        }
    }

    private suspend fun writePendingSaves() {
        pendingSave.collectLatest { pending ->
            if (pending != null) {
                sessionRepository.save(gameId, pending.session, pending.elapsedMillis)
            }
        }
    }

    private fun saveSession(session: GameSession, elapsedMillis: Long) {
        pendingSave.value = PendingSave(session, elapsedMillis)
    }

    private fun finish() {
        viewModelScope.launch {
            emit(GameEffect.CelebrateWin)
            pendingSave.value = null
            sessionRepository.clear()

            val outcome = recordSolve(
                boardSize = boardSize,
                durationMillis = finalElapsedMillis ?: elapsedSinceStart(),
                taps = session.taps,
                undos = session.undos,
            )
            emit(GameEffect.NavigateToWin(outcome.solveId))
        }
    }

    private fun emitPlacementFeedback(
        action: GameAction,
        session: GameSession,
        evaluation: BoardEvaluation
    ) {
        if (action !is GameAction.TapSquare || !session.hasPieceAt(action.position)) return

        emit(
            if (evaluation.isConflicting(action.position)) {
                GameEffect.HapticConflict
            } else {
                GameEffect.HapticPlace
            }
        )
        emit(GameEffect.SoundPlace)
    }

    private fun elapsedTicks(): Flow<Long> = flow {
        while (true) {
            val finished = finalElapsedMillis
            if (finished != null) {
                emit(finished)
                return@flow
            }
            val elapsed = elapsedSinceStart()
            emit(elapsed)
            // Wait to the next whole second rather than a whole second from this redraw
            delay(TICK_MILLIS - elapsed % TICK_MILLIS)
        }
    }

    private fun elapsedSinceStart(): Long {
        val startedAt = runningSince ?: return accumulatedMillis
        return accumulatedMillis + (timeProvider.elapsedMillis() - startedAt)
    }

    private fun setElapsed(millis: Long) {
        accumulatedMillis = millis
        if (runningSince != null) runningSince = timeProvider.elapsedMillis()
    }

    private fun evaluate(session: GameSession): BoardEvaluation = QueenRules.evaluate(
        session = session,
        includeAttackedSquares = _uiState.value.settings.showAttackLines,
    )

    private fun publish(evaluation: BoardEvaluation) {
        update {
            copy(
                squares = positions.map { position ->
                    BoardSquareState(
                        position = position,
                        hasPiece = session.hasPieceAt(position),
                        isConflicting = evaluation.isConflicting(position),
                        isAttacked = evaluation.isAttacked(position),
                    )
                },
                piecesPlaced = session.pieces.size,
                conflictKinds = evaluation.conflictKinds,
                canUndo = session.canUndo,
                isSolved = evaluation.isSolved,
            )
        }
    }

    private inline fun update(block: GameUiState.() -> GameUiState) {
        _uiState.value = _uiState.value.block()
    }

    private fun emit(effect: GameEffect) {
        _effects.trySend(effect)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("boardSize") boardSize: Int,
            @Assisted("gameId") gameId: Long,
        ): GameViewModel
    }
}

private data class PendingSave(val session: GameSession, val elapsedMillis: Long)
