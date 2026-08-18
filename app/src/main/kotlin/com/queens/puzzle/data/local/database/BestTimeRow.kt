package com.queens.puzzle.data.local.database

import androidx.room.ColumnInfo

/** The aggregate projection returned by [SolveDao.observeBestTimes]. */
data class BestTimeRow(
    @ColumnInfo(name = "board_size")
    val boardSize: Int,

    @ColumnInfo(name = "best_millis")
    val bestMillis: Long,

    @ColumnInfo(name = "solve_count")
    val solveCount: Int,
)
