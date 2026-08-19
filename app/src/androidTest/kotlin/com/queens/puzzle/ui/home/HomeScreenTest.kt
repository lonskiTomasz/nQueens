package com.queens.puzzle.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.ui.designsystem.theme.QueensTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theStartButtonNamesTheSelectedSize() {
        setScreen(HomeUiState(selectedSize = BoardSize(10)))

        composeRule.onNodeWithText("Start 10 × 10 game").assertIsDisplayed()
    }

    @Test
    fun theSelectedSizeIsMarkedSelected() {
        setScreen(HomeUiState(selectedSize = BoardSize(6)))

        composeRule.onNodeWithContentDescription("6 × 6 board").assertIsSelected()
    }

    @Test
    fun choosingASizeReportsIt() {
        var chosen: BoardSize? = null
        setScreen(HomeUiState(), onSizeSelected = { chosen = it })

        composeRule.onNodeWithContentDescription("12 × 12 board").performClick()

        assertEquals(BoardSize(12), chosen)
    }

    @Test
    fun startingReportsTheSelectedSize() {
        var started: BoardSize? = null
        setScreen(HomeUiState(selectedSize = BoardSize(5)), onStartGame = { started = it })

        composeRule.onNodeWithText("Start 5 × 5 game").performClick()

        assertEquals(BoardSize(5), started)
    }

    @Test
    fun resumeIsHiddenWithoutAStoredBoard() {
        setScreen(HomeUiState(resumableSize = null))

        composeRule.onNodeWithText("Resume last board").assertDoesNotExist()
    }

    @Test
    fun resumeReportsTheStoredSize() {
        var resumed: BoardSize? = null
        setScreen(
            HomeUiState(selectedSize = BoardSize(8), resumableSize = BoardSize(6)),
            onResumeGame = { resumed = it },
        )

        composeRule.onNodeWithText("Resume last board").performClick()

        assertEquals(BoardSize(6), resumed)
    }

    @Test
    fun tappingTheThemeSwitchTurnsTheDarkThemeOn() {
        var theme: ThemePreference? = null
        setScreen(HomeUiState(theme = ThemePreference.Light), onThemeSelected = { theme = it })

        composeRule.onNodeWithContentDescription("Dark theme").assertIsOff().performClick()

        assertEquals(ThemePreference.Dark, theme)
    }

    @Test
    fun tappingTheThemeSwitchAgainTurnsItBackOff() {
        var theme: ThemePreference? = null
        setScreen(HomeUiState(theme = ThemePreference.Dark), onThemeSelected = { theme = it })

        composeRule.onNodeWithContentDescription("Dark theme").assertIsOn().performClick()

        assertEquals(ThemePreference.Light, theme)
    }

    @Test
    fun anEmptyHistorySaysSo() {
        setScreen(HomeUiState(bestTimes = emptyList()))

        composeRule
            .onNodeWithText("No solves yet. Your times will appear here.")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun bestTimesAreListed() {
        setScreen(
            HomeUiState(
                bestTimes = listOf(BestTime(BoardSize(8), bestMillis = 161_000, solveCount = 3)),
            )
        )

        composeRule.onNodeWithText("8 × 8").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("02:41").performScrollTo().assertIsDisplayed()
    }

    private fun setScreen(
        uiState: HomeUiState,
        onSizeSelected: (BoardSize) -> Unit = {},
        onThemeSelected: (ThemePreference) -> Unit = {},
        onStartGame: (BoardSize) -> Unit = {},
        onResumeGame: (BoardSize) -> Unit = {},
    ) {
        composeRule.setContent {
            QueensTheme {
                HomeScreen(
                    uiState = uiState,
                    onSizeSelected = onSizeSelected,
                    onThemeSelected = onThemeSelected,
                    onStartGame = onStartGame,
                    onResumeGame = onResumeGame,
                    onSeeAllBestTimes = {},
                )
            }
        }
    }
}
