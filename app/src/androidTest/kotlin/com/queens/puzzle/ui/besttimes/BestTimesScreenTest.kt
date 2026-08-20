package com.queens.puzzle.ui.besttimes

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.queens.puzzle.core.util.time.RelativeDay
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.ForcedWindow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BestTimesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theHistoryListsItsSolves() {
        setScreen(state(filters = listOf(4)))

        composeRule.onNodeWithText("01:47").assertIsDisplayed()
    }

    /**
     * The filter bar outgrows a phone's width once a few sizes have been solved. Every one of
     * them still has to be selectable — this fails outright if the bar stops scrolling, since
     * `performScrollTo` needs a scrollable ancestor to work with.
     */
    @Test
    fun everySolvedSizeCanBeFilteredToOnANarrowScreen() {
        var selected: BoardSize? = null
        setScreen(
            state(filters = listOf(4, 5, 6, 8)),
            onFilterSelected = { selected = it },
            size = DpSize(360.dp, 640.dp),
        )

        composeRule.onNodeWithText("8 × 8").performScrollTo().performClick()

        assertEquals(BoardSize(8), selected)
    }

    /** Only 4x4 is solved, so "8 × 8" belongs to the chip and not to a row. */
    private fun state(filters: List<Int>) = BestTimesUiState(
        filters = filters.map(::BoardSize),
        selectedFilter = null,
        rows = listOf(
            SolveRow(
                solve = Solve(
                    id = 1L,
                    boardSize = BoardSize(4),
                    durationMillis = 107_000,
                    taps = 27,
                    undos = 2,
                    completedAtMillis = 1_000,
                ),
                isBestForSize = true,
                deltaMillis = null,
                occurred = RelativeDay.Yesterday,
            ),
        ),
    )

    private fun setScreen(
        uiState: BestTimesUiState,
        onFilterSelected: (BoardSize?) -> Unit = {},
        size: DpSize? = null,
    ) {
        composeRule.setContent {
            ForcedWindow(size) {
                BestTimesScreen(
                    uiState = uiState,
                    onFilterSelected = onFilterSelected,
                    onClearHistory = {},
                    onNavigateBack = {},
                )
            }
        }
    }
}
