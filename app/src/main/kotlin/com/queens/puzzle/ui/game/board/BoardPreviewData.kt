package com.queens.puzzle.ui.game.board

import com.queens.puzzle.domain.rules.QueenRules
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Position

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
    val evaluation =
        QueenRules.evaluate(boardSize, queens, includeAttackedSquares = showAttackLines)

    return boardSize.positions().map { position ->
        BoardSquareState(
            position = position,
            hasPiece = position in queens,
            isConflicting = evaluation.isConflicting(position),
            isAttacked = evaluation.isAttacked(position),
        )
    }
}
