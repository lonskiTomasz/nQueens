package com.queens.puzzle.testing.repository

import com.queens.puzzle.data.repository.GameSettingsRepository
import com.queens.puzzle.model.GameSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestGameSettingsRepository(
    initial: GameSettings = GameSettings(),
) : GameSettingsRepository {

    private val settings = MutableStateFlow(initial)

    val current: GameSettings get() = settings.value

    override fun observeGameSettings(): Flow<GameSettings> = settings

    override suspend fun setShowAttackLines(enabled: Boolean) {
        settings.value = settings.value.copy(showAttackLines = enabled)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        settings.value = settings.value.copy(hapticsEnabled = enabled)
    }
}
