package com.queens.puzzle.ui.win

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.R
import com.queens.puzzle.common.time.DurationFormatter
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.WinSummary
import com.queens.puzzle.ui.designsystem.component.QueenGlyph
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewScreen
import com.queens.puzzle.ui.designsystem.preview.previewWinSummary
import com.queens.puzzle.ui.designsystem.theme.NumericFont
import com.queens.puzzle.ui.designsystem.theme.QueensTheme

@Composable
fun WinScreen(
    solveId: Long,
    onPlay: (Int) -> Unit,
    onSeeBestTimes: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WinViewModel = hiltViewModel<WinViewModel, WinViewModel.Factory>(
        creationCallback = { factory -> factory.create(solveId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WinScreen(
        uiState = uiState,
        onPlay = onPlay,
        onSeeBestTimes = onSeeBestTimes,
        onClose = onClose,
        modifier = modifier,
    )
}

@Composable
fun WinScreen(
    uiState: WinUiState,
    onPlay: (Int) -> Unit,
    onSeeBestTimes: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = QueensTheme.extendedColors

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to extended.winGradientTop,
                    0.6f to MaterialTheme.colorScheme.background,
                )
            ),
    ) {
        when (uiState) {
            WinUiState.Loading -> Unit
            WinUiState.Missing -> MissingSolve(onClose)
            is WinUiState.Solved -> SolvedContent(
                summary = uiState.summary,
                onPlay = onPlay,
                onSeeBestTimes = onSeeBestTimes,
                viewportHeight = maxHeight,
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.win_close),
            )
        }
    }
}

/** A window shorter than this cannot show the whole celebration at once. */
private val CompactHeight = 480.dp

/**
 * The celebration at two scales.
 *
 * A rotated phone has room for roughly half the design's vertical spend, and what has to
 * survive the cut is the time — it is what the screen is for.
 */
private data class WinMetrics(
    val topPadding: Dp,
    val badgeSize: Dp,
    val badgeGlyph: TextUnit,
    val headline: TextUnit,
    val time: TextUnit,
    val afterBadge: Dp,
    val beforeTime: Dp,
    val beforeStats: Dp,
) {
    companion object {
        val Regular = WinMetrics(
            topPadding = 72.dp,
            badgeSize = 88.dp,
            badgeGlyph = 42.sp,
            headline = 36.sp,
            time = 56.sp,
            afterBadge = 24.dp,
            beforeTime = 32.dp,
            beforeStats = 28.dp,
        )

        val Compact = WinMetrics(
            topPadding = 20.dp,
            badgeSize = 56.dp,
            badgeGlyph = 28.sp,
            headline = 28.sp,
            time = 40.sp,
            afterBadge = 12.dp,
            beforeTime = 16.dp,
            beforeStats = 16.dp,
        )
    }
}

@Composable
private fun MissingSolve(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.best_times_empty),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClose, shape = RoundedCornerShape(percent = 50)) {
            Text(stringResource(R.string.win_home))
        }
    }
}

