package com.queens.puzzle.testing.util

import com.queens.puzzle.data.util.TimeProvider

/** A clock that only moves when a test tells it to. */
class TestTimeProvider(
    private var now: Long = FIXED_NOW,
    private var elapsed: Long = 0L,
) : TimeProvider {

    override fun nowMillis(): Long = now

    override fun elapsedMillis(): Long = elapsed

    fun advanceBy(millis: Long) {
        now += millis
        elapsed += millis
    }

    companion object {
        /** 2026-08-18T12:00:00Z. */
        const val FIXED_NOW = 1_787_054_400_000L
    }
}
