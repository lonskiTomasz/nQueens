package com.queens.puzzle.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Position
import com.queens.puzzle.testing.ForcedWindow
import com.queens.puzzle.ui.game.board.BoardSquareState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GameScreenLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun everythingFitsInALandscapeWindow() {
        setScreen(DpSize(740.dp, 360.dp))

        composeRule.onNodeWithText("Undo").assertIsDisplayed()
        composeRule.onNodeWithText("Reset").assertIsDisplayed()
        composeRule.onNodeWithText("8 queens left").assertIsDisplayed()
        composeRule.onNodeWithText("00:00").assertIsDisplayed()
    }

    @Test
    fun theWholeBoardFitsInALandscapeWindow() {
        setScreen(DpSize(740.dp, 360.dp))

        // Opposite corners: if the board overflowed, the far one would be off screen.
        composeRule.onNodeWithContentDescription("Row 1, column 1, empty").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Row 8, column 8, empty").assertIsDisplayed()
    }

    @Test
    fun squaresStayTappableInALandscapeWindow() {
        val actions = mutableListOf<GameAction>()
        setScreen(DpSize(740.dp, 360.dp), onAction = { actions += it })

        composeRule.onNodeWithContentDescription("Row 8, column 8, empty").performClick()

        assertEquals(listOf(GameAction.TapSquare(Position(7, 7))), actions)
    }

    @Test
    fun everythingFitsInAShortSquarishWindow() {
        // Wider than it is tall, but with no room for a controls column beside the board.
        setScreen(DpSize(520.dp, 470.dp))

        composeRule.onNodeWithText("Undo").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Row 8, column 8, empty").assertIsDisplayed()
    }

    @Test
    fun everythingStillFitsInThePortraitWindowTheDesignIsDrawnAt() {
        setScreen(DpSize(412.dp, 892.dp))

        composeRule.onNodeWithText("Undo").assertIsDisplayed()
        composeRule.onNodeWithText("8 queens left").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Row 8, column 8, empty").assertIsDisplayed()
    }

    @Test
    fun everythingFitsInAShortPortraitWindow() {
        setScreen(DpSize(320.dp, 560.dp))

        composeRule.onNodeWithText("Undo").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Row 8, column 8, empty").assertIsDisplayed()
    }

    private fun setScreen(size: DpSize, onAction: (GameAction) -> Unit = {}) {
        composeRule.setContent {
            ForcedWindow(size) {
                GameScreen(
                    uiState = uiState(),
                    elapsedMillis = { 0L },
                    onAction = onAction,
                    onNavigateBack = {},
                    onResetRequested = {},
                    onResetConfirmed = {},
                    onResetDismissed = {},
                    onSettingsOpened = {},
                    onSettingsDismissed = {},
                    onShowAttackLinesChanged = {},
                    onHapticsChanged = {},
                    onSoundChanged = {},
                )
            }
        }
    }

    private fun uiState(): GameUiState {
        val size = BoardSize(8)
        return GameUiState(
            boardSize = size,
            squares = size.positions().map { BoardSquareState(it) },
        )
    }
}
