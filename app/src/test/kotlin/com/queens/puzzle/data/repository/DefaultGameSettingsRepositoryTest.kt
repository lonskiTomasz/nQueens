package com.queens.puzzle.data.repository

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import com.queens.puzzle.data.local.datastore.SettingsDataSource
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.testing.local.InMemoryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultGameSettingsRepositoryTest {

    @Test
    fun `an empty store gives attack lines on and haptics off`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        assertEquals(
            GameSettings(showAttackLines = true, hapticsEnabled = false),
            repository.observeGameSettings().first(),
        )
    }

    @Test
    fun `attack lines round-trip`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        repository.setShowAttackLines(false)

        assertEquals(false, repository.observeGameSettings().first().showAttackLines)
    }

    @Test
    fun `haptics round-trip`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        repository.setHapticsEnabled(true)

        assertEquals(true, repository.observeGameSettings().first().hapticsEnabled)
    }

    @Test
    fun `writing one option leaves the other alone`() = runTest {
        val repository = repositoryOver(emptyPreferences())

        repository.setHapticsEnabled(true)
        repository.setShowAttackLines(false)

        assertEquals(
            GameSettings(showAttackLines = false, hapticsEnabled = true),
            repository.observeGameSettings().first(),
        )
    }

    @Test
    fun `stored values are read back under the documented keys`() = runTest {
        val repository = repositoryOver(
            preferencesOf(
                booleanPreferencesKey("show_attack_lines") to false,
                booleanPreferencesKey("haptics_enabled") to true,
            )
        )

        assertEquals(
            GameSettings(showAttackLines = false, hapticsEnabled = true),
            repository.observeGameSettings().first(),
        )
    }

    private fun repositoryOver(preferences: Preferences) =
        DefaultGameSettingsRepository(SettingsDataSource(InMemoryDataStore(preferences)))
}
