package com.queens.puzzle.core.designsystem.preview

import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve

fun previewSolve(
    id: Long = 1L,
    boardSize: Int = 8,
    puzzleType: PuzzleType = PuzzleType.Queens,
    durationMillis: Long = 107_000,
    taps: Int = 27,
    undos: Int = 2,
): Solve = Solve(
    id = id,
    boardSize = BoardSize(boardSize),
    puzzleType = puzzleType,
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = 1_787_054_400_000L,
)
