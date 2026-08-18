package com.queens.puzzle.ui.designsystem.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.queens.puzzle.ui.designsystem.theme.QueensTheme

/**
 * A themed background for a preview.
 *
 * [QueensTheme] is left to its default `darkTheme`, which reads `isSystemInDarkTheme()` — that
 * is what lets `@PreviewLightDark` render both schemes from one function. The app itself always
 * passes the player's stored choice instead.
 */
@Composable
fun QueensPreviewSurface(
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    QueensTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(Modifier.padding(padding)) { content() }
        }
    }
}

/** A full screen, with no padding of its own. */
@Composable
fun QueensPreviewScreen(content: @Composable () -> Unit) {
    QueensTheme {
        Surface(color = MaterialTheme.colorScheme.background) { content() }
    }
}
