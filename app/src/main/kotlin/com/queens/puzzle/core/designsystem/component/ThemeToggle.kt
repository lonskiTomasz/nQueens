package com.queens.puzzle.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.core.designsystem.preview.QueensPreviewSurface
import com.queens.puzzle.core.designsystem.theme.Dimens

/** The lit half of the track, and the distance it travels between the two segments. */
private val SegmentWidth = 52.dp
private val SegmentHeight = 40.dp

private const val THUMB_SLIDE_MILLIS = 180

@Composable
fun ThemeToggle(
    isDark: Boolean,
    onThemeSelected: (ThemePreference) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (isDark) SegmentWidth else 0.dp,
        animationSpec = tween(durationMillis = THUMB_SLIDE_MILLIS),
        label = "themeThumb",
    )

    Row(
        modifier = modifier
            .height(Dimens.MinTouchTarget)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(Dimens.BorderWidth, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .toggleable(
                value = isDark,
                role = Role.Switch,
                onValueChange = { dark ->
                    onThemeSelected(if (dark) ThemePreference.Dark else ThemePreference.Light)
                },
            )
            .semantics { contentDescription = label }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Thumb(offset = thumbOffset)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Segment {
                    Sun(lit = !isDark)
                }
                Segment {
                    Moon(lit = isDark, onThumb = isDark)
                }
            }
        }
    }
}

@Composable
private fun Thumb(offset: Dp) {
    Box(
        Modifier
            .offset(x = offset)
            .size(width = SegmentWidth, height = SegmentHeight)
            .background(MaterialTheme.colorScheme.background, CircleShape)
    )
}

@Composable
private fun Segment(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(width = SegmentWidth, height = SegmentHeight),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun Sun(lit: Boolean) {
    Box(
        Modifier
            .size(16.dp)
            .background(
                if (lit) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                CircleShape,
            )
    )
}

@Composable
private fun Moon(lit: Boolean, onThumb: Boolean) {
    val disc = if (lit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(Modifier.size(16.dp).clip(CircleShape).background(disc)) {
        // The bite that turns the disc into a crescent, painted in whatever is behind the moon.
        Box(
            Modifier
                .size(15.dp)
                .offset(x = 6.dp, y = (-4).dp)
                .background(
                    if (onThumb) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape,
                )
        )
    }
}

@PreviewLightDark
@Composable
private fun ThemeTogglePreview() {
    QueensPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemeToggle(
                isDark = false,
                onThemeSelected = {},
                label = "Dark theme",
            )
            ThemeToggle(
                isDark = true,
                onThemeSelected = {},
                label = "Dark theme",
            )
        }
    }
}
