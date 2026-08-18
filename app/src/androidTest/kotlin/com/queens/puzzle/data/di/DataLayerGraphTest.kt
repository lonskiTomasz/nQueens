package com.queens.puzzle.data.di

import com.queens.puzzle.data.repository.AppSettingsRepository
import com.queens.puzzle.data.repository.GameSettingsRepository
import com.queens.puzzle.data.repository.SessionRepository
import com.queens.puzzle.data.repository.SolveRepository
import com.queens.puzzle.data.util.TimeProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Asks for every binding the data layer publishes.
 *
 * Dagger prunes bindings nothing requests, so until a ViewModel injects a repository the graph
 * is declared but never built. Naming them all here makes a missing `@Inject`, a wrong
 * qualifier or an unbound interface a *compile* failure of this source set — which needs no
 * device, even though running the assertions does.
 */
@HiltAndroidTest
class DataLayerGraphTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var solveRepository: SolveRepository

    @Inject lateinit var gameSettingsRepository: GameSettingsRepository

    @Inject lateinit var appSettingsRepository: AppSettingsRepository

    @Inject lateinit var sessionRepository: SessionRepository

    @Inject lateinit var timeProvider: TimeProvider

    @Inject @Dispatcher(QueensDispatcher.IO) lateinit var ioDispatcher: CoroutineDispatcher

    @Inject @Dispatcher(QueensDispatcher.Default) lateinit var defaultDispatcher: CoroutineDispatcher

    @Before
    fun inject() {
        hiltRule.inject()
    }

    @Test
    fun everyDataLayerDependencyResolves() {
        assertNotNull(solveRepository)
        assertNotNull(gameSettingsRepository)
        assertNotNull(appSettingsRepository)
        assertNotNull(sessionRepository)
        assertNotNull(timeProvider)
        assertNotNull(ioDispatcher)
        assertNotNull(defaultDispatcher)
    }
}
