package com.queens.puzzle.model

/**
 * The kind of line two queens share, used to name the conflict on screen.
 *
 * Both diagonal orientations report as [Diagonal]. [Knight] is the knights variant's only
 * kind — an L-shaped attack has no row/column/diagonal to name.
 */
enum class ConflictKind {
    Row,
    Column,
    Diagonal,
    Knight,
}
