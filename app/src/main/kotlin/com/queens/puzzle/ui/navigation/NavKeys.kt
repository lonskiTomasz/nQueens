package com.queens.puzzle.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

/** [resume] distinguishes "carry on with the stored board" from "start this size fresh". */
@Serializable
data class GameKey(val boardSize: Int, val resume: Boolean = false) : NavKey

@Serializable
data class WinKey(val solveId: Long) : NavKey

@Serializable
data object BestTimesKey : NavKey
