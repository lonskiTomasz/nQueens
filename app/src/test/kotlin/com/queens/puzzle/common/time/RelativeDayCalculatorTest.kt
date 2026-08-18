package com.queens.puzzle.common.time

import org.junit.Assert.assertEquals
import org.junit.Test

private const val DAY = 24L * 60 * 60 * 1_000
private const val NOW = 1_787_054_400_000L

class RelativeDayCalculatorTest {

    @Test
    fun `the same day is today`() {
        assertEquals(RelativeDay.Today, of(NOW))
        assertEquals(RelativeDay.Today, of(NOW - DAY + 1))
    }

    @Test
    fun `a day back is yesterday`() {
        assertEquals(RelativeDay.Yesterday, of(NOW - DAY))
    }

    @Test
    fun `two to six days back count days`() {
        assertEquals(RelativeDay.DaysAgo(2), of(NOW - 2 * DAY))
        assertEquals(RelativeDay.DaysAgo(6), of(NOW - 6 * DAY))
    }

    @Test
    fun `the second week back is last week`() {
        assertEquals(RelativeDay.LastWeek, of(NOW - 7 * DAY))
        assertEquals(RelativeDay.LastWeek, of(NOW - 13 * DAY))
    }

    @Test
    fun `beyond a fortnight counts weeks`() {
        assertEquals(RelativeDay.WeeksAgo(2), of(NOW - 14 * DAY))
        assertEquals(RelativeDay.WeeksAgo(5), of(NOW - 37 * DAY))
    }

    @Test
    fun `a timestamp in the future reads as today rather than going negative`() {
        assertEquals(RelativeDay.Today, of(NOW + 5 * DAY))
    }

    private fun of(thenMillis: Long) = RelativeDayCalculator.of(thenMillis, NOW)
}
