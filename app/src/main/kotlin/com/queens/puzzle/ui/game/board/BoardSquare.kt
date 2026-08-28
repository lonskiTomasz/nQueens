package com.queens.puzzle.ui.game.board

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queens.puzzle.model.Position
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.core.designsystem.component.AttackGlyph
import com.queens.puzzle.core.designsystem.component.PieceGlyph
import com.queens.puzzle.core.designsystem.preview.QueensPreviewSurface
import com.queens.puzzle.core.designsystem.theme.QueensTheme

private const val CONFLICT_FADE_MILLIS = 120

/** The cross marks a square rather than occupying it, so it is drawn half the queen's size. */
private const val ATTACK_GLYPH_RATIO = 0.5f

/**
 * A button, with a spoken description of the form "Row 3, column 5, queen, in conflict" — which
 * is also what lets the Compose tests select squares by meaning rather than by pixel.
 */
@Composable
fun BoardSquare(
    state: BoardSquareState,
    puzzleType: PuzzleType,
    glyphSize: TextUnit,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val extended = QueensTheme.extendedColors

    val squareColor =
        if (state.isDarkSquare) extended.boardSquareDark else extended.boardSquareLight

    // Cross-fades rather than switching, so a conflict appearing does not flash the board.
    val tint by animateColorAsState(
        targetValue = if (state.isConflicting) extended.conflictTint else Color.Transparent,
        animationSpec = tween(durationMillis = CONFLICT_FADE_MILLIS),
        label = "conflictTint",
    )
    // A square a piece covers is crossed out rather than shaded: a tint that reads on both
    // square shades is too faint to notice, and the cross is unambiguous at any board size.
    val showAttackMark = state.isAttacked && !state.hasPiece
    val attackMarkAlpha by animateFloatAsState(
        targetValue = if (showAttackMark) 1f else 0f,
        animationSpec = tween(durationMillis = CONFLICT_FADE_MILLIS),
        label = "attackMark",
    )
    val attackMarkColor = if (state.isDarkSquare) {
        extended.attackMarkOnDarkSquare
    } else {
        extended.attackMarkOnLightSquare
    }

    // The piece springs in rather than appearing, which reads as placing it.
    val glyphScale by animateFloatAsState(
        targetValue = if (state.hasPiece) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pieceScale",
    )

    Box(
        modifier = modifier
            .background(squareColor)
            .background(tint)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center,
    ) {
        if (attackMarkAlpha > 0f) {
            AttackGlyph(
                color = attackMarkColor,
                fontSize = glyphSize * ATTACK_GLYPH_RATIO,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(attackMarkAlpha),
            )
        }

        if (glyphScale > 0f) {
            PieceGlyph(
                puzzleType = puzzleType,
                color = when {
                    state.isConflicting -> extended.queenConflict
                    state.isDarkSquare -> extended.queenOnDarkSquare
                    else -> extended.queenOnLightSquare
                },
                fontSize = glyphSize,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(glyphScale),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BoardSquareStatesPreview() {
    QueensPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(
                BoardSquareState(Position(0, 0)),
                BoardSquareState(Position(0, 0), isAttacked = true),
                BoardSquareState(Position(0, 0), hasPiece = true),
                BoardSquareState(Position(0, 0), hasPiece = true, isConflicting = true),
            ).forEach { state ->
                BoardSquare(
                    state = state,
                    puzzleType = PuzzleType.Queens,
                    glyphSize = 38.sp,
                    contentDescription = "",
                    onClick = {},
                    modifier = Modifier.size(84.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun BoardSquareOnDarkSquarePreview() {
    QueensPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf(
                BoardSquareState(Position(0, 1)),
                BoardSquareState(Position(0, 1), isAttacked = true),
                BoardSquareState(Position(0, 1), hasPiece = true),
                BoardSquareState(Position(0, 1), hasPiece = true, isConflicting = true),
            ).forEach { state ->
                BoardSquare(
                    state = state,
                    puzzleType = PuzzleType.Queens,
                    glyphSize = 38.sp,
                    contentDescription = "",
                    onClick = {},
                    modifier = Modifier.size(84.dp),
                )
            }
        }
    }
}
