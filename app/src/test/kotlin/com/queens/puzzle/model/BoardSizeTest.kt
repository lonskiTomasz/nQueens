package com.queens.puzzle.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardSizeTest {

    @Test
    fun `sizes below the minimum are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { BoardSize(3) }
    }

    @Test
    fun `sizes above the maximum are rejected`() {
        assertThrows(IllegalArgumentException::class.java) { BoardSize(13) }
    }

    @Test
    fun `the bounds themselves are accepted`() {
        assertEquals(4, BoardSize(BoardSize.MIN).value)
        assertEquals(12, BoardSize(BoardSize.MAX).value)
    }

    @Test
    fun `ofOrNull returns null instead of throwing for stored garbage`() {
        assertNull(BoardSize.ofOrNull(0))
        assertNull(BoardSize.ofOrNull(99))
        assertEquals(BoardSize(6), BoardSize.ofOrNull(6))
    }

    @Test
    fun `positions cover the board once each in row-major order`() {
        val positions = BoardSize(4).positions()

        assertEquals(16, positions.size)
        assertEquals(16, positions.toSet().size)
        assertEquals(Position(0, 0), positions.first())
        assertEquals(Position(0, 1), positions[1])
        assertEquals(Position(3, 3), positions.last())
    }

    @Test
    fun `contains rejects positions off the edge`() {
        val size = BoardSize(4)

        assertTrue(Position(3, 3) in size)
        assertFalse(Position(4, 0) in size)
        assertFalse(Position(0, -1) in size)
    }

    @Test
    fun `next climbs the selectable sizes`() {
        assertEquals(BoardSize(5), BoardSize(4).next)
        assertEquals(BoardSize(10), BoardSize(8).next)
    }

    @Test
    fun `next skips the sizes the home screen does not offer`() {
        // 9 and 11 are playable but not selectable, so the rung above 8 is 10.
        assertEquals(BoardSize(10), BoardSize(9).next)
        assertEquals(BoardSize(12), BoardSize(11).next)
    }

    @Test
    fun `the largest size has nothing above it`() {
        assertNull(BoardSize(BoardSize.MAX).next)
    }

    @Test
    fun `every selectable size is legal`() {
        assertTrue(BoardSize.selectable.all { it.value in BoardSize.MIN..BoardSize.MAX })
    }
}
