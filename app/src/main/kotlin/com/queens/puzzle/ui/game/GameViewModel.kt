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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** How often the clock redraws. Fine enough to look live, coarse enough to be cheap. */
private const val TICK_MILLIS = 200L

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

    private val boardState = MutableStateFlow(GameUiState(boardSize = boardSize))

    val uiState: StateFlow<GameUiState> =
        combine(boardState, elapsedTicks()) { state, elapsed -> state.copy(elapsedMillis = elapsed) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = GameUiState(boardSize = boardSize),
            )

    private val _effects = Channel<GameEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var session = GameSession(boardSize)

    private var startedAt = timeProvider.elapsedMillis()

    private var finalElapsedMillis: Long? = null

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
        if (action is GameAction.Reset) {
            startedAt = timeProvider.elapsedMillis()
        }
        val evaluation = BoardEvaluator.evaluate(after)

        if (action is GameAction.TapSquare && after.hasQueenAt(action.position)) {
            emit(
                if (evaluation.isConflicting(action.position)) {
                    GameEffect.HapticConflict
                } else {
                    GameEffect.HapticPlace
                }
            )
            emit(GameEffect.SoundPlace)
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

    fun onSoundChanged(enabled: Boolean) {
        viewModelScope.launch { gameSettingsRepository.setSoundEnabled(enabled) }
    }

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
