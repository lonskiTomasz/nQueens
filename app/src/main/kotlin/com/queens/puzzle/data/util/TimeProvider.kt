package com.queens.puzzle.data.util

/** The clock. [nowMillis] is wall-clock; [elapsedMillis] is monotonic. */
interface TimeProvider {

    /** Wall-clock time, for recording when something happened. */
    fun nowMillis(): Long

    /** Monotonic time, for measuring how long something took. Unaffected by clock changes. */
    fun elapsedMillis(): Long
}
