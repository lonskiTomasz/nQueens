package com.queens.puzzle.ui.game.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.min
import com.queens.puzzle.R
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Position
import com.queens.puzzle.core.designsystem.preview.QueensPreviewSurface
import com.queens.puzzle.core.designsystem.theme.Dimens

/** The glyph fills about two thirds of its square */
private const val GLYPH_SIZE_RATIO = 0.66f

/**
 * Takes the largest square that fits the space it is handed, and takes no more room than that
 * — so whatever is laid out beneath it sits against the board's own edge rather than against
 * the bottom of the space the board was offered. Sizing off the width alone is what broke
 * landscape: a board as wide as a 640 dp window is 640 dp tall, so it ran off the bottom of a
 * 360 dp one and took the buttons with it.
 */
@Composable
fun BoardGrid(
    boardSize: BoardSize,
    squares: List<BoardSquareState>,
    onSquareClick: (Position) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val byPosition = squares.associateBy { it.position }

    BoxWithConstraints(modifier = modifier) {
        // An unbounded axis reports Dp.Infinity, so this picks the bounded one — which is what
        // makes the board still work inside a scrolling column.
        val side = min(maxWidth, maxHeight)
        val glyphSize = with(LocalDensity.current) {
            (side / boardSize.value * GLYPH_SIZE_RATIO).toSp()
        }

        Column(
            modifier = Modifier
                .size(DpSize(side, side))
                .border(
                    BorderStroke(Dimens.BorderWidth, MaterialTheme.colorScheme.surfaceVariant),
                ),
        ) {
            repeat(boardSize.value) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    repeat(boardSize.value) { column ->
                        val position = Position(row, column)
                        val state = byPosition[position] ?: BoardSquareState(position)

                        BoardSquare(
                            state = state,
                            glyphSize = glyphSize,
                            contentDescription = state.describe(),
                            onClick = { onSquareClick(position) },
                            enabled = enabled,
                            // Weights rather than an aspect ratio: the container is already
                            // square, and dividing it leaves no rounding to overflow with.
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

/** A read-only board, for the win screen and history rows. */
@Composable
fun BoardGrid(
    boardSize: BoardSize,
    squares: List<BoardSquareState>,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    BoardGrid(
        boardSize = boardSize,
        squares = squares,
        onSquareClick = {},
        enabled = false,
        modifier = if (contentDescription == null) {
            modifier
        } else {
            modifier.semantics {
                this.contentDescription = contentDescription
            }
        },
    )
}

/** "Row 3, column 5, queen, in conflict" — rows and columns are spoken one-indexed. */
@Composable
private fun BoardSquareState.describe(): String {
    val occupancy = when {
        hasQueen && isConflicting -> stringResource(R.string.board_square_queen_conflict)
        hasQueen -> stringResource(R.string.board_square_queen)
        isAttacked -> stringResource(R.string.board_square_attacked)
        else -> stringResource(R.string.board_square_empty)
    }
    return stringResource(
        R.string.board_square_description,
        position.row + 1,
        position.column + 1,
        occupancy,
    )
}

@Preview(widthDp = 340)
@Composable
private fun BoardGridEmptyPreview() {
    QueensPreviewSurface {
        BoardGrid(
            boardSize = BoardSize(8),
            squares = emptyList(),
            onSquareClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun BoardGridInPlayPreview() {
    QueensPreviewSurface {
        BoardGrid(
            boardSize = BoardSize(8),
            squares = previewSquares(BoardSize(8), PreviewQueens),
            onSquareClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun BoardGridSolvedPreview() {
    QueensPreviewSurface {
        BoardGrid(
            boardSize = BoardSize(4),
            squares = previewSquares(BoardSize(4), PreviewSolvedQueens),
            onSquareClick = {},
        )
    }
}

@Preview(widthDp = 340)
@Composable
private fun BoardGridLargestPreview() {
    QueensPreviewSurface {
        BoardGrid(
            boardSize = BoardSize(12),
            squares = previewSquares(BoardSize(12), setOf(Position(0, 0), Position(5, 7))),
            onSquareClick = {},
        )
    }
}
