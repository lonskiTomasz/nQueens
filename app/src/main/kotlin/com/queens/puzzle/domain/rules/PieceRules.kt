package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.BoardEvaluation
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.Position

/**
 * Decides which pieces threaten each other, which squares they cover, and whether the board is
 * solved, for one piece's movement rule.
 *
 * [QueenRules] and [KnightRules] are the two implementations; a caller picks one by
 * [com.queens.puzzle.model.PuzzleType] and otherwise treats them the same way.
 */
interface PieceRules {

    fun evaluate(
        boardSize: BoardSize,
        pieces: Set<Position>,
        includeAttackedSquares: Boolean = true,
    ): BoardEvaluation
}

fun PieceRules.evaluate(session: GameSession, includeAttackedSquares: Boolean = true): BoardEvaluation =
    evaluate(session.boardSize, session.pieces, includeAttackedSquares)
