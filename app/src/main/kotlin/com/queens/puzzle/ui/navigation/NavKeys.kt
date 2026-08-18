package com.queens.puzzle.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The destinations, as type-safe keys.
 *
 * Arguments are the key's own properties, so a screen cannot be reached without them and a
 * misspelled one is a compile error rather than a null at runtime.
 */
@Serializable
data object HomeKey : NavKey

@Serializable
data class GameKey(val boardSize: Int) : NavKey

/** The win screen takes only an id and loads the solve itself, so it survives process death. */
@Serializable
data class WinKey(val solveId: Long) : NavKey

@Serializable
data object BestTimesKey : NavKey
