package com.queens.puzzle.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data class GameKey(val boardSize: Int, val gameId: Long) : NavKey

@Serializable
data class WinKey(val solveId: Long) : NavKey

@Serializable
data object BestTimesKey : NavKey
