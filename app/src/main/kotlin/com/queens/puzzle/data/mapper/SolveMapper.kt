package com.queens.puzzle.data.mapper

import com.queens.puzzle.data.local.database.BestTimeRow
import com.queens.puzzle.data.local.database.SolveEntity
import com.queens.puzzle.data.local.database.SolveWithSizeSummary
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.Solve
import com.queens.puzzle.model.SolveSizeSummary

fun SolveEntity.toModel(): Solve = Solve(
    id = id,
    boardSize = BoardSize(boardSize),
    puzzleType = PuzzleType.valueOf(puzzleType),
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = completedAtMillis,
)

/** The id is left to Room: [Solve.id] is 0 until the row is inserted. */
fun Solve.toEntity(): SolveEntity = SolveEntity(
    id = id,
    boardSize = boardSize.value,
    puzzleType = puzzleType.name,
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = completedAtMillis,
)

fun BestTimeRow.toModel(): BestTime = BestTime(
    boardSize = BoardSize(boardSize),
    puzzleType = PuzzleType.valueOf(puzzleType),
    bestMillis = bestMillis,
    solveCount = solveCount,
)

fun SolveWithSizeSummary.toModel(): SolveSizeSummary = SolveSizeSummary(
    solve = solve.toModel(),
    solveCount = solveCount,
    bestMillisExcludingSelf = bestMillisExcludingSelf,
)
