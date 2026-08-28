package com.queens.puzzle.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queens.puzzle.core.designsystem.preview.QueensPreviewSurface
import com.queens.puzzle.core.designsystem.theme.Dimens
import com.queens.puzzle.model.PuzzleType

/** Wide enough for the longer of the two labels ("Knights") plus its glyph. */
private val SegmentWidth = 108.dp
private val SegmentHeight = 40.dp

private const val THUMB_SLIDE_MILLIS = 180

/**
 * The Home screen's Queens/Knights switch — the same sliding-thumb shape as [ThemeToggle], with
 * a piece glyph and label per segment instead of a sun/moon.
 */
@Composable
fun PieceModeToggle(
    selected: PuzzleType,
    onModeSelected: (PuzzleType) -> Unit,
    queensLabel: String,
    knightsLabel: String,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (selected == PuzzleType.Queens) 0.dp else SegmentWidth,
        animationSpec = tween(durationMillis = THUMB_SLIDE_MILLIS),
        label = "pieceModeThumb",
    )

    Row(
        modifier = modifier
            .height(Dimens.MinTouchTarget)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(percent = 50))
            .selectableGroup()
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(width = SegmentWidth, height = SegmentHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.background),
            )

            Row {
                Segment(
                    puzzleType = PuzzleType.Queens,
                    label = queensLabel,
                    selected = selected == PuzzleType.Queens,
                    onClick = { onModeSelected(PuzzleType.Queens) },
                )
                Segment(
                    puzzleType = PuzzleType.Knights,
                    label = knightsLabel,
                    selected = selected == PuzzleType.Knights,
                    onClick = { onModeSelected(PuzzleType.Knights) },
                )
            }
        }
    }
}

@Composable
private fun Segment(
    puzzleType: PuzzleType,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .size(width = SegmentWidth, height = SegmentHeight)
            .clip(RoundedCornerShape(percent = 50))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val contentColor = if (selected) {
            MaterialTheme.colorScheme.onBackground
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
        PieceGlyph(puzzleType = puzzleType, color = contentColor, fontSize = 16.sp)
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 6.dp),
        )
    }
}

@PreviewLightDark
@Composable
private fun PieceModeTogglePreview() {
    QueensPreviewSurface {
        PieceModeToggle(
            selected = PuzzleType.Queens,
            onModeSelected = {},
            queensLabel = "Queens",
            knightsLabel = "Knights",
        )
    }
}
