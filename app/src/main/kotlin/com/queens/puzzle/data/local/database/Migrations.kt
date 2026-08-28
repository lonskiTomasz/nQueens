package com.queens.puzzle.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.queens.puzzle.model.PuzzleType

/**
 * Adds [SolveEntity.puzzleType]. Every existing row predates N-Knights, so it is correctly
 * backfilled as [PuzzleType.Queens] rather than needing a real migration of the data itself.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE solves ADD COLUMN puzzle_type TEXT NOT NULL DEFAULT '${PuzzleType.Queens.name}'",
        )
        db.execSQL("DROP INDEX IF EXISTS index_solves_board_size_duration_millis")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_solves_puzzle_type_board_size_duration_millis " +
                "ON solves (puzzle_type, board_size, duration_millis)",
        )
    }
}
