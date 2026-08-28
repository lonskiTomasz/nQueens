package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.datastore.SessionDataSource
import com.queens.puzzle.data.mapper.toSavedGameOrNull
import com.queens.puzzle.data.mapper.toSavedSession
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.PuzzleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The game in progress, over the typed session store.
 *
 * Every [save] is a write. Callers that save on each committed move should conflate their own
 * stream — the store applies writes in order, so it would otherwise queue one per tap.
 */
@Singleton
class DefaultSessionRepository @Inject constructor(
    private val sessionDataSource: SessionDataSource,
) : SessionRepository {

    override fun observeSavedSession(): Flow<SavedGame?> =
        sessionDataSource.savedSession.map { it?.toSavedGameOrNull() }

    override suspend fun save(
        gameId: Long,
        puzzleType: PuzzleType,
        session: GameSession,
        elapsedMillis: Long,
    ) = sessionDataSource.save(session.toSavedSession(gameId, puzzleType, elapsedMillis))

    override suspend fun clear() = sessionDataSource.clear()
}
