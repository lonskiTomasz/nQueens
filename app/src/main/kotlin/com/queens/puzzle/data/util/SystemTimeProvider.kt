package com.queens.puzzle.data.util

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The platform clock.
 *
 * [elapsedMillis] reads `elapsedRealtime`, which cannot be moved by the user or by a network
 * time update — a game timer must not jump because the clock was corrected mid-solve.
 */
@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {

    override fun nowMillis(): Long = System.currentTimeMillis()

    override fun elapsedMillis(): Long = SystemClock.elapsedRealtime()
}
