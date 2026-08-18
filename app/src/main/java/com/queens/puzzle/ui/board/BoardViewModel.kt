package com.queens.puzzle.ui.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.queens.puzzle.data.BoardRepository
import com.queens.puzzle.ui.board.BoardUiState.Error
import com.queens.puzzle.ui.board.BoardUiState.Loading
import com.queens.puzzle.ui.board.BoardUiState.Success
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardRepository: BoardRepository
) : ViewModel() {

    val uiState: StateFlow<BoardUiState> = boardRepository
        .boards.map<List<String>, BoardUiState>(::Success)
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    fun addBoard(name: String) {
        viewModelScope.launch {
            boardRepository.add(name)
        }
    }
}

sealed interface BoardUiState {
    object Loading : BoardUiState
    data class Error(val throwable: Throwable) : BoardUiState
    data class Success(val data: List<String>) : BoardUiState
}
