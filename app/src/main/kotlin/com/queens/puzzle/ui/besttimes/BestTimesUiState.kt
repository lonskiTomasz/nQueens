package com.queens.puzzle.ui.besttimes

import com.queens.puzzle.common.time.RelativeDay
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve

/** Everything the history screen renders. */
data class BestTimesUiState(
    /** Sizes that have been played, offered as filters beside "all boards". */
    val filters: List<BoardSize> = emptyList(),
    val selectedFilter: BoardSize? = null,
    val rows: List<SolveRow> = emptyList(),
) {
    val isEmpty: Boolean get() = rows.isEmpty()
}

/**
 * One solve, with how it stands against the best for its size.
 *
 * [deltaMillis] is positive when this solve was faster than the size's best — which only the
 * best itself can be, so it is null exactly when [isBestForSize] is true.
 */
data class SolveRow(
    val solve: Solve,
    val isBestForSize: Boolean,
    val deltaMillis: Long?,
    val occurred: RelativeDay,
)
