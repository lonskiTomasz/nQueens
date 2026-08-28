package com.queens.puzzle.ui.board

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Position
import com.queens.puzzle.core.designsystem.theme.QueensTheme
import com.queens.puzzle.ui.game.board.BoardGrid
import com.queens.puzzle.ui.game.board.BoardSquareState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Squares are selected by their spoken description, never by pixel coordinates. */
class BoardGridTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun everySquareIsReachableByItsSpokenPosition() {
        setBoard(BoardSize(4), emptyList())

        composeRule.onNodeWithContentDescription("Row 1, column 1, empty").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Row 4, column 4, empty").assertIsDisplayed()
    }

    @Test
    fun tappingASquareReportsItsPosition() {
        val tapped = mutableListOf<Position>()
        setBoard(BoardSize(4), emptyList(), onSquareClick = { tapped += it })

        composeRule.onNodeWithContentDescription("Row 2, column 3, empty").performClick()

        assertEquals(listOf(Position(1, 2)), tapped)
    }

    @Test
    fun aQueenIsAnnouncedOnItsSquare() {
        setBoard(
            BoardSize(4),
            listOf(BoardSquareState(Position(0, 0), hasPiece = true)),
        )

        composeRule.onNodeWithContentDescription("Row 1, column 1, queen").assertIsDisplayed()
    }

    @Test
    fun aConflictingQueenSaysSo() {
        setBoard(
            BoardSize(4),
            listOf(BoardSquareState(Position(0, 0), hasPiece = true, isConflicting = true)),
        )

        composeRule
            .onNodeWithContentDescription("Row 1, column 1, queen, in conflict")
            .assertIsDisplayed()
    }

    @Test
    fun anAttackedSquareSaysSo() {
        setBoard(
            BoardSize(4),
            listOf(BoardSquareState(Position(1, 1), isAttacked = true)),
        )

        composeRule
            .onNodeWithContentDescription("Row 2, column 2, empty, under attack")
            .assertIsDisplayed()
    }

    private fun setBoard(
        boardSize: BoardSize,
        squares: List<BoardSquareState>,
        onSquareClick: (Position) -> Unit = {},
    ) {
        composeRule.setContent {
            QueensTheme {
                BoardGrid(
                    boardSize = boardSize,
                    squares = squares,
                    onSquareClick = onSquareClick,
                )
            }
        }
    }
}
