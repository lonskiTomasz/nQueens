package com.queens.puzzle.common.time

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {

    @Test
    fun `zero renders as a full clock`() {
        assertEquals("00:00", DurationFormatter.format(0))
    }

    @Test
    fun `seconds and minutes are zero padded`() {
        assertEquals("00:07", DurationFormatter.format(7_000))
        assertEquals("02:14", DurationFormatter.format(134_000))
    }

    @Test
    fun `partial seconds are truncated, never rounded up`() {
        assertEquals("00:00", DurationFormatter.format(999))
        assertEquals("00:01", DurationFormatter.format(1_999))
    }

    @Test
    fun `the minute boundary rolls over exactly once`() {
        assertEquals("00:59", DurationFormatter.format(59_999))
        assertEquals("01:00", DurationFormatter.format(60_000))
    }

    @Test
    fun `past an hour the hour field appears`() {
        assertEquals("59:59", DurationFormatter.format(3_599_000))
        assertEquals("1:00:00", DurationFormatter.format(3_600_000))
        assertEquals("1:05:09", DurationFormatter.format(3_909_000))
        assertEquals("10:00:00", DurationFormatter.format(36_000_000))
    }

    @Test
    fun `a negative clock is clamped to zero`() {
        assertEquals("00:00", DurationFormatter.format(-5_000))
    }

    @Test
    fun `an improvement reads as a negative delta`() {
        assertEquals("-54s", DurationFormatter.formatDelta(54_000))
        assertEquals("-1:20", DurationFormatter.formatDelta(80_000))
    }

    @Test
    fun `a slower time reads as a positive delta`() {
        assertEquals("+12s", DurationFormatter.formatDelta(-12_000))
        assertEquals("+2:05", DurationFormatter.formatDelta(-125_000))
    }

    @Test
    fun `a dead heat has no sign of improvement`() {
        assertEquals("+0s", DurationFormatter.formatDelta(0))
    }

    @Test
    fun `the delta minute boundary switches format`() {
        assertEquals("-59s", DurationFormatter.formatDelta(59_000))
        assertEquals("-1:00", DurationFormatter.formatDelta(60_000))
    }
}
