package com.queens.puzzle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.queens.puzzle.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SolveGameEndToEndTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun solvingAFourByFourBoardReachesTheWinScreen() {
        composeRule.onNodeWithContentDescription("4 × 4 board").performClick()
        composeRule.onNodeWithText("Start 4 × 4 game").performClick()

        composeRule.onNodeWithText("4 queens left").assertIsDisplayed()

        listOf(1 to 2, 2 to 4, 3 to 1, 4 to 3).forEach { (row, column) ->
            composeRule
                .onNodeWithContentDescription("Row $row, column $column, empty")
                .performClick()
        }

        composeRule.onNodeWithText("Solved!").assertIsDisplayed()
        composeRule.onNodeWithText("4 × 4 board · 4 taps").assertIsDisplayed()
    }
}
