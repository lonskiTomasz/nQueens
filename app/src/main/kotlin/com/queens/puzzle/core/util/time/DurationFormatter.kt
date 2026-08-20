package com.queens.puzzle.core.util.time

import kotlin.math.absoluteValue

/**
 * Formats elapsed times and deltas for display.
 *
 * Emits digits and separators only, so nothing here needs translating.
 */
object DurationFormatter {

    private const val MILLIS_PER_SECOND = 1_000L
    private const val SECONDS_PER_MINUTE = 60L
    private const val MINUTES_PER_HOUR = 60L

    /**
     * A running or final clock: `02:14`, rolling over to `1:05:09` past an hour.
     *
     * Truncates rather than rounds. Negative input formats as zero.
     */
    fun format(millis: Long): String {
        val totalSeconds = (millis.coerceAtLeast(0)) / MILLIS_PER_SECOND
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        val minutes = (totalSeconds / SECONDS_PER_MINUTE) % MINUTES_PER_HOUR
        val hours = totalSeconds / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR)

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    /**
     * A signed improvement against a previous best: `-54s` under a minute, `-1:20` above it,
     * `+12s` when slower.
     *
     * A positive [improvementMillis] means faster, and displays with a leading `-`.
     */
    fun formatDelta(improvementMillis: Long): String {
        val sign = if (improvementMillis > 0) "-" else "+"
        val seconds = improvementMillis.absoluteValue / MILLIS_PER_SECOND

        return if (seconds < SECONDS_PER_MINUTE) {
            "$sign${seconds}s"
        } else {
            "$sign%d:%02d".format(seconds / SECONDS_PER_MINUTE, seconds % SECONDS_PER_MINUTE)
        }
    }
}
