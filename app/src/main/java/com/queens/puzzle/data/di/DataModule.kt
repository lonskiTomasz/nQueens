package com.queens.puzzle.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import com.queens.puzzle.data.BoardRepository
import com.queens.puzzle.data.DefaultBoardRepository
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsBoardRepository(
        boardRepository: DefaultBoardRepository
    ): BoardRepository
}

class FakeBoardRepository @Inject constructor() : BoardRepository {
    override val boards: Flow<List<String>> = flowOf(fakeBoards)

    override suspend fun add(name: String) {
        throw NotImplementedError()
    }
}

val fakeBoards = listOf("One", "Two", "Three")
