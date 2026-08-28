package com.queens.puzzle.domain.game

import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.Move
import com.queens.puzzle.model.Position

/**
 * Applies [action] to the session and returns the result.
 *
 * Total: every action against every state returns a session. A refused action — a tap on a
 * full board, an undo with nothing to undo, a tap off the board — returns the receiver
 * unchanged rather than throwing.
 */
fun GameSession.reduce(action: GameAction): GameSession = when (action) {
    is GameAction.TapSquare -> tapSquare(action.position)
    GameAction.Undo -> undo()
    GameAction.Reset -> reset()
}

/**
 * Places a piece on an empty square, removes one from an occupied square.
 *
 * Placement is refused once the board holds `n` pieces. Removal is always allowed.
 */
private fun GameSession.tapSquare(position: Position): GameSession {
    if (position !in boardSize) return this

    return when {
        hasPieceAt(position) -> copy(
            pieces = pieces - position,
            moves = moves + Move.Remove(position),
            taps = taps + 1,
        )

        piecesRemaining == 0 -> this

        else -> copy(
            pieces = pieces + position,
            moves = moves + Move.Place(position),
            taps = taps + 1,
        )
    }
}

/**
 * Reverses the last move, one at a time.
 *
 * Counts towards [GameSession.undos] and leaves [GameSession.taps] alone. Undoing pushes
 * nothing onto the stack, so an undo cannot itself be undone.
 */
private fun GameSession.undo(): GameSession {
    val lastMove = moves.lastOrNull() ?: return this
    val remaining = moves.dropLast(1)

    return when (lastMove) {
        is Move.Place -> copy(
            pieces = pieces - lastMove.position,
            moves = remaining,
            undos = undos + 1,
        )

        is Move.Remove -> copy(
            pieces = pieces + lastMove.position,
            moves = remaining,
            undos = undos + 1,
        )
    }
}

private fun GameSession.reset(): GameSession = GameSession(boardSize = boardSize)
