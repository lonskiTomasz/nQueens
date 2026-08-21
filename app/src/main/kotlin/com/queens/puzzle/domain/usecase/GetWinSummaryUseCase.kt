package com.queens.puzzle.domain.usecase

import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.model.WinSummary
import javax.inject.Inject

class GetWinSummaryUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {

    suspend operator fun invoke(solveId: Long): WinSummary? {
        val context = solveRepository.solveSizeSummaryFor(solveId) ?: return null

        return WinSummary(
            solve = context.solve,
            isNewBest = context.bestMillisExcludingSelf == null ||
                context.solve.durationMillis < context.bestMillisExcludingSelf,
            improvementMillis = context.bestMillisExcludingSelf?.let { it - context.solve.durationMillis },
            solveCountForSize = context.solveCount,
        )
    }
}
