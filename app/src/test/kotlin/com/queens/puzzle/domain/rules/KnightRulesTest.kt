package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KnightRulesTest {

    private val size = BoardSize(8)

    @Test
    fun `empty board has no conflicts and is not solved`() {
        val evaluation = KnightRules.evaluate(size, emptySet())

        assertTrue(evaluation.conflicts.isEmpty())
        assertTrue(evaluation.conflictKinds.isEmpty())
        assertTrue(evaluation.attackedSquares.isEmpty())
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `single knight conflicts with nothing`() {
        val evaluation = KnightRules.evaluate(size, setOf(Position(3, 3)))

        assertTrue(evaluation.conflicts.isEmpty())
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `knights a knight's move apart are both flagged`() {
        val knights = setOf(Position(3, 3), Position(4, 5))

        val evaluation = KnightRules.evaluate(size, knights)

        assertEquals(knights, evaluation.conflicts)
        assertEquals(setOf(ConflictKind.Knight), evaluation.conflictKinds)
    }

    @Test
    fun `knights sharing a row do not conflict`() {
        val knights = setOf(Position(3, 3), Position(3, 7))

        val evaluation = KnightRules.evaluate(size, knights)

        assertTrue(evaluation.conflicts.isEmpty())
        assertTrue(evaluation.conflictKinds.isEmpty())
    }

    @Test
    fun `knights sharing a column do not conflict`() {
        val knights = setOf(Position(0, 4), Position(7, 4))

        val evaluation = KnightRules.evaluate(size, knights)

        assertTrue(evaluation.conflicts.isEmpty())
    }

    @Test
    fun `knights sharing a diagonal do not conflict`() {
        val knights = setOf(Position(1, 1), Position(4, 4))

        val evaluation = KnightRules.evaluate(size, knights)

        assertTrue(evaluation.conflicts.isEmpty())
    }

    @Test
    fun `knights adjacent to each other do not conflict`() {
        val knights = setOf(Position(3, 3), Position(3, 4))

        val evaluation = KnightRules.evaluate(size, knights)

        assertTrue(evaluation.conflicts.isEmpty())
    }

    @Test
    fun `unrelated knight is not dragged into a conflict`() {
        val clashing = setOf(Position(0, 0), Position(1, 2))
        val innocent = Position(5, 5)

        val evaluation = KnightRules.evaluate(size, clashing + innocent)

        assertEquals(clashing, evaluation.conflicts)
        assertFalse(evaluation.isConflicting(innocent))
    }

    @Test
    fun `a knight may conflict with several others at once`() {
        // (3,3) is a knight's move from both (1,2) and (5,4); those two are not a knight's
        // move from each other, so only (3,3) picks up two attackers.
        val knights = setOf(Position(3, 3), Position(1, 2), Position(5, 4))

        val evaluation = KnightRules.evaluate(size, knights)

        assertEquals(knights, evaluation.conflicts)
    }

    @Test
    fun `full clean board is solved`() {
        // All four squares share the same colour (row + column even), and same-colour squares
        // never attack each other in a knight's move, so this board is conflict-free by
        // construction.
        val evaluation = KnightRules.evaluate(BoardSize(4), FOUR_KNIGHTS_SOLUTION)

        assertTrue(evaluation.conflicts.isEmpty())
        assertTrue(evaluation.isSolved)
    }

    @Test
    fun `clean but incomplete board is not solved`() {
        val knights = FOUR_KNIGHTS_SOLUTION - Position(0, 0)

        val evaluation = KnightRules.evaluate(BoardSize(4), knights)

        assertTrue(evaluation.conflicts.isEmpty())
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `full board with a conflict is not solved`() {
        val knights = FOUR_KNIGHTS_SOLUTION - Position(0, 2) + Position(1, 2)

        val evaluation = KnightRules.evaluate(BoardSize(4), knights)

        assertTrue(evaluation.hasConflicts)
        assertFalse(evaluation.isSolved)
    }

    @Test
    fun `attacked squares are every knight's-move square from a corner`() {
        val evaluation = KnightRules.evaluate(size, setOf(Position(0, 0)))

        assertEquals(setOf(Position(1, 2), Position(2, 1)), evaluation.attackedSquares)
    }

    @Test
    fun `attacked squares cover all eight destinations from an interior square`() {
        val evaluation = KnightRules.evaluate(size, setOf(Position(4, 4)))

        val expected = setOf(
            Position(5, 6), Position(6, 5), Position(3, 6), Position(2, 5),
            Position(5, 2), Position(6, 3), Position(3, 2), Position(2, 3),
        )
        assertEquals(expected, evaluation.attackedSquares)
    }

    @Test
    fun `attacked squares exclude squares a knight sits on`() {
        val knights = setOf(Position(0, 0), Position(1, 2))

        val evaluation = KnightRules.evaluate(size, knights)

        assertFalse(evaluation.isAttacked(Position(0, 0)))
        assertFalse(evaluation.isAttacked(Position(1, 2)))
    }

    @Test
    fun `attacked squares stay on the board`() {
        val evaluation = KnightRules.evaluate(size, setOf(Position(0, 7), Position(7, 0)))

        assertTrue(evaluation.attackedSquares.all { it in size })
    }

    @Test
    fun `attacked squares are left out when they are not asked for`() {
        val knights = setOf(Position(3, 3), Position(4, 5))

        val evaluation = KnightRules.evaluate(size, knights, includeAttackedSquares = false)

        assertTrue(evaluation.attackedSquares.isEmpty())
        assertFalse(evaluation.isAttacked(Position(5, 6)))
        assertEquals(knights, evaluation.conflicts)
    }

    @Test
    fun `a solved board is still solved without the attacked squares`() {
        val evaluation =
            KnightRules.evaluate(BoardSize(4), FOUR_KNIGHTS_SOLUTION, includeAttackedSquares = false)

        assertTrue(evaluation.isSolved)
        assertTrue(evaluation.attackedSquares.isEmpty())
    }

    private companion object {
        /** Four same-colour squares on a 4x4 board — mutually non-attacking by construction. */
        val FOUR_KNIGHTS_SOLUTION = setOf(
            Position(0, 0), Position(0, 2), Position(2, 0), Position(2, 2),
        )
    }
}
