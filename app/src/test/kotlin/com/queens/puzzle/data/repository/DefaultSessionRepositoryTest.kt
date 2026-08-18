package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.datastore.SavedMove
import com.queens.puzzle.data.local.datastore.SavedMoveKind
import com.queens.puzzle.data.local.datastore.SavedPosition
import com.queens.puzzle.data.local.datastore.SavedSession
import com.queens.puzzle.data.local.datastore.SessionDataSource
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.Move
import com.queens.puzzle.model.Position
import com.queens.puzzle.testing.local.InMemoryDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DefaultSessionRepositoryTest {

    @Test
    fun `nothing is saved before a game is played`() = runTest {
        assertNull(repositoryOver(null).observeSavedSession().first())
    }

    @Test
    fun `a saved game round-trips whole`() = runTest {
        val repository = repositoryOver(null)
        val session = GameSession(
            boardSize = BoardSize(6),
            queens = setOf(Position(0, 1), Position(2, 3)),
            moves = listOf(Move.Place(Position(0, 1)), Move.Place(Position(2, 3))),
            taps = 5,
            undos = 1,
        )

        repository.save(session, elapsedMillis = 42_000)

        assertEquals(
            SavedGame(session = session, elapsedMillis = 42_000),
            repository.observeSavedSession().first(),
        )
    }

    @Test
    fun `the undo stack survives a resume`() = runTest {
        val repository = repositoryOver(null)
        val moves = listOf(
            Move.Place(Position(0, 1)),
            Move.Place(Position(2, 3)),
            Move.Remove(Position(2, 3)),
        )

        repository.save(
            GameSession(BoardSize(6), queens = setOf(Position(0, 1)), moves = moves),
            elapsedMillis = 0,
        )

        val resumed = repository.observeSavedSession().first()!!.session
        assertEquals(moves, resumed.moves)
        assertEquals(true, resumed.canUndo)
    }

    @Test
    fun `saving replaces the previous game rather than accumulating`() = runTest {
        val repository = repositoryOver(null)

        repository.save(GameSession(BoardSize(4)), elapsedMillis = 1_000)
        repository.save(GameSession(BoardSize(8)), elapsedMillis = 2_000)

        val saved = repository.observeSavedSession().first()!!
        assertEquals(BoardSize(8), saved.session.boardSize)
        assertEquals(2_000L, saved.elapsedMillis)
    }

    @Test
    fun `clearing leaves nothing to resume`() = runTest {
        val repository = repositoryOver(null)
        repository.save(GameSession(BoardSize(8)), elapsedMillis = 1_000)

        repository.clear()

        assertNull(repository.observeSavedSession().first())
    }

    @Test
    fun `a stored board size outside the supported range reads as nothing saved`() = runTest {
        val repository = repositoryOver(savedSession(boardSize = 99))

        assertNull(repository.observeSavedSession().first())
    }

    @Test
    fun `a stored queen off the board reads as nothing saved`() = runTest {
        val repository = repositoryOver(
            savedSession(boardSize = 4, queens = listOf(SavedPosition(9, 9)))
        )

        assertNull(repository.observeSavedSession().first())
    }

    @Test
    fun `more stored queens than the board allows reads as nothing saved`() = runTest {
        val repository = repositoryOver(
            savedSession(
                boardSize = 4,
                queens = (0 until 4).flatMap { row -> (0 until 2).map { SavedPosition(row, it) } },
            )
        )

        assertNull(repository.observeSavedSession().first())
    }

    private fun savedSession(
        boardSize: Int,
        queens: List<SavedPosition> = emptyList(),
        moves: List<SavedMove> = queens.map { SavedMove(SavedMoveKind.Place, it) },
    ) = SavedSession(
        boardSize = boardSize,
        queens = queens,
        moves = moves,
        taps = queens.size,
        undos = 0,
        elapsedMillis = 1_000,
    )

    private fun repositoryOver(stored: SavedSession?) =
        DefaultSessionRepository(SessionDataSource(InMemoryDataStore(stored)))
}
