package com.queens.puzzle.domain.usecase

import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.model.BestTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** The best time per board size, largest board first. */
class ObserveBestTimesUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {

    operator fun invoke(): Flow<List<BestTime>> =
        solveRepository.observeBestTimes().map { bestTimes ->
            bestTimes.sortedByDescending { it.boardSize.value }
        }
}
