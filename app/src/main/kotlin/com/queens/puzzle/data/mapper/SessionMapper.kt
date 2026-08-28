package com.queens.puzzle.data.mapper

import com.queens.puzzle.data.local.datastore.SavedMove
import com.queens.puzzle.data.local.datastore.SavedMoveKind
import com.queens.puzzle.data.local.datastore.SavedPosition
import com.queens.puzzle.data.local.datastore.SavedPuzzleType
import com.queens.puzzle.data.local.datastore.SavedSession
import com.queens.puzzle.data.repository.SavedGame
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.GameSession
import com.queens.puzzle.model.Move
import com.queens.puzzle.model.Position
import com.queens.puzzle.model.PuzzleType

fun GameSession.toSavedSession(
    gameId: Long,
    puzzleType: PuzzleType,
    elapsedMillis: Long,
): SavedSession = SavedSession(
    gameId = gameId,
    boardSize = boardSize.value,
    puzzleType = puzzleType.toSaved(),
    pieces = pieces.map { it.toSaved() },
    moves = moves.map { it.toSaved() },
    taps = taps,
    undos = undos,
    elapsedMillis = elapsedMillis,
)

/**
 * Returns null when the stored session no longer describes a legal game — an out-of-range
 * board size, or a queen off the board.
 *
 * A resumable session is disposable: dropping one costs the player a board they had not
 * finished, where throwing would take the home screen down with it every launch.
 */
fun SavedSession.toSavedGameOrNull(): SavedGame? {
    val size = BoardSize.ofOrNull(boardSize) ?: return null
    return runCatching {
        SavedGame(
            gameId = gameId,
            puzzleType = puzzleType.toModel(),
            session = GameSession(
                boardSize = size,
                pieces = pieces.map { it.toModel() }.toSet(),
                moves = moves.map { it.toModel() },
                taps = taps,
                undos = undos,
            ),
            elapsedMillis = elapsedMillis,
        )
    }.getOrNull()
}

private fun PuzzleType.toSaved(): SavedPuzzleType = when (this) {
    PuzzleType.Queens -> SavedPuzzleType.Queens
    PuzzleType.Knights -> SavedPuzzleType.Knights
}

private fun SavedPuzzleType.toModel(): PuzzleType = when (this) {
    SavedPuzzleType.Queens -> PuzzleType.Queens
    SavedPuzzleType.Knights -> PuzzleType.Knights
}

private fun Position.toSaved() = SavedPosition(row = row, column = column)

private fun SavedPosition.toModel() = Position(row = row, column = column)

private fun Move.toSaved() = SavedMove(
    kind = when (this) {
        is Move.Place -> SavedMoveKind.Place
        is Move.Remove -> SavedMoveKind.Remove
    },
    position = position.toSaved(),
)

private fun SavedMove.toModel(): Move = when (kind) {
    SavedMoveKind.Place -> Move.Place(position.toModel())
    SavedMoveKind.Remove -> Move.Remove(position.toModel())
}
