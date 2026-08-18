package com.queens.puzzle.ui.board


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import com.queens.puzzle.data.BoardRepository

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class BoardViewModelTest {
    @Test
    fun uiState_initiallyLoading() = runTest {
        val viewModel = BoardViewModel(FakeBoardRepository())
        assertEquals(viewModel.uiState.first(), BoardUiState.Loading)
    }

    @Test
    fun uiState_onItemSaved_isDisplayed() = runTest {
        val viewModel = BoardViewModel(FakeBoardRepository())
        assertEquals(viewModel.uiState.first(), BoardUiState.Loading)
    }
}

private class FakeBoardRepository : BoardRepository {

    private val data = mutableListOf<String>()

    override val boards: Flow<List<String>>
        get() = flow { emit(data.toList()) }

    override suspend fun add(name: String) {
        data.add(0, name)
    }
}
