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

private const val KNIGHT = "♞"

/**
 * Semantics are cleared: the square that owns the glyph already describes itself, and a second
 * spoken node per knight would double every board announcement.
 */
@Composable
fun KnightGlyph(
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
        Text(
            text = KNIGHT,
            color = color,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(lineHeight = fontSize),
        )
    }
}
