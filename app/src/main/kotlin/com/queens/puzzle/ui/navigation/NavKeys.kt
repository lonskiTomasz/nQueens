package com.queens.puzzle.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.queens.puzzle.model.PuzzleType
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : NavKey

@Serializable
data class GameKey(val boardSize: Int, val gameId: Long, val puzzleType: PuzzleType) : NavKey

@Serializable
data class WinKey(val solveId: Long) : NavKey

@Serializable
data object BestTimesKey : NavKey
