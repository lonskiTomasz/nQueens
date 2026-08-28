package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.database.SolveDao
import com.queens.puzzle.data.mapper.toEntity
import com.queens.puzzle.data.mapper.toModel
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.SolveSizeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultSolveRepository @Inject constructor(
    private val solveDao: SolveDao,
) : SolveRepository {

    override fun observeSolves(): Flow<List<Solve>> =
        solveDao.observeAll().map { rows -> rows.map { it.toModel() } }

    override fun observeSolves(boardSize: BoardSize, puzzleType: PuzzleType): Flow<List<Solve>> =
        solveDao.observeForSize(boardSize.value, puzzleType.name)
            .map { rows -> rows.map { it.toModel() } }

    override suspend fun bestFor(boardSize: BoardSize, puzzleType: PuzzleType): Solve? =
        solveDao.bestFor(boardSize.value, puzzleType.name)?.toModel()

    override suspend fun solveSizeSummaryFor(id: Long): SolveSizeSummary? =
        solveDao.getWithSizeSummary(id)?.toModel()

    override suspend fun record(solve: Solve): Long = solveDao.insert(solve.toEntity())

    override fun observeBestTimes(puzzleType: PuzzleType): Flow<List<BestTime>> =
        solveDao.observeBestTimes(puzzleType.name).map { rows -> rows.map { it.toModel() } }

    override suspend fun clearHistory() = solveDao.clear()
}
