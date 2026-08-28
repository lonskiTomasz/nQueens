package com.queens.puzzle.testing.repository

import com.queens.puzzle.data.repository.SavedGame
import com.queens.puzzle.data.repository.SessionRepository
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.PuzzleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestSessionRepository(initial: SavedGame? = null) : SessionRepository {

    private val saved = MutableStateFlow(initial)

    val current: SavedGame? get() = saved.value

    /** Every write, in order — so a test can assert that saves were conflated. */
    val writes: List<SavedGame> get() = _writes.toList()
    private val _writes = mutableListOf<SavedGame>()

    var clearCount: Int = 0
        private set

    override fun observeSavedSession(): Flow<SavedGame?> = saved

    override suspend fun save(
        gameId: Long,
        puzzleType: PuzzleType,
        session: GameSession,
        elapsedMillis: Long,
    ) {
        val game = SavedGame(gameId, puzzleType, session, elapsedMillis)
        _writes += game
        saved.value = game
    }

    override suspend fun clear() {
        clearCount++
        saved.value = null
    }
}
