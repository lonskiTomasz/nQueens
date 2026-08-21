package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardEvaluatorTest {

    private val size = BoardSize(8)

    @Test
    fun `empty board has no conflicts and is not solved`() {
        val evaluation = BoardEvaluator.evaluate(size, emptySet())

        assertTrue(evaluation.conflicts.isEmpty())
        assertTrue(evaluation.conflictKinds.isEmpty())
        assertTrue(evaluation.attackedSquares.isEmpty())
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `single queen conflicts with nothing`() {
        val evaluation = BoardEvaluator.evaluate(size, setOf(Position(3, 3)))

        assertTrue(evaluation.conflicts.isEmpty())
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `queens sharing a row are both flagged`() {
        val queens = setOf(Position(2, 1), Position(2, 6))

        val evaluation = BoardEvaluator.evaluate(size, queens)

        assertEquals(queens, evaluation.conflicts)
        assertEquals(setOf(ConflictKind.Row), evaluation.conflictKinds)
    }

    @Test
    fun `queens sharing a column are both flagged`() {
        val queens = setOf(Position(0, 4), Position(7, 4))

        val evaluation = BoardEvaluator.evaluate(size, queens)

        assertEquals(queens, evaluation.conflicts)
        assertEquals(setOf(ConflictKind.Column), evaluation.conflictKinds)
    }

    @Test
    fun `queens sharing a descending diagonal are both flagged`() {
        val queens = setOf(Position(1, 1), Position(4, 4))

        val evaluation = BoardEvaluator.evaluate(size, queens)

        assertEquals(queens, evaluation.conflicts)
        assertEquals(setOf(ConflictKind.Diagonal), evaluation.conflictKinds)
    }

    @Test
    fun `queens sharing an ascending diagonal are both flagged`() {
        val queens = setOf(Position(0, 5), Position(3, 2))

        val evaluation = BoardEvaluator.evaluate(size, queens)

        assertEquals(queens, evaluation.conflicts)
        assertEquals(setOf(ConflictKind.Diagonal), evaluation.conflictKinds)
    }

    @Test
    fun `unrelated queen is not dragged into a conflict`() {
        val clashing = setOf(Position(0, 0), Position(0, 5))
        val innocent = Position(3, 1)

        val evaluation = BoardEvaluator.evaluate(size, clashing + innocent)

        assertEquals(clashing, evaluation.conflicts)
        assertFalse(evaluation.isConflicting(innocent))
    }

    @Test
    fun `a queen may conflict on several lines at once`() {
        val queens = setOf(Position(4, 4), Position(4, 7), Position(1, 1))

        val evaluation = BoardEvaluator.evaluate(size, queens)

        assertEquals(queens, evaluation.conflicts)
        assertEquals(setOf(ConflictKind.Row, ConflictKind.Diagonal), evaluation.conflictKinds)
    }

    @Test
    fun `knight-move neighbours do not conflict`() {
        val queens = setOf(Position(0, 0), Position(1, 2))

        val evaluation = BoardEvaluator.evaluate(size, queens)

        assertTrue(evaluation.conflicts.isEmpty())
    }

    @Test
    fun `full clean board is solved`() {
        val evaluation = BoardEvaluator.evaluate(BoardSize(4), FOUR_QUEENS_SOLUTION)

        assertTrue(evaluation.conflicts.isEmpty())
        assertTrue(evaluation.isSolved)
    }

    @Test
    fun `clean but incomplete board is not solved`() {
        val queens = FOUR_QUEENS_SOLUTION - Position(0, 1)

        val evaluation = BoardEvaluator.evaluate(BoardSize(4), queens)

        assertTrue(evaluation.conflicts.isEmpty())
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `full board with a conflict is not solved`() {
        val queens = setOf(Position(0, 0), Position(1, 1), Position(2, 2), Position(3, 3))

        val evaluation = BoardEvaluator.evaluate(BoardSize(4), queens)

        assertTrue(evaluation.hasConflicts)
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `attacked squares cover the lines through a queen but not the queen itself`() {
        val queen = Position(0, 0)

        val evaluation = BoardEvaluator.evaluate(BoardSize(4), setOf(queen))

        val expected = setOf(
            Position(0, 1), Position(0, 2), Position(0, 3),
            Position(1, 0), Position(2, 0), Position(3, 0),
            Position(1, 1), Position(2, 2), Position(3, 3),
        )
        assertEquals(expected, evaluation.attackedSquares)
        assertFalse(evaluation.isAttacked(queen))
    }

    @Test
    fun `attacked squares stay on the board`() {
        val evaluation = BoardEvaluator.evaluate(size, setOf(Position(0, 7), Position(7, 0)))

        assertTrue(evaluation.attackedSquares.all { it in size })
    }

    @Test
    fun `a solved board leaves every empty square attacked`() {
        val boardSize = BoardSize(4)

        val evaluation = BoardEvaluator.evaluate(boardSize, FOUR_QUEENS_SOLUTION)

        val emptySquares = boardSize.positions().toSet() - FOUR_QUEENS_SOLUTION
        assertEquals(emptySquares, evaluation.attackedSquares)
    }

    @Test
    fun `attacked squares are left out when they are not asked for`() {
        val queens = setOf(Position(0, 0), Position(1, 1))

        val evaluation = BoardEvaluator.evaluate(size, queens, includeAttackedSquares = false)

        assertTrue(evaluation.attackedSquares.isEmpty())
        assertFalse(evaluation.isAttacked(Position(0, 1)))
        assertEquals(queens, evaluation.conflicts)
    }

    @Test
    fun `a solved board is still solved without the attacked squares`() {
        val boardSize = BoardSize(4)

        val evaluation =
            BoardEvaluator.evaluate(boardSize, FOUR_QUEENS_SOLUTION, includeAttackedSquares = false)

        assertTrue(evaluation.isSolved)
        assertTrue(evaluation.attackedSquares.isEmpty())
    }

    private companion object {
        /** The 4x4 solution, in row order: columns 1, 3, 0, 2. */
        val FOUR_QUEENS_SOLUTION = setOf(
            Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2),
        )
    }
}
