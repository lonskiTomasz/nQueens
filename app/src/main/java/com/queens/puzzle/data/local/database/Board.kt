package com.queens.puzzle.data.local.database

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity
data class Board(
    val name: String
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
}

@Dao
interface BoardDao {
    @Query("SELECT * FROM board ORDER BY uid DESC LIMIT 10")
    fun getBoards(): Flow<List<Board>>

    @Insert
    suspend fun insertBoard(item: Board)
}
