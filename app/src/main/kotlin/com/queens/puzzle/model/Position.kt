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

    override fun toString(): String = "($row,$column)"
}
