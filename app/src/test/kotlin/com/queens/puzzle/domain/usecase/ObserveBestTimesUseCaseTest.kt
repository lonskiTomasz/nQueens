package com.queens.puzzle.domain.usecase

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.repository.TestSolveRepository
import com.queens.puzzle.testing.util.TestTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveBestTimesUseCaseTest {

    @Test
    fun `no history yields no best times`() = runTest {
        val bestTimes = ObserveBestTimesUseCase(TestSolveRepository())().first()

        assertTrue(bestTimes.isEmpty())
    }

    @Test
    fun `each size reports its fastest solve and how many times it was solved`() = runTest {
        val repository = TestSolveRepository(
            listOf(
                solve(id = 1, boardSize = 8, durationMillis = 120_000),
                solve(id = 2, boardSize = 8, durationMillis = 66_000),
                solve(id = 3, boardSize = 8, durationMillis = 90_000),
                solve(id = 4, boardSize = 4, durationMillis = 8_000),
            )
        )

        val bestTimes = ObserveBestTimesUseCase(repository)().first()

        val eight = bestTimes.single { it.boardSize == BoardSize(8) }
        assertEquals(66_000L, eight.bestMillis)
        assertEquals(3, eight.solveCount)

        val four = bestTimes.single { it.boardSize == BoardSize(4) }
        assertEquals(8_000L, four.bestMillis)
        assertEquals(1, four.solveCount)
    }

    @Test
    fun `sizes are ordered largest board first`() = runTest {
        val repository = TestSolveRepository(
            listOf(
                solve(id = 1, boardSize = 6, durationMillis = 30_000),
                solve(id = 2, boardSize = 12, durationMillis = 300_000),
                solve(id = 3, boardSize = 4, durationMillis = 8_000),
            )
        )

        val bestTimes = ObserveBestTimesUseCase(repository)().first()

        assertEquals(listOf(12, 6, 4), bestTimes.map { it.boardSize.value })
    }

    @Test
    fun `recording a solve updates the observed bests`() = runTest {
        val repository = TestSolveRepository(listOf(solve(id = 1, boardSize = 8, durationMillis = 90_000)))
        val useCase = ObserveBestTimesUseCase(repository)

        assertEquals(90_000L, useCase().first().single().bestMillis)

        repository.record(solve(id = 0, boardSize = 8, durationMillis = 45_000))

        assertEquals(45_000L, useCase().first().single().bestMillis)
    }

    private fun solve(id: Long, boardSize: Int, durationMillis: Long) = Solve(
        id = id,
        boardSize = BoardSize(boardSize),
        durationMillis = durationMillis,
        taps = 20,
        undos = 0,
        completedAtMillis = TestTimeProvider.FIXED_NOW - id * 1_000,
    )
}
