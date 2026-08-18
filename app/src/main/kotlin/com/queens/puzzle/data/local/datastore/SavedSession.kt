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
 */
@Serializable
data class SavedSession(
    val boardSize: Int,
    val queens: List<SavedPosition>,
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
