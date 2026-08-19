package com.queens.puzzle.data.di

import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.data.repository.DefaultAppSettingsRepository
import com.queens.puzzle.data.repository.DefaultGameSettingsRepository
import com.queens.puzzle.data.repository.DefaultSessionRepository
import com.queens.puzzle.data.repository.DefaultSolveRepository
import com.queens.puzzle.data.repository.GameSettingsRepository
import com.queens.puzzle.data.repository.SessionRepository
import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.data.util.SystemTimeProvider
import com.queens.puzzle.data.util.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsSolveRepository(impl: DefaultSolveRepository): SolveRepository

    @Binds
    @Singleton
    abstract fun bindsGameSettingsRepository(
        impl: DefaultGameSettingsRepository,
    ): GameSettingsRepository

    @Binds
    @Singleton
    abstract fun bindsAppSettingsRepository(
        impl: DefaultAppSettingsRepository,
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindsSessionRepository(impl: DefaultSessionRepository): SessionRepository

    @Binds
    @Singleton
    abstract fun bindsTimeProvider(impl: SystemTimeProvider): TimeProvider
}
