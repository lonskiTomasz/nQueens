package com.queens.puzzle.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.queens.puzzle.data.local.database.Board
import com.queens.puzzle.data.local.database.BoardDao

@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class DefaultBoardRepositoryTest {

    @Test
    fun boards_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultBoardRepository(FakeBoardDao())

        repository.add("Repository")

        assertEquals(repository.boards.first().size, 1)
    }

}

private class FakeBoardDao : BoardDao {

    private val data = mutableListOf<Board>()

    override fun getBoards(): Flow<List<Board>> = flow {
        emit(data)
    }

    override suspend fun insertBoard(item: Board) {
        data.add(0, item)
    }
}
