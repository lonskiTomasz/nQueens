package com.queens.puzzle.data.di

import android.content.Context
import androidx.room.Room
import com.queens.puzzle.data.local.database.AppDatabase
import com.queens.puzzle.data.local.database.MIGRATION_1_2
import com.queens.puzzle.data.local.database.SolveDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providesSolveDao(database: AppDatabase): SolveDao = database.solveDao()
}
