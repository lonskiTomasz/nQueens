package com.queens.puzzle.ui.game

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.ui.board.BoardSquareState
import com.queens.puzzle.ui.designsystem.theme.QueensTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GameScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun theQueensLeftCounterReflectsTheBoard() {
        setScreen(state(boardSize = 4, queensPlaced = 1))

        composeRule.onNodeWithText("3 queens left").assertIsDisplayed()
    }

    @Test
    fun theCounterIsSingularWithOneQueenLeft() {
        setScreen(state(boardSize = 4, queensPlaced = 3))

        composeRule.onNodeWithText("1 queen left").assertIsDisplayed()
    }

    @Test
    fun theClockIsShown() {
        setScreen(state(boardSize = 4, elapsedMillis = 134_000))

        composeRule.onNodeWithText("02:14").assertIsDisplayed()
    }

    @Test
    fun tappingASquareReportsTheAction() {
        val actions = mutableListOf<GameAction>()
        setScreen(state(boardSize = 4), onAction = { actions += it })

        composeRule.onNodeWithContentDescription("Row 1, column 2, empty").performClick()

        assertEquals(listOf(GameAction.TapSquare(Position(0, 1))), actions)
    }

    @Test
    fun theBannerWarnsWhileQueensAreInConflict() {
        setScreen(state(boardSize = 4, conflictKinds = setOf(ConflictKind.Diagonal)))

        composeRule.onNodeWithText("Queens are attacking each other.").assertIsDisplayed()
    }

    /** The board marks which squares are at fault, so the sentence does not change with them. */
    @Test
    fun theBannerSaysTheSameThingWhateverTheQueensShare() {
        setScreen(state(boardSize = 4, conflictKinds = setOf(ConflictKind.Row)))

        composeRule.onNodeWithText("Queens are attacking each other.").assertIsDisplayed()
    }

    @Test
    fun undoIsOffOnAnUntouchedBoard() {
        setScreen(state(boardSize = 4))

        composeRule.onNodeWithText("Undo").assertIsNotEnabled()
        composeRule.onNodeWithText("Reset").assertIsNotEnabled()
    }

    @Test
    fun undoIsOnOnceThereIsSomethingToUndo() {
        setScreen(state(boardSize = 4, queensPlaced = 1, canUndo = true))

        composeRule.onNodeWithText("Undo").assertIsEnabled()
    }

    @Test
    fun theResetDialogAsksBeforeClearing() {
        setScreen(
            state(boardSize = 4, queensPlaced = 2, isResetDialogVisible = true),
        )

        composeRule.onNodeWithText("Reset board?").assertIsDisplayed()
        composeRule
            .onNodeWithText("This clears all 2 queens you’ve placed. It can’t be undone.")
            .assertIsDisplayed()
    }

    @Test
    fun confirmingTheResetDialogReportsIt() {
        var confirmed = false
        setScreen(
            state(boardSize = 4, queensPlaced = 2, isResetDialogVisible = true),
            onResetConfirmed = { confirmed = true },
        )

        // "Reset" is on both the bottom bar and the dialog; the dialog's is the enabled one
        // drawn last, so filter to it rather than picking an index.
        composeRule
            .onAllNodesWithText("Reset")
            .filterToOne(hasAnyAncestor(isDialog()))
            .performClick()

        assertEquals(true, confirmed)
    }

    @Test
    fun cancellingTheResetDialogReportsIt() {
        var dismissed = false
        setScreen(
            state(boardSize = 4, queensPlaced = 2, isResetDialogVisible = true),
            onResetDismissed = { dismissed = true },
        )

        composeRule.onNodeWithText("Cancel").performClick()

        assertEquals(true, dismissed)
    }

    @Test
    fun theSettingsSheetShowsBothToggles() {
        setScreen(state(boardSize = 4, isSettingsSheetVisible = true))

        composeRule.onNodeWithText("Game settings").assertIsDisplayed()
        composeRule.onNodeWithText("Mark attacked squares").assertIsDisplayed()
        composeRule.onNodeWithText("Haptic on place").assertIsDisplayed()
    }

    private fun state(
        boardSize: Int,
        queensPlaced: Int = 0,
        elapsedMillis: Long = 0L,
        conflictKinds: Set<ConflictKind> = emptySet(),
        canUndo: Boolean = false,
        isResetDialogVisible: Boolean = false,
        isSettingsSheetVisible: Boolean = false,
    ): GameUiState {
        val size = BoardSize(boardSize)
        return GameUiState(
            boardSize = size,
            squares = size.positions().map { BoardSquareState(it) },
            queensPlaced = queensPlaced,
            elapsedMillis = elapsedMillis,
            conflictKinds = conflictKinds,
            canUndo = canUndo,
            settings = GameSettings(),
            isResetDialogVisible = isResetDialogVisible,
            isSettingsSheetVisible = isSettingsSheetVisible,
        )
    }

    private fun setScreen(
        uiState: GameUiState,
        onAction: (GameAction) -> Unit = {},
        onResetConfirmed: () -> Unit = {},
        onResetDismissed: () -> Unit = {},
    ) {
        composeRule.setContent {
            QueensTheme {
                GameScreen(
                    uiState = uiState,
                    onAction = onAction,
                    onNavigateBack = {},
                    onResetRequested = {},
                    onResetConfirmed = onResetConfirmed,
                    onResetDismissed = onResetDismissed,
                    onSettingsOpened = {},
                    onSettingsDismissed = {},
                    onShowAttackLinesChanged = {},
                    onHapticsChanged = {},
                )
            }
        }
    }
}
