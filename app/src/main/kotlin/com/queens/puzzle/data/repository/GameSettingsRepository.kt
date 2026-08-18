package com.queens.puzzle.data.repository

import com.queens.puzzle.model.GameSettings
import kotlinx.coroutines.flow.Flow

/** Options that change how a game is played and read on the board. */
interface GameSettingsRepository {

    fun observeGameSettings(): Flow<GameSettings>

    suspend fun setShowAttackLines(enabled: Boolean)

    suspend fun setHapticsEnabled(enabled: Boolean)
}
