package com.queens.puzzle.model

/**
 * A square on the board, zero-indexed from the top-left.
 *
 * [diagonal] and [antiDiagonal] identify the two diagonals through the square: squares on a
 * top-left-to-bottom-right diagonal share `row - column`, and squares on a
 * top-right-to-bottom-left diagonal share `row + column`.
 */
data class Position(val row: Int, val column: Int) {

    val diagonal: Int get() = row - column

    val antiDiagonal: Int get() = row + column

    /** True when a queen on this square attacks [other] — same row, column, or diagonal. */
    fun attacks(other: Position): Boolean =
        this != other && (
            row == other.row ||
                column == other.column ||
                diagonal == other.diagonal ||
                antiDiagonal == other.antiDiagonal
            )

    override fun toString(): String = "($row,$column)"
}
