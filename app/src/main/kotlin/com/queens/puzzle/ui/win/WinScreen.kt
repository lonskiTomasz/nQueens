package com.queens.puzzle.ui.win

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.R
import com.queens.puzzle.common.time.DurationFormatter
import com.queens.puzzle.model.WinSummary
import com.queens.puzzle.ui.designsystem.component.QueenGlyph
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

/** Stateless, so previews and Compose tests can drive it without a ViewModel. */
@Composable
fun WinScreen(
    uiState: WinUiState,
    onPlay: (Int) -> Unit,
    onSeeBestTimes: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = QueensTheme.extendedColors

    Box(
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
            )
        }

        // The way out, in the corner rather than in the stack of actions: leaving is not a
        // third thing to consider alongside the next board and the times, and in the corner it
        // keeps its place while the summary beneath it scrolls. Outside the `when`, so a screen
        // still loading its solve is never a screen with no way off it.
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
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(badgeScale)
                .background(MaterialTheme.colorScheme.tertiary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            QueenGlyph(color = MaterialTheme.colorScheme.onTertiary, fontSize = 42.sp)
        }

        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.win_headline),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
            color = extended.winHeadline,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )

        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.win_subtitle, solve.boardSize.value, solve.taps),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(32.dp))
        Text(
            text = DurationFormatter.format(solve.durationMillis),
            fontFamily = NumericFont,
            fontSize = 56.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
        )

        BestBadge(summary = summary, modifier = Modifier.padding(top = 14.dp))

        Spacer(Modifier.height(28.dp))
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

        Spacer(Modifier.weight(1f))

        // What to do next, in the order a player wants it: the board one size up, then the
        // times. Solving a board is an invitation to the next one, so the primary button offers
        // that rather than the same board again — the home screen's ladder of sizes, climbed a
        // rung. At the largest board there is no rung above, and the offer falls back to
        // playing it again.
        val next = solve.boardSize.next

        Button(
            onClick = { onPlay((next ?: solve.boardSize).value) },
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

/** "NEW BEST · −54s" when the solve beat the previous best, and nothing loud when it did not. */
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
