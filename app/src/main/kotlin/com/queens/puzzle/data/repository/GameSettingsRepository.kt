package com.queens.puzzle.data.repository

import com.queens.puzzle.model.GameSettings
import kotlinx.coroutines.flow.Flow

interface GameSettingsRepository {

    fun observeGameSettings(): Flow<GameSettings>

    suspend fun setShowAttackLines(enabled: Boolean)

    suspend fun setHapticsEnabled(enabled: Boolean)
}
