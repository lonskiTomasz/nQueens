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
import javax.inject.Inject

/** Nothing renders until the stored theme is known, so the app cannot flash the wrong scheme. */
sealed interface ThemeState {

    data object Loading : ThemeState

    data class Ready(val theme: ThemePreference) : ThemeState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = appSettingsRepository.observeAppSettings()
        .map { ThemeState.Ready(it.theme) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeState.Loading,
        )
}
