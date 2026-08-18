package com.queens.puzzle.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.queens.puzzle.data.local.database.Board
import com.queens.puzzle.data.local.database.BoardDao
import javax.inject.Inject

interface BoardRepository {
    val boards: Flow<List<String>>

    suspend fun add(name: String)
}

class DefaultBoardRepository @Inject constructor(
    private val boardDao: BoardDao
) : BoardRepository {

    override val boards: Flow<List<String>> =
        boardDao.getBoards().map { items -> items.map { it.name } }

    override suspend fun add(name: String) {
        boardDao.insertBoard(Board(name = name))
    }
}
