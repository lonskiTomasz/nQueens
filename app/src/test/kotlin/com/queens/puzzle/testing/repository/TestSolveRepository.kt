package com.queens.puzzle.testing.repository

import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestSolveRepository(initialSolves: List<Solve> = emptyList()) : SolveRepository {

    private val solves = MutableStateFlow(initialSolves)
    private var nextId = (initialSolves.maxOfOrNull { it.id } ?: 0L) + 1

    /** Everything recorded through [record], in call order. */
    val recorded: List<Solve> get() = _recorded.toList()
    private val _recorded = mutableListOf<Solve>()

    override fun observeSolves(): Flow<List<Solve>> =
        solves.map { list -> list.sortedByDescending { it.completedAtMillis } }

    override fun observeSolves(boardSize: BoardSize): Flow<List<Solve>> =
        observeSolves().map { list -> list.filter { it.boardSize == boardSize } }

    override suspend fun bestFor(boardSize: BoardSize): Solve? =
        solves.value.filter { it.boardSize == boardSize }.minByOrNull { it.durationMillis }

    override suspend fun record(solve: Solve): Long {
        val stored = solve.copy(id = nextId++)
        _recorded += stored
        solves.value += stored
        return stored.id
    }

    override fun observeBestTimes(): Flow<List<BestTime>> = solves.map { list ->
        list.groupBy { it.boardSize }.map { (boardSize, forSize) ->
            BestTime(
                boardSize = boardSize,
                bestMillis = forSize.minOf { it.durationMillis },
                solveCount = forSize.size,
            )
        }
    }

    override suspend fun clearHistory() {
        solves.value = emptyList()
    }
}
