package com.queens.puzzle.ui

import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestAppSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `the stored theme is what gets painted`() = runTest {
        val repository = TestAppSettingsRepository(AppSettings(theme = ThemePreference.Dark))

        val viewModel = MainViewModel(repository)

        assertEquals(ThemeState.Ready(ThemePreference.Dark), viewModel.themeState.value)
    }

    @Test
    fun `a player who has never chosen follows the system, with nothing written`() = runTest {
        val repository = TestAppSettingsRepository()

        val viewModel = MainViewModel(repository)

        val theme = (viewModel.themeState.value as ThemeState.Ready).theme
        assertEquals(ThemePreference.System, theme)
        assertTrue(theme.shouldUseDarkTheme(systemDark = true))
        assertFalse(theme.shouldUseDarkTheme(systemDark = false))
        assertEquals(AppSettings(), repository.current)
    }

    @Test
    fun `a stored choice ignores the system scheme`() = runTest {
        val repository = TestAppSettingsRepository(AppSettings(theme = ThemePreference.Light))

        val viewModel = MainViewModel(repository)

        val theme = (viewModel.themeState.value as ThemeState.Ready).theme
        assertFalse(theme.shouldUseDarkTheme(systemDark = true))
    }
}
