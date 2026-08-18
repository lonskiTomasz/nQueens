package com.queens.puzzle.ui.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.queens.puzzle.R
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.Position
import com.queens.puzzle.ui.designsystem.preview.PreviewQueens
import com.queens.puzzle.ui.designsystem.preview.PreviewSolvedQueens
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewSurface
import com.queens.puzzle.ui.designsystem.preview.previewSquares

/** The glyph fills about two thirds of its square, matching the spec's 30 sp inside 45 dp. */
private const val GLYPH_SIZE_RATIO = 0.66f

/**
 * The board, and nothing else.
 *
 * Takes squares and a click lambda — it has no ViewModel and knows nothing about the game,
 * which is what lets it be previewed at every size and reused on the win and history screens.
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                RoundedCornerShape(16.dp),
            ),
    ) {
        val glyphSize = with(LocalDensity.current) {
            (maxWidth / boardSize.value * GLYPH_SIZE_RATIO).toSp()
        }

        Column(Modifier.fillMaxWidth()) {
            repeat(boardSize.value) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(boardSize.value) { column ->
                        val position = Position(row, column)
                        val state = byPosition[position] ?: BoardSquareState(position)

                        BoardSquare(
                            state = state,
                            glyphSize = glyphSize,
                            contentDescription = state.describe(),
                            onClick = { onSquareClick(position) },
                            enabled = enabled,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
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

/**
 * The board carries no game state of its own, so it previews at any size from sample squares
 * alone — the reason it is a standalone widget rather than part of the game screen.
 */
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

/** Mid-game: five queens down, two of them sharing a diagonal, attack lines on. */
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

/** The smallest board there is, solved. */
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

/** The largest board, where the squares are at their smallest. */
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
