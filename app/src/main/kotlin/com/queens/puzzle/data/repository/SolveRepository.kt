package com.queens.puzzle.data.repository

import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve
import kotlinx.coroutines.flow.Flow

/** The solve history. */
interface SolveRepository {

    /** All solves, newest first. */
    fun observeSolves(): Flow<List<Solve>>

    fun observeSolves(boardSize: BoardSize): Flow<List<Solve>>

    /** The fastest solve for [boardSize], or null when it has never been solved. */
    suspend fun bestFor(boardSize: BoardSize): Solve?

    /** Records a finished game and returns its assigned id. */
    suspend fun record(solve: Solve): Long

    fun observeBestTimes(): Flow<List<BestTime>>

    suspend fun clearHistory()
}
