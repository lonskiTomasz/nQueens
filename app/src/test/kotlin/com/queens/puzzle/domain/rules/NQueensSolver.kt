package com.queens.puzzle.domain.rules

import com.queens.puzzle.model.Position

/**
 * A backtracking N-Queens solver, used as an independent oracle for [BoardEvaluator].
 *
 * Test-source only: nothing in the app calls it.
 *
 * Places one queen per row, tracking occupancy in three bitmasks — columns, descending
 * diagonals (`row - column`, biased to stay non-negative) and ascending diagonals
 * (`row + column`). Each is a single Long, so a candidate square is checked and claimed with
 * bit operations rather than a scan.
 */
object NQueensSolver {

    /** Every distinct solution for [n], each a set of one queen per row. */
    fun solutions(n: Int): List<Set<Position>> {
        require(n >= 0) { "Board size must not be negative" }
        val found = mutableListOf<Set<Position>>()
        place(n, row = 0, columns = 0L, diagonals = 0L, antiDiagonals = 0L, queens = ArrayDeque()) {
            found += it
        }
        return found
    }

    /** The first solution for [n], or null when none exists. */
    fun firstSolution(n: Int): Set<Position>? = solutions(n).firstOrNull()

    fun countSolutions(n: Int): Int = solutions(n).size

    private fun place(
        n: Int,
        row: Int,
        columns: Long,
        diagonals: Long,
        antiDiagonals: Long,
        queens: ArrayDeque<Position>,
        onSolution: (Set<Position>) -> Unit,
    ) {
        if (row == n) {
            onSolution(queens.toSet())
            return
        }
        for (column in 0 until n) {
            val columnBit = 1L shl column
            val diagonalBit = 1L shl (row - column + n - 1)
            val antiDiagonalBit = 1L shl (row + column)

            val occupied = columns and columnBit != 0L ||
                diagonals and diagonalBit != 0L ||
                antiDiagonals and antiDiagonalBit != 0L
            if (occupied) continue

            queens.addLast(Position(row, column))
            place(
                n = n,
                row = row + 1,
                columns = columns or columnBit,
                diagonals = diagonals or diagonalBit,
                antiDiagonals = antiDiagonals or antiDiagonalBit,
                queens = queens,
                onSolution = onSolution,
            )
            queens.removeLast()
        }
    }
}
