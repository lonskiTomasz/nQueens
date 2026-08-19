package com.queens.puzzle.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
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
import com.queens.puzzle.ui.board.BoardSquareState
import com.queens.puzzle.ui.designsystem.theme.QueensTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The game screen at window shapes the design was never drawn at.
 *
 * A rotated phone used to render a board as wide as the window and therefore taller than it,
 * which pushed the top bar off the top and buried Undo and Reset underneath the squares.
 */
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
                    onAction = onAction,
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
    }

    private fun uiState(): GameUiState {
        val size = BoardSize(8)
        return GameUiState(
            boardSize = size,
            squares = size.positions().map { BoardSquareState(it) },
        )
    }

    @Composable
    private fun ForcedWindow(size: DpSize, content: @Composable () -> Unit) {
        // Scales a window of the given size into the test device, so one device covers every
        // shape rather than needing an emulator per orientation.
        DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size)) {
            QueensTheme { content() }
        }
    }
}
