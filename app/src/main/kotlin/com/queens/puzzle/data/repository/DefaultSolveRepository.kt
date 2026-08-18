package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.database.SolveDao
import com.queens.puzzle.data.mapper.toEntity
import com.queens.puzzle.data.mapper.toModel
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The solve history, over Room.
 *
 * Ordering and aggregation are the DAO's, so this is a mapping boundary and nothing more.
 * Room already runs its queries off the main thread, so there is no dispatcher to inject.
 */
@Singleton
class DefaultSolveRepository @Inject constructor(
    private val solveDao: SolveDao,
) : SolveRepository {

    override fun observeSolves(): Flow<List<Solve>> =
        solveDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override fun observeSolves(boardSize: BoardSize): Flow<List<Solve>> =
        solveDao.observeForSize(boardSize.value).map { rows -> rows.map { it.toModel() } }

    override suspend fun bestFor(boardSize: BoardSize): Solve? =
        solveDao.bestFor(boardSize.value)?.toModel()

    override suspend fun record(solve: Solve): Long = solveDao.insert(solve.toEntity())

    override fun observeBestTimes(): Flow<List<BestTime>> =
        solveDao.observeBestTimes().map { rows -> rows.map { it.toModel() } }

    override suspend fun clearHistory() = solveDao.clear()
}
