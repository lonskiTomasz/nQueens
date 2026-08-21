package com.queens.puzzle.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.data.repository.SessionRepository
import com.queens.puzzle.data.util.TimeProvider
import com.queens.puzzle.domain.usecase.ObserveBestTimesUseCase
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val timeProvider: TimeProvider,
    sessionRepository: SessionRepository,
    observeBestTimes: ObserveBestTimesUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        appSettingsRepository.observeAppSettings(),
        sessionRepository.observeSavedSession(),
        observeBestTimes(),
    ) { appSettings, savedGame, bestTimes ->
        HomeUiState(
            selectedSize = appSettings.lastBoardSize,
            theme = appSettings.theme,
            bestTimes = bestTimes,
            resumable = savedGame?.let { ResumableGame(it.gameId, it.session.boardSize) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeUiState(),
    )

    fun onSizeSelected(boardSize: BoardSize) {
        viewModelScope.launch { appSettingsRepository.setLastBoardSize(boardSize) }
    }

    fun onThemeSelected(theme: ThemePreference) {
        viewModelScope.launch { appSettingsRepository.setTheme(theme) }
    }

    fun newGameId(): Long = timeProvider.nowMillis()
}
