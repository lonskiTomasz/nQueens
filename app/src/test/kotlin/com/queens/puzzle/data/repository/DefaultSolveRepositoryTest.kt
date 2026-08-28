package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.database.SolveEntity
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.local.TestSolveDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSolveRepositoryTest {

    private val dao = TestSolveDao()
    private val repository = DefaultSolveRepository(dao)

    @Test
    fun `maps a stored row onto its model`() = runTest {
        dao.seed(entity(boardSize = 8, durationMillis = 92_000, taps = 14, undos = 2, completedAt = 500))

        val solve = repository.observeSolves().first().single()

        assertEquals(BoardSize(8), solve.boardSize)
        assertEquals(92_000L, solve.durationMillis)
        assertEquals(14, solve.taps)
        assertEquals(2, solve.undos)
        assertEquals(500L, solve.completedAtMillis)
        assertTrue("Room assigns the id", solve.id > 0L)
    }

    @Test
    fun `history is newest first`() = runTest {
        dao.seed(
            entity(completedAt = 100),
            entity(completedAt = 300),
            entity(completedAt = 200),
        )

        val completedAt = repository.observeSolves().first().map { it.completedAtMillis }

        assertEquals(listOf(300L, 200L, 100L), completedAt)
    }

    @Test
    fun `history for one size excludes the others`() = runTest {
        dao.seed(
            entity(boardSize = 6, completedAt = 100),
            entity(boardSize = 8, completedAt = 200),
            entity(boardSize = 6, completedAt = 300),
        )

        val sizes = repository.observeSolves(BoardSize(6), PuzzleType.Queens).first().map { it.boardSize }

        assertEquals(listOf(BoardSize(6), BoardSize(6)), sizes)
    }

    @Test
    fun `best for a size is the fastest solve of that size`() = runTest {
        dao.seed(
            entity(boardSize = 8, durationMillis = 90_000),
            entity(boardSize = 8, durationMillis = 40_000),
            entity(boardSize = 6, durationMillis = 10_000),
        )

        assertEquals(40_000L, repository.bestFor(BoardSize(8), PuzzleType.Queens)?.durationMillis)
    }

    @Test
    fun `best for an unplayed size is null`() = runTest {
        dao.seed(entity(boardSize = 8, durationMillis = 90_000))

        assertNull(repository.bestFor(BoardSize(12), PuzzleType.Queens))
    }

    @Test
    fun `best for the other puzzle type at the same size is unaffected`() = runTest {
        dao.seed(entity(boardSize = 8, puzzleType = PuzzleType.Queens, durationMillis = 40_000))

        assertNull(repository.bestFor(BoardSize(8), PuzzleType.Knights))
    }

    @Test
    fun `recording returns the assigned id and adds to the history`() = runTest {
        val id = repository.record(solve(boardSize = 5, durationMillis = 30_000))

        val stored = repository.observeSolves().first().single()
        assertEquals(id, stored.id)
        assertEquals(BoardSize(5), stored.boardSize)
        assertEquals(30_000L, stored.durationMillis)
    }

    @Test
    fun `best times hold the fastest and the count per size`() = runTest {
        dao.seed(
            entity(boardSize = 8, durationMillis = 90_000),
            entity(boardSize = 8, durationMillis = 40_000),
            entity(boardSize = 6, durationMillis = 10_000),
        )

        val bestTimes = repository.observeBestTimes(PuzzleType.Queens).first()

        assertEquals(
            listOf(
                BestTime(BoardSize(6), PuzzleType.Queens, bestMillis = 10_000, solveCount = 1),
                BestTime(BoardSize(8), PuzzleType.Queens, bestMillis = 40_000, solveCount = 2),
            ),
            bestTimes,
        )
    }

    @Test
    fun `best times are empty before anything is solved`() = runTest {
        assertEquals(emptyList<BestTime>(), repository.observeBestTimes(PuzzleType.Queens).first())
    }

    @Test
    fun `best times only cover the requested puzzle type`() = runTest {
        dao.seed(
            entity(boardSize = 8, puzzleType = PuzzleType.Queens, durationMillis = 90_000),
            entity(boardSize = 8, puzzleType = PuzzleType.Knights, durationMillis = 40_000),
        )

        assertEquals(
            listOf(BestTime(BoardSize(8), PuzzleType.Knights, bestMillis = 40_000, solveCount = 1)),
            repository.observeBestTimes(PuzzleType.Knights).first(),
        )
    }

    @Test
    fun `clearing history empties both the history and the bests`() = runTest {
        dao.seed(entity(boardSize = 8, durationMillis = 90_000))

        repository.clearHistory()

        assertEquals(emptyList<Solve>(), repository.observeSolves().first())
        assertEquals(emptyList<BestTime>(), repository.observeBestTimes(PuzzleType.Queens).first())
    }

    private fun entity(
        boardSize: Int = 8,
        puzzleType: PuzzleType = PuzzleType.Queens,
        durationMillis: Long = 60_000,
        taps: Int = 10,
        undos: Int = 0,
        completedAt: Long = 1_000,
    ) = SolveEntity(
        boardSize = boardSize,
        puzzleType = puzzleType.name,
        durationMillis = durationMillis,
        taps = taps,
        undos = undos,
        completedAtMillis = completedAt,
    )

    private fun solve(
        boardSize: Int = 8,
        puzzleType: PuzzleType = PuzzleType.Queens,
        durationMillis: Long = 60_000,
    ) = Solve(
        id = 0L,
        boardSize = BoardSize(boardSize),
        puzzleType = puzzleType,
        durationMillis = durationMillis,
        taps = 10,
        undos = 0,
        completedAtMillis = 1_000,
    )
}
