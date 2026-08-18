package com.queens.puzzle.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Board::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boardDao(): BoardDao
}
