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
import com.queens.puzzle.model.PuzzleType
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
            soundEnabled = stored[Keys.SOUND_ENABLED] ?: DEFAULT_GAME.soundEnabled,
        )
    }

    val appSettings: Flow<AppSettings> = preferences.map { stored ->
        AppSettings(
            theme = stored[Keys.THEME]?.toThemeOrNull() ?: DEFAULT_APP.theme,
            lastBoardSize = stored[Keys.LAST_BOARD_SIZE]?.let(BoardSize::ofOrNull)
                ?: DEFAULT_APP.lastBoardSize,
            lastPuzzleType = stored[Keys.LAST_PUZZLE_TYPE]?.toPuzzleTypeOrNull()
                ?: DEFAULT_APP.lastPuzzleType,
        )
    }

    suspend fun setShowAttackLines(enabled: Boolean) = edit(Keys.SHOW_ATTACK_LINES, enabled)

    suspend fun setHapticsEnabled(enabled: Boolean) = edit(Keys.HAPTICS_ENABLED, enabled)

    suspend fun setSoundEnabled(enabled: Boolean) = edit(Keys.SOUND_ENABLED, enabled)

    suspend fun setTheme(theme: ThemePreference) = edit(Keys.THEME, theme.name)

    suspend fun setLastBoardSize(boardSize: BoardSize) = edit(Keys.LAST_BOARD_SIZE, boardSize.value)

    suspend fun setLastPuzzleType(puzzleType: PuzzleType) =
        edit(Keys.LAST_PUZZLE_TYPE, puzzleType.name)

    private suspend fun <T> edit(key: Preferences.Key<T>, value: T) {
        dataStore.edit { it[key] = value }
    }

    private fun String.toThemeOrNull(): ThemePreference? =
        ThemePreference.entries.firstOrNull { it.name == this }

    private fun String.toPuzzleTypeOrNull(): PuzzleType? =
        PuzzleType.entries.firstOrNull { it.name == this }

    private object Keys {
        val SHOW_ATTACK_LINES = booleanPreferencesKey("show_attack_lines")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val THEME = stringPreferencesKey("theme")
        val LAST_BOARD_SIZE = intPreferencesKey("last_board_size")
        val LAST_PUZZLE_TYPE = stringPreferencesKey("last_puzzle_type")
    }

    companion object {

        const val STORE_NAME = "settings"

        private val DEFAULT_GAME = GameSettings()
        private val DEFAULT_APP = AppSettings()
    }
}
