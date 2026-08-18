package com.queens.puzzle.domain.usecase

import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.model.WinSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Reads one solve back and works out how it stands against the rest of its size.
 *
 * The win screen is reached with an id alone, so that it survives process death (§6.2) — which
 * means the "new best, −54s" comparison has to be reconstructed rather than carried through the
 * back stack. "Previous best" is the fastest of the *other* solves at that size, so a solve is
 * never compared against itself.
 */
class ObserveWinSummaryUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {

    operator fun invoke(solveId: Long): Flow<WinSummary?> =
        solveRepository.observeSolves().map { solves ->
            val solve = solves.firstOrNull { it.id == solveId } ?: return@map null
            val sameSize = solves.filter { it.boardSize == solve.boardSize }
            val previousBest = sameSize
                .filter { it.id != solveId }
                .minOfOrNull { it.durationMillis }

            WinSummary(
                solve = solve,
                // A first solve of a size is a personal best; there is nothing to have beaten.
                isNewBest = previousBest == null || solve.durationMillis < previousBest,
                improvementMillis = previousBest?.let { it - solve.durationMillis },
                solveCountForSize = sameSize.size,
            )
        }
}
