package com.queens.puzzle.data.repository

import com.queens.puzzle.model.AppSettings
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.ThemePreference
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    fun observeAppSettings(): Flow<AppSettings>

    suspend fun setTheme(theme: ThemePreference)

    suspend fun setLastBoardSize(boardSize: BoardSize)

    suspend fun setLastPuzzleType(puzzleType: PuzzleType)
}
