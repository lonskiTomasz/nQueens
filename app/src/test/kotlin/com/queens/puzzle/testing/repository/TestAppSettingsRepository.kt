package com.queens.puzzle.testing.repository

import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestAppSettingsRepository(
    initial: AppSettings = AppSettings(),
) : AppSettingsRepository {

    private val settings = MutableStateFlow(initial)

    val current: AppSettings get() = settings.value

    override fun observeAppSettings(): Flow<AppSettings> = settings

    override suspend fun setTheme(theme: ThemePreference) {
        settings.value = settings.value.copy(theme = theme)
    }

    override suspend fun setLastBoardSize(boardSize: BoardSize) {
        settings.value = settings.value.copy(lastBoardSize = boardSize)
    }
}