@Composable
private fun SolvedContent(
    summary: WinSummary,
    onPlay: (Int) -> Unit,
    onSeeBestTimes: () -> Unit,
    viewportHeight: Dp,
) {
    val scrollState = rememberScrollState()
    val compact = viewportHeight < CompactHeight
    val metrics = if (compact) WinMetrics.Compact else WinMetrics.Regular

    if (compact) { // too short to show the whole celebration
        Column(modifier = Modifier.fillMaxSize()) {
            Summary(
                summary = summary,
                metrics = metrics,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(top = metrics.topPadding),
            )
            WinActions(
                boardSize = summary.solve.boardSize,
                onPlay = onPlay,
                onSeeBestTimes = onSeeBestTimes,
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .heightIn(min = viewportHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Summary(
                summary = summary,
                metrics = metrics,
                modifier = Modifier.padding(top = metrics.topPadding),
            )
            WinActions(
                boardSize = summary.solve.boardSize,
                onPlay = onPlay,
                onSeeBestTimes = onSeeBestTimes,
            )
        }
    }
}

@Composable
private fun Summary(
    summary: WinSummary,
    metrics: WinMetrics,
    modifier: Modifier = Modifier,
) {
    val extended = QueensTheme.extendedColors
    val solve = summary.solve

    // The badge springs in once, so arriving on the screen reads as an arrival.
    var badgeVisible by remember { mutableStateOf(false) }
    val badgeScale by animateFloatAsState(
        targetValue = if (badgeVisible) 1f else 0.6f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "winBadge",
    )
    LaunchedEffect(Unit) { badgeVisible = true }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(metrics.badgeSize)
                .scale(badgeScale)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            QueenGlyph(
                color = MaterialTheme.colorScheme.onTertiary,
                fontSize = metrics.badgeGlyph,
            )
        }

        Spacer(Modifier.height(metrics.afterBadge))
        Text(
            text = stringResource(R.string.win_headline),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = metrics.headline),
            color = extended.winHeadline,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.win_subtitle, solve.boardSize.value, solve.taps),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(metrics.beforeTime))
        Text(
            text = DurationFormatter.format(solve.durationMillis),
            fontFamily = NumericFont,
            fontSize = metrics.time,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )

        BestBadge(summary = summary, modifier = Modifier.padding(top = 14.dp))

        Spacer(Modifier.height(metrics.beforeStats))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                value = solve.taps.toString(),
                label = stringResource(R.string.win_stat_taps),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = solve.undos.toString(),
                label = stringResource(R.string.win_stat_undos),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                value = summary.solveCountForSize.toString(),
                label = stringResource(R.string.win_stat_solved),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WinActions(
    boardSize: BoardSize,
    onPlay: (Int) -> Unit,
    onSeeBestTimes: () -> Unit,
) {
    val next = boardSize.next

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The screen is edge-to-edge and draws its own gradient behind the system bars, so
            // the actions have to hold themselves clear of the navigation bar.
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = { onPlay((next ?: boardSize).value) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text(
                text = if (next == null) {
                    stringResource(R.string.win_play_again)
                } else {
                    stringResource(R.string.win_play_next, next.value)
                },
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
            )
        }
        TextButton(
            onClick = onSeeBestTimes,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
        ) {
            Text(
                text = stringResource(R.string.win_best_times),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun BestBadge(summary: WinSummary, modifier: Modifier = Modifier) {
    val extended = QueensTheme.extendedColors
    val improvement = summary.improvementMillis

    val (text, background, foreground) = when {
        summary.isNewBest && improvement != null -> Triple(
            stringResource(R.string.win_new_best, DurationFormatter.formatDelta(improvement)),
            extended.success,
            MaterialTheme.colorScheme.onPrimary,
        )

        summary.isNewBest -> Triple(
            stringResource(R.string.win_personal_best),
            extended.success,
            MaterialTheme.colorScheme.onPrimary,
        )

        else -> Triple(
            stringResource(
                R.string.win_slower,
                DurationFormatter.formatDelta(improvement ?: 0L),
            ),
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }

    Box(
        modifier = modifier
            .height(32.dp)
            .background(background, RoundedCornerShape(percent = 50))
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = foreground,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                fontFamily = NumericFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/** The screen it is built for: a new best, with the improvement spelled out. */
@PreviewLightDark
@Composable
private fun WinScreenNewBestPreview() {
    QueensPreviewScreen {
        WinScreen(
            uiState = WinUiState.Solved(previewWinSummary()),
            onPlay = {},
            onSeeBestTimes = {},
            onClose = {},
        )
    }
}

/** Rotated: the summary scrolls, the buttons stay put. */
@Preview(widthDp = 740, heightDp = 360)
@Composable
private fun WinScreenLandscapePreview() {
    QueensPreviewScreen {
        WinScreen(
            uiState = WinUiState.Solved(previewWinSummary()),
            onPlay = {},
            onSeeBestTimes = {},
            onClose = {},
        )
    }
}

/** A first solve of a size: a best, but with nothing to have beaten. */
@Preview
@Composable
private fun WinScreenFirstSolvePreview() {
    QueensPreviewScreen {
        WinScreen(
            uiState = WinUiState.Solved(
                previewWinSummary(improvementMillis = null, solveCountForSize = 1),
            ),
            onPlay = {},
            onSeeBestTimes = {},
            onClose = {},
        )
    }
}

/** Slower than the best — the badge has to congratulate without celebrating. */
@Preview
@Composable
private fun WinScreenSlowerPreview() {
    QueensPreviewScreen {
        WinScreen(
            uiState = WinUiState.Solved(
                previewWinSummary(isNewBest = false, improvementMillis = -54_000),
            ),
            onPlay = {},
            onSeeBestTimes = {},
            onClose = {},
        )
    }
}

/** The solve is gone — the history was cleared while this screen was open. */
@Preview
@Composable
private fun WinScreenMissingPreview() {
    QueensPreviewScreen {
        WinScreen(
            uiState = WinUiState.Missing,
            onPlay = {},
            onSeeBestTimes = {},
            onClose = {},
        )
    }
}
