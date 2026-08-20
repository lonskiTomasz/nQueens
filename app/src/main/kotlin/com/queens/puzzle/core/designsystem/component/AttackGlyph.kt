package com.queens.puzzle.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

private const val ATTACK_MARK = "✕"

/**
 * The cross on a square a queen covers, sized to its square.
 *
 * Semantics are cleared for the reason the queen's are: the square that owns the glyph already
 * says it is attacked, and a second spoken node per marked square would flood the board.
 */
@Composable
fun AttackGlyph(
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
        Text(
            text = ATTACK_MARK,
            color = color,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(lineHeight = fontSize),
        )
    }
}
