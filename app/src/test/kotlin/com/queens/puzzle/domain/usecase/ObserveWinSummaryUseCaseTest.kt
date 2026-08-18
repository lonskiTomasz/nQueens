package com.queens.puzzle.domain.usecase

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import com.queens.puzzle.testing.repository.TestSolveRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveWinSummaryUseCaseTest {

    private val solveRepository = TestSolveRepository()
    private val observeWinSummary = ObserveWinSummaryUseCase(solveRepository)

    @Test
    fun `a first solve of a size is a personal best with no delta`() = runTest {
        val id = solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))

        val summary = observeWinSummary(id).first()!!

        assertTrue(summary.isNewBest)
        assertNull(summary.improvementMillis)
        assertEquals(1, summary.solveCountForSize)
    }

    @Test
    fun `beating the previous best reports the improvement`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 160_000))
        val id = solveRepository.record(solve(boardSize = 8, durationMillis = 106_000))

        val summary = observeWinSummary(id).first()!!

        assertTrue(summary.isNewBest)
        assertEquals(54_000L, summary.improvementMillis)
        assertEquals(2, summary.solveCountForSize)
    }

    @Test
    fun `a slower solve is not a best and reports a negative delta`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))
        val id = solveRepository.record(solve(boardSize = 8, durationMillis = 111_000))

        val summary = observeWinSummary(id).first()!!

        assertFalse(summary.isNewBest)
        assertEquals(-11_000L, summary.improvementMillis)
    }

    @Test
    fun `a solve is never compared against itself`() = runTest {
        val id = solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))

        val summary = observeWinSummary(id).first()!!

        assertTrue(summary.isNewBest)
        assertNull(summary.improvementMillis)
    }

    @Test
    fun `other board sizes do not affect the comparison`() = runTest {
        solveRepository.record(solve(boardSize = 4, durationMillis = 5_000))
        val id = solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))

        val summary = observeWinSummary(id).first()!!

        assertTrue(summary.isNewBest)
        assertNull(summary.improvementMillis)
        assertEquals(1, summary.solveCountForSize)
    }

    @Test
    fun `an unknown id has no summary`() = runTest {
        solveRepository.record(solve(boardSize = 8, durationMillis = 100_000))

        assertNull(observeWinSummary(999L).first())
    }

    private fun solve(boardSize: Int, durationMillis: Long) = Solve(
        id = 0L,
        boardSize = BoardSize(boardSize),
        durationMillis = durationMillis,
        taps = 20,
        undos = 1,
        completedAtMillis = 1_000,
    )
}
