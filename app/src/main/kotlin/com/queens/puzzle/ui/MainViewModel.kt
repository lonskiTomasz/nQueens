package com.queens.puzzle.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Nothing renders until the stored theme is known, so the app cannot flash the wrong scheme. */
sealed interface ThemeState {

    data object Loading : ThemeState

    data class Ready(val theme: ThemePreference) : ThemeState
}

/**
 * Holds the one thing the root composable needs: which scheme to paint.
 *
 * Light and dark are the player's choice rather than the system's (§12, row 12); the system
 * value only seeds the setting the first time the app runs.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = appSettingsRepository.observeAppSettings()
        .map { ThemeState.Ready(it.theme) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeState.Loading,
        )

    /** Safe on every launch: it only writes when the player has no stored choice. */
    fun seedTheme(systemTheme: ThemePreference) {
        viewModelScope.launch { appSettingsRepository.seedThemeIfUnset(systemTheme) }
    }
}
