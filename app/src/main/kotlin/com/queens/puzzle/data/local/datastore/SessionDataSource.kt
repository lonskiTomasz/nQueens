package com.queens.puzzle.data.local.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** The typed store holding the one game in progress. */
@Singleton
class SessionDataSource @Inject constructor(
    private val dataStore: DataStore<SavedSession?>,
) {

    /** A read failure reads as "nothing saved" rather than killing the collector. */
    val savedSession: Flow<SavedSession?> = dataStore.data
        .catch { cause ->
            if (cause is IOException) emit(null) else throw cause
        }

    suspend fun save(session: SavedSession) {
        dataStore.updateData { session }
    }

    suspend fun clear() {
        dataStore.updateData { null }
    }

    companion object {
        const val STORE_NAME = "session.json"
    }
}
