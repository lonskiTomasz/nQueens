package com.queens.puzzle.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.queens.puzzle.data.local.datastore.SavedSession
import com.queens.puzzle.data.local.datastore.SessionDataSource
import com.queens.puzzle.data.local.datastore.SessionSerializer
import com.queens.puzzle.data.local.datastore.SettingsDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * The two stores of §5.2.
 *
 * Each gets its own supervised scope, so a failure writing one cannot cancel the other. Both
 * fall back to their empty value on a corrupt file: settings and an unfinished board are both
 * cheap to lose, and refusing to start is not.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providesPreferencesDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(QueensDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(SettingsDataSource.STORE_NAME) },
    )

    @Provides
    @Singleton
    fun providesSessionDataStore(
        @ApplicationContext context: Context,
        @Dispatcher(QueensDispatcher.IO) ioDispatcher: CoroutineDispatcher,
    ): DataStore<SavedSession?> = DataStoreFactory.create(
        serializer = SessionSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { null },
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.dataStoreFile(SessionDataSource.STORE_NAME) },
    )
}
