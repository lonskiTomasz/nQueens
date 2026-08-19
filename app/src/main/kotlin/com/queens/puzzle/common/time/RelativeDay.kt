package com.queens.puzzle.common.time

/** How long ago something happened */
sealed interface RelativeDay {

    data object Today : RelativeDay

    data object Yesterday : RelativeDay

    data class DaysAgo(val days: Int) : RelativeDay

    data object LastWeek : RelativeDay

    data class WeeksAgo(val weeks: Int) : RelativeDay
}

/**
 * Buckets a timestamp against the present.
 *
 * Works on elapsed days rather than calendar days, which is coarse near midnight but needs no
 * time zone and stays a pure function — so the buckets are testable without a clock.
 */
object RelativeDayCalculator {

    private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1_000
    private const val DAYS_PER_WEEK = 7

    fun of(thenMillis: Long, nowMillis: Long): RelativeDay {
        val days = ((nowMillis - thenMillis).coerceAtLeast(0) / MILLIS_PER_DAY).toInt()

        return when {
            days == 0 -> RelativeDay.Today
            days == 1 -> RelativeDay.Yesterday
            days < DAYS_PER_WEEK -> RelativeDay.DaysAgo(days)
            days < DAYS_PER_WEEK * 2 -> RelativeDay.LastWeek
            else -> RelativeDay.WeeksAgo(days / DAYS_PER_WEEK)
        }
    }
}
