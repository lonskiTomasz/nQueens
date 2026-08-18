package com.queens.puzzle.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** U+0021. Drawn as text so no vector asset ships, the same way [QueenGlyph] is. */
private const val ALERT_MARK = "!"

/**
 * The disc that marks something gone wrong — the reset dialog's warning and the game screen's
 * conflict banner wear the same one, at their own sizes and colours.
 *
 * Semantics are cleared: what the badge sits beside is the message, and a screen reader that
 * says "exclamation mark" first only delays it.
 */
@Composable
fun AlertBadge(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer,
    contentColor: Color = MaterialTheme.colorScheme.onErrorContainer,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(containerColor, CircleShape)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = ALERT_MARK, style = textStyle, color = contentColor)
    }
}

