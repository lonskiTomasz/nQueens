package com.queens.puzzle.ui.game.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

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

    override fun conflict() = haptics.performHapticFeedback(HapticFeedbackType.LongPress)

    override fun win() = haptics.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberGameFeedback(enabled: Boolean): GameFeedback {
    val haptics = LocalHapticFeedback.current
    return remember(enabled, haptics) {
        if (enabled) HapticGameFeedback(haptics) else SilentGameFeedback
    }
}
