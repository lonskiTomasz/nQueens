package com.queens.puzzle.ui.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Physical feedback for the three moments that deserve it.
 *
 * An interface so a test can bind a recording implementation and assert *that* feedback was
 * requested, with no device involved.
 */
interface GameFeedback {

    fun place()

    fun conflict()

    fun win()
}

/** Does nothing. Bound when the player has haptics switched off. */
object SilentGameFeedback : GameFeedback {
    override fun place() = Unit
    override fun conflict() = Unit
    override fun win() = Unit
}

private class HapticGameFeedback(private val haptics: HapticFeedback) : GameFeedback {

    override fun place() = haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)

    /** Heavier than a placement, so a conflict is felt as different rather than just louder. */
    override fun conflict() = haptics.performHapticFeedback(HapticFeedbackType.LongPress)

    override fun win() = haptics.performHapticFeedback(HapticFeedbackType.LongPress)
}

/** Honours the player's haptics setting by binding [SilentGameFeedback] when it is off. */
@Composable
fun rememberGameFeedback(enabled: Boolean): GameFeedback {
    val haptics = LocalHapticFeedback.current
    return remember(enabled, haptics) {
        if (enabled) HapticGameFeedback(haptics) else SilentGameFeedback
    }
}
