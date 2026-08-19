package com.queens.puzzle.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The index covers the two queries that matter — best-per-size and the best for one size —
 * so both are served from the index without touching the table.
 */
@Entity(
    tableName = "solves",
    indices = [Index(value = ["board_size", "duration_millis"])],
)
data class SolveEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "board_size")
    val boardSize: Int,

    @ColumnInfo(name = "duration_millis")
    val durationMillis: Long,

    @ColumnInfo(name = "taps")
    val taps: Int,

    @ColumnInfo(name = "undos")
    val undos: Int,

    /** Epoch millis. */
    @ColumnInfo(name = "completed_at")
    val completedAtMillis: Long,
)
