package com.queens.puzzle.ui.designsystem.component

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

private const val QUEEN = "♛"

/**
 * Semantics are cleared: the square that owns the glyph already describes itself, and a second
 * spoken node per queen would double every board announcement.
 */
@Composable
fun QueenGlyph(
    color: Color,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
        Text(
            text = QUEEN,
            color = color,
            fontSize = fontSize,
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(lineHeight = fontSize),
        )
    }
}
