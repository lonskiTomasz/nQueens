package com.queens.puzzle.domain.usecase

import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.PuzzleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveBestTimesUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {

    operator fun invoke(puzzleType: PuzzleType): Flow<List<BestTime>> =
        solveRepository.observeBestTimes(puzzleType).map { bestTimes ->
            bestTimes.sortedByDescending { it.boardSize.value }
        }
}
