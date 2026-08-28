package com.queens.puzzle.data.local.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.queens.puzzle.model.PuzzleType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SolveDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var solveDao: SolveDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        solveDao = database.solveDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun anIdWithNothingBehindItHasNoSummary() = runBlocking {
        solveDao.insert(solve(boardSize = 8, durationMillis = 100_000))

        assertNull(solveDao.getWithSizeSummary(999L))
    }

    @Test
    fun theOnlySolveOfItsSizeIsNotComparedAgainstItself() = runBlocking {
        val id = solveDao.insert(solve(boardSize = 8, durationMillis = 100_000))

        val summary = solveDao.getWithSizeSummary(id)!!

        assertEquals(1, summary.solveCount)
        assertNull(summary.bestMillisExcludingSelf)
    }

    @Test
    fun aFasterSolveIsComparedAgainstTheOneItBeat() = runBlocking {
        solveDao.insert(solve(boardSize = 8, durationMillis = 160_000))
        val id = solveDao.insert(solve(boardSize = 8, durationMillis = 106_000))

        val summary = solveDao.getWithSizeSummary(id)!!

        assertEquals(2, summary.solveCount)
        assertEquals(160_000L, summary.bestMillisExcludingSelf)
    }

    @Test
    fun aSlowerSolveIsComparedAgainstTheStandingBest() = runBlocking {
        solveDao.insert(solve(boardSize = 8, durationMillis = 100_000))
        val id = solveDao.insert(solve(boardSize = 8, durationMillis = 111_000))

        val summary = solveDao.getWithSizeSummary(id)!!

        assertEquals(2, summary.solveCount)
        assertEquals(100_000L, summary.bestMillisExcludingSelf)
    }

    @Test
    fun solvesOfOtherSizesAreNotCountedOrCompared() = runBlocking {
        solveDao.insert(solve(boardSize = 4, durationMillis = 5_000))
        solveDao.insert(solve(boardSize = 12, durationMillis = 9_000))
        val id = solveDao.insert(solve(boardSize = 8, durationMillis = 100_000))

        val summary = solveDao.getWithSizeSummary(id)!!

        assertEquals(1, summary.solveCount)
        assertNull(summary.bestMillisExcludingSelf)
    }

    /** Matching the standing best is not beating it — the win screen's verdict is a strict `<`. */
    @Test
    fun anExactTieStillReportsTheOtherSolveAsTheBest() = runBlocking {
        solveDao.insert(solve(boardSize = 8, durationMillis = 100_000))
        val id = solveDao.insert(solve(boardSize = 8, durationMillis = 100_000))

        val summary = solveDao.getWithSizeSummary(id)!!

        assertEquals(100_000L, summary.bestMillisExcludingSelf)
        assertFalse(summary.solve.durationMillis < summary.bestMillisExcludingSelf!!)
    }

    @Test
    fun theSolveItselfComesBackAlongsideTheAggregation() = runBlocking {
        val id = solveDao.insert(
            solve(boardSize = 8, durationMillis = 107_000, taps = 27, undos = 2)
        )

        val summary = solveDao.getWithSizeSummary(id)!!

        assertEquals(id, summary.solve.id)
        assertEquals(8, summary.solve.boardSize)
        assertEquals(107_000L, summary.solve.durationMillis)
        assertEquals(27, summary.solve.taps)
        assertEquals(2, summary.solve.undos)
    }

    private fun solve(
        boardSize: Int,
        durationMillis: Long,
        taps: Int = 20,
        undos: Int = 1,
    ) = SolveEntity(
        boardSize = boardSize,
        puzzleType = PuzzleType.Queens.name,
        durationMillis = durationMillis,
        taps = taps,
        undos = undos,
        completedAtMillis = 1_000,
    )
}
