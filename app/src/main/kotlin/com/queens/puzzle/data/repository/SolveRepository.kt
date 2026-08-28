package com.queens.puzzle.data.repository

import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.SolveSizeSummary
import kotlinx.coroutines.flow.Flow

interface SolveRepository {

    /** All solves, newest first, across every puzzle type. */
    fun observeSolves(): Flow<List<Solve>>

    fun observeSolves(boardSize: BoardSize, puzzleType: PuzzleType): Flow<List<Solve>>

    /** The fastest solve for [boardSize]/[puzzleType], or null when it has never been solved. */
    suspend fun bestFor(boardSize: BoardSize, puzzleType: PuzzleType): Solve?

    /**
     * The solve with this id, alongside how it compares to the rest of its (mode, size) — or
     * null when it no longer exists.
     */
    suspend fun solveSizeSummaryFor(id: Long): SolveSizeSummary?

    /** Records a finished game and returns its assigned id. */
    suspend fun record(solve: Solve): Long

    fun observeBestTimes(puzzleType: PuzzleType): Flow<List<BestTime>>

    suspend fun clearHistory()
}
