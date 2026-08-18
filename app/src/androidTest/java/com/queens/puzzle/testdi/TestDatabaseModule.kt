package com.queens.puzzle.testdi

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import com.queens.puzzle.data.BoardRepository
import com.queens.puzzle.data.di.DataModule
import com.queens.puzzle.data.di.FakeBoardRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
interface FakeDataModule {

    @Binds
    abstract fun bindRepository(
        fakeRepository: FakeBoardRepository
    ): BoardRepository
}
