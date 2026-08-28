package com.queens.puzzle.data.local.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.queens.puzzle.model.PuzzleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_DB = "migration-test"

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun solvesRecordedBeforeKnightsAreKeptAsQueens() {
        helper.createDatabase(TEST_DB, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO solves (board_size, duration_millis, taps, undos, completed_at)
                VALUES (8, 107000, 27, 2, 1000)
                """.trimIndent(),
            )
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        migrated.query("SELECT board_size, puzzle_type FROM solves").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(8, cursor.getInt(0))
            assertEquals(PuzzleType.Queens.name, cursor.getString(1))
        }
    }

    /** The old index is dropped rather than left shadowing the one the queries now rely on. */
    @Test
    fun theCoveringIndexIsReplaced() {
        helper.createDatabase(TEST_DB, 1).close()

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        val indices = mutableListOf<String>()
        migrated.query("SELECT name FROM sqlite_master WHERE type = 'index'").use { cursor ->
            while (cursor.moveToNext()) indices += cursor.getString(0)
        }

        assertTrue(
            "expected the (puzzle_type, board_size, duration_millis) index, had $indices",
            "index_solves_puzzle_type_board_size_duration_millis" in indices,
        )
        assertTrue(
            "the board_size-only index should be gone, had $indices",
            "index_solves_board_size_duration_millis" !in indices,
        )
    }
}
