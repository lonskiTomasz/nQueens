package com.queens.puzzle.ui.win

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.WinSummary
import com.queens.puzzle.testing.ForcedWindow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WinScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theSolveTimeAndBoardAreStated() {
        setScreen(summary(durationMillis = 107_000, taps = 27))

        composeRule.onNodeWithText("Solved!").assertIsDisplayed()
        composeRule.onNodeWithText("01:47").assertIsDisplayed()
        composeRule.onNodeWithText("8 × 8 board · 27 taps").assertIsDisplayed()
    }

    @Test
    fun beatingTheBestIsCelebratedWithTheDelta() {
        setScreen(summary(durationMillis = 107_000, isNewBest = true, improvementMillis = 54_000))

        composeRule.onNodeWithText("NEW BEST · -54s").assertIsDisplayed()
    }

    @Test
    fun aFirstSolveOfASizeIsAPersonalBest() {
        setScreen(summary(durationMillis = 107_000, isNewBest = true, improvementMillis = null))

        composeRule.onNodeWithText("PERSONAL BEST").assertIsDisplayed()
    }

    @Test
    fun aSlowerSolveSaysHowFarOffItWas() {
        setScreen(summary(durationMillis = 161_000, isNewBest = false, improvementMillis = -54_000))

        composeRule.onNodeWithText("+54s off your best").assertIsDisplayed()
    }

    @Test
    fun theStatsAreShown() {
        setScreen(summary(taps = 27, undos = 2, solveCountForSize = 12))

        composeRule.onNodeWithText("27").assertIsDisplayed()
        composeRule.onNodeWithText("2").assertIsDisplayed()
        composeRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun theNextBoardUpIsWhatIsOffered() {
        var played: Int? = null
        setScreen(summary(boardSize = 6), onPlay = { played = it })

        composeRule.onNodeWithText("Play 7 × 7").performClick()

        assertEquals(7, played)
    }

    /** The largest board has nothing above it, so the offer falls back to playing it again. */
    @Test
    fun theLargestBoardIsOfferedAgain() {
        var played: Int? = null
        setScreen(summary(boardSize = 12), onPlay = { played = it })

        composeRule.onNodeWithText("Play again").performClick()

        assertEquals(12, played)
    }

    @Test
    fun theTimesAreOneTapAway() {
        var asked = false
        setScreen(summary(), onSeeBestTimes = { asked = true })

        // On a short window the summary overflows and this sits below the fold, so scroll to
        // it first — the column scrolls exactly so it is still reachable there.
        composeRule.onNodeWithText("See best times").performScrollTo().performClick()

        assertEquals(true, asked)
    }

    @Test
    fun closingLeavesTheScreen() {
        var closed = false
        setScreen(summary(), onClose = { closed = true })

        composeRule.onNodeWithContentDescription("Close").performClick()

        assertEquals(true, closed)
    }

    @Test
    fun theActionsStayOnScreenInALandscapeWindow() {
        // The summary is taller than a rotated phone, so it scrolls — but the way out of the
        // screen must not scroll with it.
        setScreen(summary(), size = DpSize(740.dp, 360.dp))

        composeRule.onNodeWithText("Play 10 × 10").assertIsDisplayed()
        composeRule.onNodeWithText("See best times").assertIsDisplayed()
    }

    private fun summary(
        boardSize: Int = 8,
        durationMillis: Long = 107_000,
        taps: Int = 27,
        undos: Int = 2,
        isNewBest: Boolean = true,
        improvementMillis: Long? = null,
        solveCountForSize: Int = 1,
    ) = WinSummary(
        solve = Solve(
            id = 1L,
            boardSize = BoardSize(boardSize),
            puzzleType = PuzzleType.Queens,
            durationMillis = durationMillis,
            taps = taps,
            undos = undos,
            completedAtMillis = 1_000,
        ),
        isNewBest = isNewBest,
        improvementMillis = improvementMillis,
        solveCountForSize = solveCountForSize,
    )

    private fun setScreen(
        summary: WinSummary,
        onPlay: (Int) -> Unit = {},
        onSeeBestTimes: () -> Unit = {},
        onClose: () -> Unit = {},
        size: DpSize? = null,
    ) {
        composeRule.setContent {
            ForcedWindow(size) {
                WinScreen(
                    uiState = WinUiState.Solved(summary),
                    onPlay = onPlay,
                    onSeeBestTimes = onSeeBestTimes,
                    onClose = onClose,
                )
            }
        }
    }
}
