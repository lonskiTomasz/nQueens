package com.queens.puzzle.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Reads and writes of the solve history.
 *
 * Aggregation happens in SQL rather than in Kotlin: the database already has the index to
 * answer it, and the alternative is loading every solve to fold it in memory.
 */
@Dao
interface SolveDao {

    @Query("SELECT * FROM solves ORDER BY completed_at DESC")
    fun observeAll(): Flow<List<SolveEntity>>

    @Query("SELECT * FROM solves WHERE board_size = :boardSize ORDER BY completed_at DESC")
    fun observeForSize(boardSize: Int): Flow<List<SolveEntity>>

    /** The fastest solve for one size, or null when it has never been solved. */
    @Query("SELECT * FROM solves WHERE board_size = :boardSize ORDER BY duration_millis ASC LIMIT 1")
    suspend fun bestFor(boardSize: Int): SolveEntity?

    @Query(
        """
        SELECT board_size,
               MIN(duration_millis) AS best_millis,
               COUNT(*) AS solve_count
        FROM solves
        GROUP BY board_size
        ORDER BY board_size ASC
        """
    )
    fun observeBestTimes(): Flow<List<BestTimeRow>>

    /** Returns the assigned row id. */
    @Insert
    suspend fun insert(solve: SolveEntity): Long

    @Query("DELETE FROM solves")
    suspend fun clear()
}
