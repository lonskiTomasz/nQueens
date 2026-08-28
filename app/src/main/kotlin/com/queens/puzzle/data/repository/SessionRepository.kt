package com.queens.puzzle.data.repository

import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.PuzzleType
import kotlinx.coroutines.flow.Flow

/**
 * The single game in progress, persisted so that leaving the app resumes rather than restarts.
 *
 * Elapsed time and puzzle type are stored alongside the session, which carries neither.
 */
interface SessionRepository {

    fun observeSavedSession(): Flow<SavedGame?>

    suspend fun save(gameId: Long, puzzleType: PuzzleType, session: GameSession, elapsedMillis: Long)

    suspend fun clear()
}

data class SavedGame(
    val gameId: Long,
    val puzzleType: PuzzleType,
    val session: GameSession,
    val elapsedMillis: Long,
)
