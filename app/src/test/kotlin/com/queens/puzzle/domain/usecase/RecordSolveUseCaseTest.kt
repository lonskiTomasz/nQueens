package com.queens.puzzle.domain.usecase

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordSolveUseCaseTest {

    private val boardSize = BoardSize(8)
    private val timeProvider = TestTimeProvider()

    @Test
    fun `a first solve of a size is a personal best with no delta`() = runTest {
        val useCase = useCase()

        val outcome = useCase(boardSize, PuzzleType.Queens, durationMillis = 90_000, taps = 27, undos = 2)

        assertTrue(outcome.isNewBest)
        assertNull(outcome.improvementMillis)
    }

    @Test
    fun `beating the previous best reports the improvement`() = runTest {
        val useCase = useCase(existing(durationMillis = 120_000))

        val outcome = useCase(boardSize, PuzzleType.Queens, durationMillis = 66_000, taps = 20, undos = 0)

        assertTrue(outcome.isNewBest)
        assertEquals(54_000L, outcome.improvementMillis)
    }

    @Test
    fun `a slower solve is not a best and reports a negative improvement`() = runTest {
        val useCase = useCase(existing(durationMillis = 60_000))

        val outcome = useCase(boardSize, PuzzleType.Queens, durationMillis = 72_000, taps = 31, undos = 4)

        assertFalse(outcome.isNewBest)
        assertEquals(-12_000L, outcome.improvementMillis)
    }

    @Test
    fun `matching the previous best exactly is not a new best`() = runTest {
        val useCase = useCase(existing(durationMillis = 60_000))

        val outcome = useCase(boardSize, PuzzleType.Queens, durationMillis = 60_000, taps = 18, undos = 0)

        assertFalse(outcome.isNewBest)
        assertEquals(0L, outcome.improvementMillis)
    }

    @Test
    fun `the new solve cannot beat itself`() = runTest {
        val repository = TestSolveRepository()
        val useCase = RecordSolveUseCase(repository, timeProvider)

        useCase(boardSize, PuzzleType.Queens, durationMillis = 90_000, taps = 27, undos = 2)
        val second = useCase(boardSize, PuzzleType.Queens, durationMillis = 90_000, taps = 27, undos = 2)

        // The first run is now stored, so the identical second run ties rather than wins.
        assertFalse(second.isNewBest)
        assertEquals(0L, second.improvementMillis)
    }

    @Test
    fun `a best on another size does not count as the previous best`() = runTest {
        val useCase = useCase(existing(durationMillis = 10_000, boardSize = BoardSize(4)))

        val outcome = useCase(boardSize, PuzzleType.Queens, durationMillis = 90_000, taps = 27, undos = 2)

        assertTrue(outcome.isNewBest)
        assertNull(outcome.improvementMillis)
    }

    @Test
    fun `the solve is stored with its stats and a wall-clock stamp`() = runTest {
        val repository = TestSolveRepository()
        val useCase = RecordSolveUseCase(repository, timeProvider)
        timeProvider.advanceBy(5_000)

        val outcome = useCase(boardSize, PuzzleType.Queens, durationMillis = 90_000, taps = 27, undos = 2)

        val stored = repository.recorded.single()
        assertEquals(boardSize, stored.boardSize)
        assertEquals(90_000L, stored.durationMillis)
        assertEquals(27, stored.taps)
        assertEquals(2, stored.undos)
        assertEquals(TestTimeProvider.FIXED_NOW + 5_000, stored.completedAtMillis)
        assertEquals(stored.id, outcome.solveId)
    }

    private fun useCase(vararg solves: Solve) =
        RecordSolveUseCase(TestSolveRepository(solves.toList()), timeProvider)

    private fun existing(durationMillis: Long, boardSize: BoardSize = this.boardSize) = Solve(
        id = 1L,
        boardSize = boardSize,
        puzzleType = PuzzleType.Queens,
        durationMillis = durationMillis,
        taps = 30,
        undos = 1,
        completedAtMillis = TestTimeProvider.FIXED_NOW - 86_400_000,
    )
}
