package com.queens.puzzle.data.repository

import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    fun observeAppSettings(): Flow<AppSettings>

    suspend fun setTheme(theme: ThemePreference)

    suspend fun setLastBoardSize(boardSize: BoardSize)

    /**
     * Stores [systemTheme] as the theme if none has been stored yet. Does nothing once the
     * player has a stored choice, so this is safe to call on every launch.
     */
    suspend fun seedThemeIfUnset(systemTheme: ThemePreference)
}
