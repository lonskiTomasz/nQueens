package com.queens.puzzle.domain.game

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.Move
import com.queens.puzzle.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameReducerTest {

    private val session = GameSession(BoardSize(4))

    @Test
    fun `tapping an empty square places a queen`() {
        val result = session.reduce(tap(1, 2))

        assertEquals(setOf(Position(1, 2)), result.queens)
        assertEquals(listOf(Move.Place(Position(1, 2))), result.moves)
        assertEquals(1, result.taps)
    }

    @Test
    fun `tapping an occupied square removes the queen`() {
        val result = session
            .reduce(tap(1, 2))
            .reduce(tap(1, 2))

        assertTrue(result.queens.isEmpty())
        assertEquals(listOf(Move.Place(Position(1, 2)), Move.Remove(Position(1, 2))), result.moves)
        assertEquals(2, result.taps)
    }

    @Test
    fun `placing is refused once the board is full`() {
        val full = fillBoard()

        val result = full.reduce(tap(3, 3))

        assertSame(full, result)
        assertEquals(4, result.queens.size)
        assertEquals(4, result.taps)
    }

    @Test
    fun `removing still works on a full board`() {
        val full = fillBoard()

        val result = full.reduce(tap(0, 1))

        assertEquals(3, result.queens.size)
        assertEquals(1, result.queensRemaining)
    }

    @Test
    fun `queens remaining never goes negative`() {
        var result = fillBoard()
        repeat(5) { result = result.reduce(tap(3, 3)) }

        assertEquals(0, result.queensRemaining)
    }

    @Test
    fun `a tap outside the board changes nothing`() {
        val result = session.reduce(tap(4, 0))

        assertSame(session, result)
        assertEquals(0, result.taps)
    }

    @Test
    fun `undo unwinds exactly one placement`() {
        val result = session
            .reduce(tap(0, 0))
            .reduce(tap(1, 2))
            .reduce(GameAction.Undo)

        assertEquals(setOf(Position(0, 0)), result.queens)
        assertEquals(listOf(Move.Place(Position(0, 0))), result.moves)
        assertEquals(1, result.undos)
    }

    @Test
    fun `undo restores a removed queen`() {
        val result = session
            .reduce(tap(0, 0))
            .reduce(tap(0, 0))
            .reduce(GameAction.Undo)

        assertEquals(setOf(Position(0, 0)), result.queens)
        assertEquals(1, result.undos)
    }

    @Test
    fun `undo past the start of the game is a no-op`() {
        val result = session.reduce(GameAction.Undo)

        assertSame(session, result)
        assertEquals(0, result.undos)
    }

    @Test
    fun `undo cannot itself be undone`() {
        val result = session
            .reduce(tap(0, 0))
            .reduce(GameAction.Undo)
            .reduce(GameAction.Undo)

        assertTrue(result.queens.isEmpty())
        assertFalse(result.canUndo)
        assertEquals(1, result.undos)
    }

    @Test
    fun `undoing every move returns the board to empty`() {
        val played = session
            .reduce(tap(0, 1))
            .reduce(tap(1, 3))
            .reduce(tap(0, 1))
            .reduce(tap(2, 0))

        val rewound = generateSequence(played) { it.reduce(GameAction.Undo) }
            .first { !it.canUndo }

        assertEquals(session.queens, rewound.queens)
        assertEquals(4, rewound.taps)
        assertEquals(4, rewound.undos)
    }

    @Test
    fun `reset clears queens moves and counters but keeps the size`() {
        val result = session
            .reduce(tap(0, 0))
            .reduce(GameAction.Undo)
            .reduce(tap(2, 2))
            .reduce(GameAction.Reset)

        assertEquals(GameSession(BoardSize(4)), result)
        assertTrue(result.isPristine)
        assertEquals(0, result.taps)
        assertEquals(0, result.undos)
        assertEquals(BoardSize(4), result.boardSize)
    }

    @Test
    fun `reset on an untouched board changes nothing observable`() {
        assertEquals(session, session.reduce(GameAction.Reset))
    }

    @Test
    fun `a fresh session is pristine and cannot undo`() {
        assertTrue(session.isPristine)
        assertFalse(session.canUndo)
        assertEquals(4, session.queensRemaining)
    }

    @Test
    fun `conflicting placements are allowed - the evaluator judges them, not the reducer`() {
        val result = session
            .reduce(tap(0, 0))
            .reduce(tap(0, 1))

        assertEquals(setOf(Position(0, 0), Position(0, 1)), result.queens)
    }

    private fun tap(row: Int, column: Int) = GameAction.TapSquare(Position(row, column))

    private fun fillBoard(): GameSession = session
        .reduce(tap(0, 1))
        .reduce(tap(1, 3))
        .reduce(tap(2, 0))
        .reduce(tap(3, 2))
}
