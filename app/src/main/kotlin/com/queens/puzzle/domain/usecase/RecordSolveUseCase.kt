package com.queens.puzzle.domain.usecase

import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.data.util.TimeProvider
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.SolveOutcome
import javax.inject.Inject

/**
 * Records a finished game and reports how it compares with the player's previous best.
 *
 * Reads the previous best before inserting, so the new solve is not compared against itself.
 */
class RecordSolveUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
    private val timeProvider: TimeProvider,
) {

    suspend operator fun invoke(
        boardSize: BoardSize,
        puzzleType: PuzzleType,
        durationMillis: Long,
        taps: Int,
        undos: Int,
    ): SolveOutcome {
        val previousBest = solveRepository.bestFor(boardSize, puzzleType)

        val solveId = solveRepository.record(
            Solve(
                id = 0L,
                boardSize = boardSize,
                puzzleType = puzzleType,
                durationMillis = durationMillis,
                taps = taps,
                undos = undos,
                completedAtMillis = timeProvider.nowMillis(),
            )
        )

        val improvement = previousBest?.let { it.durationMillis - durationMillis }

        return SolveOutcome(
            solveId = solveId,
            isNewBest = previousBest == null || durationMillis < previousBest.durationMillis,
            improvementMillis = improvement,
        )
    }
}
