package com.queens.puzzle.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one preferences file, holding both gameplay options and app-wide preferences.
 *
 * Two repositories read it (§12, row 15) — the split is in the interfaces, not the storage.
 * Values that cannot be interpreted fall back to the default rather than throwing: this file
 * is editable on a rooted device and survives a downgrade, so it is not trusted input.
 */
@Singleton
class SettingsDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** A read failure yields defaults rather than killing the collector. */
    private val preferences: Flow<Preferences> = dataStore.data
        .catch { cause ->
            if (cause is IOException) emit(emptyPreferences()) else throw cause
        }

    val gameSettings: Flow<GameSettings> = preferences.map { stored ->
        GameSettings(
            showAttackLines = stored[Keys.SHOW_ATTACK_LINES] ?: DEFAULT_GAME.showAttackLines,
            hapticsEnabled = stored[Keys.HAPTICS_ENABLED] ?: DEFAULT_GAME.hapticsEnabled,
        )
    }

    val appSettings: Flow<AppSettings> = preferences.map { stored ->
        AppSettings(
            theme = stored[Keys.THEME]?.toThemeOrNull() ?: DEFAULT_APP.theme,
            lastBoardSize = stored[Keys.LAST_BOARD_SIZE]?.let(BoardSize::ofOrNull)
                ?: DEFAULT_APP.lastBoardSize,
        )
    }

    suspend fun setShowAttackLines(enabled: Boolean) = edit(Keys.SHOW_ATTACK_LINES, enabled)

    suspend fun setHapticsEnabled(enabled: Boolean) = edit(Keys.HAPTICS_ENABLED, enabled)

    suspend fun setTheme(theme: ThemePreference) = edit(Keys.THEME, theme.name)

    suspend fun setLastBoardSize(boardSize: BoardSize) = edit(Keys.LAST_BOARD_SIZE, boardSize.value)

    /**
     * Writes [systemTheme] only when no theme has been stored, so calling this on every launch
     * cannot overwrite a choice the player has made. The check and the write share one [edit],
     * which DataStore applies atomically.
     */
    suspend fun seedThemeIfUnset(systemTheme: ThemePreference) {
        dataStore.edit { stored ->
            if (stored[Keys.THEME] == null) stored[Keys.THEME] = systemTheme.name
        }
    }

    private suspend fun <T> edit(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private fun String.toThemeOrNull(): ThemePreference? =
        ThemePreference.entries.firstOrNull { it.name == this }

    private object Keys {
        val SHOW_ATTACK_LINES = booleanPreferencesKey("show_attack_lines")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val THEME = stringPreferencesKey("theme")
        val LAST_BOARD_SIZE = intPreferencesKey("last_board_size")
    }

    companion object {

        /** File name of the preferences store, without its `.preferences_pb` suffix. */
        const val STORE_NAME = "settings"

        private val DEFAULT_GAME = GameSettings()
        private val DEFAULT_APP = AppSettings()
    }
}
