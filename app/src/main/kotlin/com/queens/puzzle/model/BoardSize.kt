package com.queens.puzzle.model

/**
 * The edge length of a square board. Values outside [MIN]..[MAX] are rejected at construction.
 *
 * Below [MIN] the N-Queens problem has no solution; [MAX] is a playability limit.
 */
@JvmInline
value class BoardSize(val value: Int) {

    init {
        require(value in MIN..MAX) { "Board size must be in $MIN..$MAX, was $value" }
    }

    /** Number of squares on the board. */
    val squareCount: Int get() = value * value

    /**
     * The next size up among [selectable], or `null` at the largest one.
     *
     * What the win screen offers once a board is solved: the ladder the home screen lays out,
     * climbed one rung. Sizes off that ladder (a stored 9, say) still find the next rung above
     * them rather than falling to `null`.
     */
    val next: BoardSize? get() = selectable.firstOrNull { it.value > value }

    /** Every position on the board, in row-major order. */
    fun positions(): List<Position> =
        (0 until value).flatMap { row -> (0 until value).map { column -> Position(row, column) } }

    operator fun contains(position: Position): Boolean =
        position.row in 0 until value && position.column in 0 until value

    override fun toString(): String = "${value}x$value"

    companion object {
        const val MIN = 4
        const val MAX = 12

        /** Sizes offered on the home screen. */
        val selectable: List<BoardSize> = listOf(4, 5, 6, 7, 8, 10, 12).map(::BoardSize)

        val Default: BoardSize = BoardSize(8)

        /** Returns the size, or `null` when [value] is out of range. */
        fun ofOrNull(value: Int): BoardSize? = if (value in MIN..MAX) BoardSize(value) else null
    }
}
