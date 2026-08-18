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

    suspend fun save(session: GameSession, elapsedMillis: Long)

    suspend fun clear()
}

/** A game in progress, as stored. */
data class SavedGame(
    val session: GameSession,
    val elapsedMillis: Long,
)
