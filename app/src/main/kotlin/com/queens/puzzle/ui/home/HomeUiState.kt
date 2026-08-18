package com.queens.puzzle.ui.home

import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ThemePreference

/** Everything the home screen renders. */
data class HomeUiState(
    val sizes: List<BoardSize> = BoardSize.selectable,
    val selectedSize: BoardSize = BoardSize.Default,
    val theme: ThemePreference = ThemePreference.Light,
    val bestTimes: List<BestTime> = emptyList(),
    /** The size of the stored board, when there is one to carry on with. */
    val resumableSize: BoardSize? = null,
) {
    val canResume: Boolean get() = resumableSize != null
}
