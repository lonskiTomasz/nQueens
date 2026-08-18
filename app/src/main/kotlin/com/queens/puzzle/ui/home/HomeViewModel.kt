package com.queens.puzzle.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.data.repository.SessionRepository
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

private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

/**
 * The home screen.
 *
 * Pure pass-through work goes straight to a repository; the one piece of real logic — folding
 * the history into per-size bests — sits in a use case (§4.4).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val sessionRepository: SessionRepository,
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
            resumableSize = savedGame?.session?.boardSize,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
        initialValue = HomeUiState(),
    )

    /** The chosen size is remembered immediately, so it survives leaving without playing. */
    fun onSizeSelected(boardSize: BoardSize) {
        viewModelScope.launch { appSettingsRepository.setLastBoardSize(boardSize) }
    }

    fun onThemeSelected(theme: ThemePreference) {
        viewModelScope.launch { appSettingsRepository.setTheme(theme) }
    }
}
