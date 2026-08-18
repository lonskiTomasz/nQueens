package com.queens.puzzle.data.mapper

import com.queens.puzzle.data.local.database.BestTimeRow
import com.queens.puzzle.data.local.database.SolveEntity
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Solve

/**
 * Stored rows to model and back.
 *
 * `board_size` is only ever written from a validated [BoardSize], so reconstructing one here
 * cannot fail for data this app wrote. Narrowing [BoardSize.MIN]..[BoardSize.MAX] later would
 * strand existing rows, and that is a migration's job rather than a silent drop here.
 */
fun SolveEntity.toModel(): Solve = Solve(
    id = id,
    boardSize = BoardSize(boardSize),
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = completedAtMillis,
)

/** The id is left to Room: [Solve.id] is 0 until the row is inserted. */
fun Solve.toEntity(): SolveEntity = SolveEntity(
    id = id,
    boardSize = boardSize.value,
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = completedAtMillis,
)

fun BestTimeRow.toModel(): BestTime = BestTime(
    boardSize = BoardSize(boardSize),
    bestMillis = bestMillis,
    solveCount = solveCount,
)
