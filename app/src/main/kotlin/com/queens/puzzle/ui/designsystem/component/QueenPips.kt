package com.queens.puzzle.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewSurface

@Composable
fun QueenPips(
    total: Int,
    placed: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < placed) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun QueenPipsPreview() {
    QueensPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QueenPips(total = 8, placed = 0)
            QueenPips(total = 8, placed = 5)
            QueenPips(total = 8, placed = 8)
            // The widest board: twelve pips still have to fit beside the counter.
            QueenPips(total = 12, placed = 7)
        }
    }
}
