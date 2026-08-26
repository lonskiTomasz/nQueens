package com.queens.puzzle.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.queens.puzzle.data.local.datastore.SettingsDataSource
import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.testing.local.InMemoryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAppSettingsRepositoryTest {

    @Test
    fun `an empty store follows the system theme and gives the default board size`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        assertEquals(
            AppSettings(theme = ThemePreference.System, lastBoardSize = BoardSize.Default),
            repository.observeAppSettings().first(),
        )
    }

    @Test
    fun `theme round-trips`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        repository.setTheme(ThemePreference.Dark)

        assertEquals(ThemePreference.Dark, repository.observeAppSettings().first().theme)
    }

    @Test
    fun `last board size round-trips`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        repository.setLastBoardSize(BoardSize(12))

        assertEquals(BoardSize(12), repository.observeAppSettings().first().lastBoardSize)
    }

    @Test
    fun `a stored board size outside the supported range falls back to the default`() = runTest {
        val repository = repositoryOver(preferencesOf(intPreferencesKey("last_board_size") to 99))

        assertEquals(BoardSize.Default, repository.observeAppSettings().first().lastBoardSize)
    }

    private fun repositoryOver(preferences: Preferences) =
        DefaultAppSettingsRepository(SettingsDataSource(InMemoryDataStore(preferences)))
}
