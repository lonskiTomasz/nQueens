package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.datastore.SettingsDataSource
import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** App-wide preferences, over the preferences store. */
@Singleton
class DefaultAppSettingsRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
) : AppSettingsRepository {

    override fun observeAppSettings(): Flow<AppSettings> = settingsDataSource.appSettings

    override suspend fun setTheme(theme: ThemePreference) = settingsDataSource.setTheme(theme)

    override suspend fun setLastBoardSize(boardSize: BoardSize) =
        settingsDataSource.setLastBoardSize(boardSize)

    override suspend fun seedThemeIfUnset(systemTheme: ThemePreference) =
        settingsDataSource.seedThemeIfUnset(systemTheme)
}
