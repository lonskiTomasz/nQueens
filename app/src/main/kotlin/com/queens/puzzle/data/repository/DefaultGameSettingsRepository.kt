package com.queens.puzzle.data.repository

import com.queens.puzzle.data.local.datastore.SettingsDataSource
import com.queens.puzzle.model.GameSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Gameplay options, over the preferences store. */
@Singleton
class DefaultGameSettingsRepository @Inject constructor(
    private val settingsDataSource: SettingsDataSource,
) : GameSettingsRepository {

    override fun observeGameSettings(): Flow<GameSettings> = settingsDataSource.gameSettings

    override suspend fun setShowAttackLines(enabled: Boolean) =
        settingsDataSource.setShowAttackLines(enabled)

    override suspend fun setHapticsEnabled(enabled: Boolean) =
        settingsDataSource.setHapticsEnabled(enabled)
}
