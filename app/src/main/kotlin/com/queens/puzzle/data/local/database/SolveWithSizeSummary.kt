package com.queens.puzzle.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class SolveWithSizeSummary(
    @Embedded
    val solve: SolveEntity,

    @ColumnInfo(name = "solve_count")
    val solveCount: Int,

    @ColumnInfo(name = "best_millis")
    val bestMillisExcludingSelf: Long?,
)
