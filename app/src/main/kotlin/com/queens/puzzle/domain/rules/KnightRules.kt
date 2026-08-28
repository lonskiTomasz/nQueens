package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.BoardEvaluation
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.Position

/**
 * Decides which knights attack each other, which squares they cover, and whether the board is
 * solved.
 *
 * A knight's reach is a fixed set of (at most eight) L-shaped destinations rather than a line, so
 * each knight is simply checked against its own destinations — O(n), eight being a constant.
 */
object KnightRules : PieceRules {

    private val OFFSETS = listOf(
        1 to 2, 2 to 1, -1 to 2, -2 to 1,
        1 to -2, 2 to -1, -1 to -2, -2 to -1,
    )

    override fun evaluate(
        boardSize: BoardSize,
        pieces: Set<Position>,
        includeAttackedSquares: Boolean,
    ): BoardEvaluation {
        val knights = pieces
        if (knights.isEmpty()) return BoardEvaluation()

        val conflicts = knights
            .filter { knight -> reach(knight, boardSize).any { it in knights } }
            .toSet()

        return BoardEvaluation(
            conflicts = conflicts,
            conflictKinds = if (conflicts.isEmpty()) emptySet() else setOf(ConflictKind.Knight),
            attackedSquares =
                if (includeAttackedSquares) attackedSquares(boardSize, knights) else emptySet(),
            isSolved = knights.size == boardSize.value && conflicts.isEmpty(),
        )
    }

    /** Every empty square a knight's move away from at least one knight. Knights excluded. */
    private fun attackedSquares(boardSize: BoardSize, knights: Set<Position>): Set<Position> =
        knights.flatMap { reach(it, boardSize) }.toSet() - knights

    private fun reach(from: Position, boardSize: BoardSize): List<Position> =
        OFFSETS.map { (dRow, dColumn) -> Position(from.row + dRow, from.column + dColumn) }
            .filter { it in boardSize }
}
