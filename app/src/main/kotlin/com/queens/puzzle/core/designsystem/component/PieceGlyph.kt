package com.queens.puzzle.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import com.queens.puzzle.model.PuzzleType

/** [QueenGlyph] or [KnightGlyph], picked by which piece is in play. */
@Composable
fun PieceGlyph(
    puzzleType: PuzzleType,
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    when (puzzleType) {
        PuzzleType.Queens -> QueenGlyph(color = color, fontSize = fontSize, modifier = modifier)
        PuzzleType.Knights -> KnightGlyph(color = color, fontSize = fontSize, modifier = modifier)
    }
}
