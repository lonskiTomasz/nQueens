package com.queens.puzzle.testing.local

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes updates behind a mutex the way the real store does, so a test that writes
 * concurrently sees the same last-writer-wins behaviour without touching disk.
 */
class InMemoryDataStore<T>(initial: T) : DataStore<T> {

    private val state = MutableStateFlow(initial)
    private val writeLock = Mutex()

    override val data: Flow<T> = state

    override suspend fun updateData(transform: suspend (T) -> T): T = writeLock.withLock {
        transform(state.value).also { state.value = it }
    }
}
