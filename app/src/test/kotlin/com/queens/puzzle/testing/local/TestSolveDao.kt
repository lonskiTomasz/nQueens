package com.queens.puzzle.testing.local

import com.queens.puzzle.data.local.database.BestTimeRow
import com.queens.puzzle.data.local.database.SolveDao
import com.queens.puzzle.data.local.database.SolveEntity
import com.queens.puzzle.data.local.database.SolveWithSizeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class TestSolveDao : SolveDao {

    private val rows = MutableStateFlow<List<SolveEntity>>(emptyList())
    private var nextId = 1L

    /** Inserts [entities] as if recorded in argument order, assigning ids. */
    fun seed(vararg entities: SolveEntity) {
        entities.forEach { rows.value += it.copy(id = nextId++) }
    }

    override fun observeAll(): Flow<List<SolveEntity>> =
        rows.map { list -> list.sortedByDescending { it.completedAtMillis } }

    override fun observeForSize(boardSize: Int, puzzleType: String): Flow<List<SolveEntity>> =
        observeAll().map { list ->
            list.filter { it.boardSize == boardSize && it.puzzleType == puzzleType }
        }

    override suspend fun bestFor(boardSize: Int, puzzleType: String): SolveEntity? =
        rows.value
            .filter { it.boardSize == boardSize && it.puzzleType == puzzleType }
            .minByOrNull { it.durationMillis }

    override suspend fun getWithSizeSummary(id: Long): SolveWithSizeSummary? {
        val solve = rows.value.firstOrNull { it.id == id } ?: return null
        val forSize = rows.value.filter {
            it.boardSize == solve.boardSize && it.puzzleType == solve.puzzleType
        }
        return SolveWithSizeSummary(
            solve = solve,
            solveCount = forSize.size,
            bestMillisExcludingSelf = forSize
                .filter { it.id != id }
                .minOfOrNull { it.durationMillis },
        )
    }

    override fun observeBestTimes(puzzleType: String): Flow<List<BestTimeRow>> = rows.map { list ->
        list.filter { it.puzzleType == puzzleType }
            .groupBy { it.boardSize }
            .map { (boardSize, forSize) ->
                BestTimeRow(
                    boardSize = boardSize,
                    puzzleType = puzzleType,
                    bestMillis = forSize.minOf { it.durationMillis },
                    solveCount = forSize.size,
                )
            }
            .sortedBy { it.boardSize }
    }

    override suspend fun insert(solve: SolveEntity): Long {
        val stored = solve.copy(id = nextId++)
        rows.value += stored
        return stored.id
    }

    override suspend fun clear() {
        rows.value = emptyList()
    }
}
