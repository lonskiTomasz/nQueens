package com.queens.puzzle.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PositionTest {

    @Test
    fun `a position does not attack itself`() {
        assertFalse(Position(2, 2).attacks(Position(2, 2)))
    }

    @Test
    fun `attacks along row column and both diagonals`() {
        val queen = Position(4, 4)

        assertTrue(queen.attacks(Position(4, 0)))
        assertTrue(queen.attacks(Position(0, 4)))
        assertTrue(queen.attacks(Position(6, 6)))
        assertTrue(queen.attacks(Position(2, 6)))
    }

    @Test
    fun `does not attack a knight move away`() {
        assertFalse(Position(4, 4).attacks(Position(6, 5)))
    }

    @Test
    fun `attacking is symmetric`() {
        val a = Position(1, 1)
        val b = Position(5, 5)

        assertTrue(a.attacks(b))
        assertTrue(b.attacks(a))
    }
}
