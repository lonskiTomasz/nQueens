package com.queens.puzzle.data.local.datastore

import kotlinx.serialization.Serializable

/**
 * A game in progress, as written to disk.
 *
 * Deliberately its own shape rather than the `model` types: keeping the stored form here means
 * `model` stays dependency-free, and a change to the on-disk format is a change to this file
 * and its mapper rather than to the vocabulary the whole app shares.
 *
 * [moves] is stored so that undo survives a resume — without it a resumed board could not be
 * unwound past the point it was reloaded.
 *
 * The field names are the on-disk format, so renaming one is a format change. A board saved by
 * an older version no longer parses and is dropped by the store's corruption handler, which
 * costs the player an unfinished board and nothing else — solve history lives in Room.
 */
@Serializable
data class SavedSession(
    val gameId: Long = 0L,
    val boardSize: Int,
    val pieces: List<SavedPosition>,
    val moves: List<SavedMove>,
    val taps: Int,
    val undos: Int,
    val elapsedMillis: Long,
)

@Serializable
data class SavedPosition(val row: Int, val column: Int)

@Serializable
data class SavedMove(val kind: SavedMoveKind, val position: SavedPosition)

@Serializable
enum class SavedMoveKind { Place, Remove }
