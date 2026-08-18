package com.queens.puzzle.testing.repository

import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** An in-memory [AppSettingsRepository]. */
class TestAppSettingsRepository(
    initial: AppSettings = AppSettings(),
    private var themeStored: Boolean = false,
) : AppSettingsRepository {

    private val settings = MutableStateFlow(initial)

    val current: AppSettings get() = settings.value

    override fun observeAppSettings(): Flow<AppSettings> = settings

    override suspend fun setTheme(theme: ThemePreference) {
        themeStored = true
        settings.value = settings.value.copy(theme = theme)
    }

    override suspend fun setLastBoardSize(boardSize: BoardSize) {
        settings.value = settings.value.copy(lastBoardSize = boardSize)
    }

    override suspend fun seedThemeIfUnset(systemTheme: ThemePreference) {
        if (themeStored) return
        themeStored = true
        settings.value = settings.value.copy(theme = systemTheme)
    }
}
