package com.queens.puzzle.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

/** Schemas are exported to `app/schemas` and committed. */
@Database(
    entities = [SolveEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun solveDao(): SolveDao

    companion object {
        const val NAME = "queens.db"
    }
}
