package com.queens.puzzle.data.repository

import com.queens.puzzle.model.GameSession
import kotlinx.coroutines.flow.Flow

/**
 * The single game in progress, persisted so that leaving the app resumes rather than restarts.
 *
 * Elapsed time is stored alongside the session, which does not carry it.
 */
interface SessionRepository {

    fun observeSavedSession(): Flow<SavedGame?>

    suspend fun save(gameId: Long, session: GameSession, elapsedMillis: Long)

    suspend fun clear()
}

data class SavedGame(
    val gameId: Long,
    val session: GameSession,
    val elapsedMillis: Long,
)
