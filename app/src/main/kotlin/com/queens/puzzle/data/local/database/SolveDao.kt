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

    /**
     * One solve by id, alongside how it compares to the rest of its size: how many times that
     * size has been solved, and the fastest of the *other* solves — so a solve is never
     * compared against itself. Null when the id no longer exists.
     */
    @Query(
        """
        SELECT s.*,
               (SELECT COUNT(*) FROM solves WHERE board_size = s.board_size) AS solve_count,
               (SELECT MIN(duration_millis) FROM solves
                 WHERE board_size = s.board_size AND id != s.id) AS best_millis
        FROM solves s
        WHERE s.id = :id
        """
    )
    suspend fun getWithSizeSummary(id: Long): SolveWithSizeSummary?

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
