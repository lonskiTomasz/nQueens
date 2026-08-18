package com.queens.puzzle.ui.designsystem.preview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.ui.besttimes.BestTimesScreen
import com.queens.puzzle.ui.game.GameScreen
import com.queens.puzzle.ui.home.HomeScreen
import com.queens.puzzle.ui.win.WinScreen
import com.queens.puzzle.ui.win.WinUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * The previews themselves are `private` and rendered by the IDE, but everything they are made
 * of is here: the sample state and the themed wrapper. A preview that throws is invisible until
 * somebody opens the file, so the sample data is composed for real instead.
 */
class PreviewDataTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theSampleBoardIsTheOneTheDesignIsDrawnWith() {
        val squares = previewSquares(BoardSize(8), PreviewQueens)

        assertEquals(64, squares.size)
        assertEquals(5, squares.count { it.hasQueen })
        // The design's board shows exactly one diagonal conflict, between (3,5) and (4,6).
        assertEquals(
            setOf(Position(3, 5), Position(4, 6)),
            squares.filter { it.isConflicting }.map { it.position }.toSet(),
        )
    }

    @Test
    fun theSampleSolutionActuallySolvesItsBoard() {
        val state = previewGameUiState(boardSize = 4, queens = PreviewSolvedQueens)

        assertTrue(state.isSolved)
        assertTrue(state.conflictKinds.isEmpty())
    }

    @Test
    fun turningAttackLinesOffChangesTheSampleBoard() {
        val on = previewGameUiState(settings = GameSettings(showAttackLines = true))
        val off = previewGameUiState(settings = GameSettings(showAttackLines = false))

        assertTrue(on.squares.any { it.isAttacked })
        assertTrue(off.squares.none { it.isAttacked })
    }

    @Test
    fun theGameSampleComposes() {
        composeRule.setContent {
            QueensPreviewScreen {
                GameScreen(
                    uiState = previewGameUiState(),
                    onAction = {},
                    onNavigateBack = {},
                    onResetRequested = {},
                    onResetConfirmed = {},
                    onResetDismissed = {},
                    onSettingsOpened = {},
                    onSettingsDismissed = {},
                    onShowAttackLinesChanged = {},
                    onHapticsChanged = {},
                )
            }
        }

        composeRule.onNodeWithText("3 queens left").assertIsDisplayed()
        composeRule.onNodeWithText("Queens are attacking each other.").assertIsDisplayed()
        composeRule.onNodeWithText("02:14").assertIsDisplayed()
    }

    @Test
    fun theHomeSampleComposes() {
        composeRule.setContent {
            QueensPreviewScreen {
                HomeScreen(
                    uiState = previewHomeUiState(selectedSize = 10, resumableSize = 6),
                    onSizeSelected = {},
                    onThemeSelected = {},
                    onStartGame = {},
                    onResumeGame = {},
                    onSeeAllBestTimes = {},
                )
            }
        }

        composeRule.onNodeWithText("Start 10 × 10 game").assertIsDisplayed()
        composeRule.onNodeWithText("Resume last board").assertIsDisplayed()
    }

    @Test
    fun theWinSampleComposes() {
        composeRule.setContent {
            QueensPreviewScreen {
                WinScreen(
                    uiState = WinUiState.Solved(previewWinSummary()),
                    onPlay = {},
                    onSeeBestTimes = {},
                    onClose = {},
                )
            }
        }

        composeRule.onNodeWithText("Solved!").assertIsDisplayed()
        composeRule.onNodeWithText("NEW BEST · -54s").assertIsDisplayed()
    }

    @Test
    fun theHistorySampleComposesWithEveryDateBucket() {
        composeRule.setContent {
            QueensPreviewScreen {
                BestTimesScreen(
                    uiState = previewBestTimesUiState(),
                    onFilterSelected = {},
                    onClearHistory = {},
                    onNavigateBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Yesterday").assertIsDisplayed()
        composeRule.onNodeWithText("2 days ago").assertIsDisplayed()
        composeRule.onNodeWithText("Last week").assertIsDisplayed()
        composeRule.onNodeWithText("2 weeks ago").assertIsDisplayed()
    }

    @Test
    fun theLargestBoardSampleComposes() {
        composeRule.setContent {
            QueensPreviewScreen {
                GameScreen(
                    uiState = previewGameUiState(
                        boardSize = 12,
                        queens = setOf(Position(0, 0), Position(2, 5), Position(7, 11)),
                    ),
                    onAction = {},
                    onNavigateBack = {},
                    onResetRequested = {},
                    onResetConfirmed = {},
                    onResetDismissed = {},
                    onSettingsOpened = {},
                    onSettingsDismissed = {},
                    onShowAttackLinesChanged = {},
                    onHapticsChanged = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Row 1, column 1, queen").assertIsDisplayed()
    }
}
