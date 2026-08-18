package com.queens.puzzle.model

/**
 * The kind of line two queens share, used to name the conflict on screen.
 *
 * Both diagonal orientations report as [Diagonal].
 */
enum class ConflictKind {
    Row,
    Column,
    Diagonal,
}
