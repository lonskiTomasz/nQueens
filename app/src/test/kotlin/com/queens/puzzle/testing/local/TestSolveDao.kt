package com.queens.puzzle.testing.local

import com.queens.puzzle.data.local.database.BestTimeRow
import com.queens.puzzle.data.local.database.SolveDao
import com.queens.puzzle.data.local.database.SolveEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * An in-memory [SolveDao].
 *
 * Reproduces the ordering and aggregation the real queries perform, so a repository test
 * reads the same values it would against Room without needing a device.
 */
class TestSolveDao : SolveDao {

    private val rows = MutableStateFlow<List<SolveEntity>>(emptyList())
    private var nextId = 1L

    /** Inserts [entities] as if recorded in argument order, assigning ids. */
    fun seed(vararg entities: SolveEntity) {
        entities.forEach { rows.value = rows.value + it.copy(id = nextId++) }
    }

    override fun observeAll(): Flow<List<SolveEntity>> =
        rows.map { list -> list.sortedByDescending { it.completedAtMillis } }

    override fun observeForSize(boardSize: Int): Flow<List<SolveEntity>> =
        observeAll().map { list -> list.filter { it.boardSize == boardSize } }

    override suspend fun bestFor(boardSize: Int): SolveEntity? =
        rows.value.filter { it.boardSize == boardSize }.minByOrNull { it.durationMillis }

    override fun observeBestTimes(): Flow<List<BestTimeRow>> = rows.map { list ->
        list.groupBy { it.boardSize }
            .map { (boardSize, forSize) ->
                BestTimeRow(
                    boardSize = boardSize,
                    bestMillis = forSize.minOf { it.durationMillis },
                    solveCount = forSize.size,
                )
            }
            .sortedBy { it.boardSize }
    }

    override suspend fun insert(solve: SolveEntity): Long {
        val stored = solve.copy(id = nextId++)
        rows.value = rows.value + stored
        return stored.id
    }

    override suspend fun clear() {
        rows.value = emptyList()
    }
}
