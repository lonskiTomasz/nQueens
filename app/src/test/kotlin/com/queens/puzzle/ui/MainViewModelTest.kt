package com.queens.puzzle.ui

import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.testing.MainDispatcherRule
import com.queens.puzzle.testing.repository.TestAppSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `the stored theme is what gets painted`() = runTest {
        val repository = TestAppSettingsRepository(
            AppSettings(theme = ThemePreference.Dark),
            themeStored = true,
        )

        val viewModel = MainViewModel(repository)

        assertEquals(ThemeState.Ready(ThemePreference.Dark), viewModel.themeState.value)
    }

    @Test
    fun `the system theme seeds the setting on a first launch`() = runTest {
        val repository = TestAppSettingsRepository()
        val viewModel = MainViewModel(repository)

        viewModel.seedTheme(ThemePreference.Dark)

        assertEquals(ThemePreference.Dark, repository.current.theme)
        assertEquals(ThemeState.Ready(ThemePreference.Dark), viewModel.themeState.value)
    }

    @Test
    fun `seeding never overrides a choice the player has made`() = runTest {
        val repository = TestAppSettingsRepository()
        val viewModel = MainViewModel(repository)
        repository.setTheme(ThemePreference.Light)

        viewModel.seedTheme(ThemePreference.Dark)

        assertEquals(ThemePreference.Light, repository.current.theme)
    }
}
