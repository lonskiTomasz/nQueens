package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.BoardEvaluation
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.ConflictKind
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.Position

/**
 * Decides which queens threaten each other, which squares they cover, and whether the board
 * is solved.
 *
 * Works by counting occupancy per line: a queen is in conflict exactly when it sits on a row,
 * column, diagonal or anti-diagonal holding more than one queen. That is two linear passes over
 * the queens — O(n) time and space.
 *
 * The attacked squares are the expensive half: covering them walks the four lines through every
 * queen, which makes a call that asks for them O(n²). A caller that does not draw the attack
 * marks should leave `includeAttackedSquares` off and pay for the conflict pass alone.
 *
 * Stateless and pure.
 */
object QueenRules {

    fun evaluate(session: GameSession, includeAttackedSquares: Boolean = true): BoardEvaluation =
        evaluate(session.boardSize, session.pieces, includeAttackedSquares)

    fun evaluate(
        boardSize: BoardSize,
        queens: Set<Position>,
        includeAttackedSquares: Boolean = true,
    ): BoardEvaluation {
        if (queens.isEmpty()) return BoardEvaluation()

        val rows = mutableMapOf<Int, Int>()
        val columns = mutableMapOf<Int, Int>()
        val diagonals = mutableMapOf<Int, Int>()
        val antiDiagonals = mutableMapOf<Int, Int>()

        for (queen in queens) {
            rows.increment(queen.row)
            columns.increment(queen.column)
            diagonals.increment(queen.diagonal)
            antiDiagonals.increment(queen.antiDiagonal)
        }

        val conflicts = mutableSetOf<Position>()
        val conflictKinds = mutableSetOf<ConflictKind>()

        for (queen in queens) {
            var conflicted = false
            if (rows.isShared(queen.row)) {
                conflictKinds += ConflictKind.Row
                conflicted = true
            }
            if (columns.isShared(queen.column)) {
                conflictKinds += ConflictKind.Column
                conflicted = true
            }
            if (diagonals.isShared(queen.diagonal) || antiDiagonals.isShared(queen.antiDiagonal)) {
                conflictKinds += ConflictKind.Diagonal
                conflicted = true
            }
            if (conflicted) conflicts += queen
        }

        return BoardEvaluation(
            conflicts = conflicts,
            conflictKinds = conflictKinds,
            attackedSquares =
                if (includeAttackedSquares) attackedSquares(boardSize, queens) else emptySet(),
            isSolved = queens.size == boardSize.value && conflicts.isEmpty(),
        )
    }

    /** Every empty square covered by at least one queen. Queens themselves are excluded. */
    private fun attackedSquares(boardSize: BoardSize, queens: Set<Position>): Set<Position> {
        val attacked = mutableSetOf<Position>()
        for (queen in queens) {
            for (i in 0 until boardSize.value) {
                attacked += Position(queen.row, i)
                attacked += Position(i, queen.column)
                Position(i, i - queen.diagonal).takeIf { it in boardSize }?.let { attacked += it }
                Position(i, queen.antiDiagonal - i).takeIf { it in boardSize }?.let { attacked += it }
            }
        }
        return attacked - queens
    }

    private fun MutableMap<Int, Int>.increment(key: Int) {
        this[key] = (this[key] ?: 0) + 1
    }

    private fun Map<Int, Int>.isShared(key: Int): Boolean = (this[key] ?: 0) > 1
}
