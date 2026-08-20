package com.queens.puzzle.ui.designsystem.preview

import com.queens.puzzle.domain.rules.BoardEvaluator
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Position
import com.queens.puzzle.model.Solve
import com.queens.puzzle.ui.board.BoardSquareState

val PreviewQueens: Set<Position> = setOf(
    Position(0, 0),
    Position(1, 4),
    Position(2, 7),
    Position(3, 5),
    Position(4, 6),
)

val PreviewSolvedQueens: Set<Position> = setOf(
    Position(0, 1),
    Position(1, 3),
    Position(2, 0),
    Position(3, 2),
)

fun previewSquares(
    boardSize: BoardSize,
    queens: Set<Position>,
    showAttackLines: Boolean = true,
): List<BoardSquareState> {
    val evaluation = BoardEvaluator.evaluate(boardSize, queens)

    return boardSize.positions().map { position ->
        BoardSquareState(
            position = position,
            hasQueen = position in queens,
            isConflicting = evaluation.isConflicting(position),
            isAttacked = showAttackLines && evaluation.isAttacked(position),
        )
    }
}

fun previewSolve(
    id: Long = 1L,
    boardSize: Int = 8,
    durationMillis: Long = 107_000,
    taps: Int = 27,
    undos: Int = 2,
): Solve = Solve(
    id = id,
    boardSize = BoardSize(boardSize),
    durationMillis = durationMillis,
    taps = taps,
    undos = undos,
    completedAtMillis = 1_787_054_400_000L,
)
