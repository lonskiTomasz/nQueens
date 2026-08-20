package com.queens.puzzle.ui.besttimes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.R
import com.queens.puzzle.core.util.time.DurationFormatter
import com.queens.puzzle.core.util.time.RelativeDay
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.core.designsystem.component.SizeChip
import com.queens.puzzle.core.designsystem.preview.PreviewState
import com.queens.puzzle.core.designsystem.preview.QueensPreviewScreen
import com.queens.puzzle.core.designsystem.theme.Dimens
import com.queens.puzzle.core.designsystem.theme.NumericFont
import com.queens.puzzle.core.designsystem.theme.QueensTheme
import com.queens.puzzle.core.designsystem.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BestTimesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BestTimesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BestTimesScreen(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onClearHistory = viewModel::onClearHistory,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun BestTimesScreen(
    uiState: BestTimesUiState,
    onFilterSelected: (BoardSize?) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TopBar(onNavigateBack = onNavigateBack, onClearHistory = onClearHistory)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.ContentGap)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.ScreenPaddingHorizontal),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SizeChip(
                    label = stringResource(R.string.best_times_filter_all),
                    selected = uiState.selectedFilter == null,
                    onClick = { onFilterSelected(null) },
                )
                uiState.filters.forEach { size ->
                    SizeChip(
                        label = stringResource(R.string.best_times_filter_size, size.value),
                        selected = uiState.selectedFilter == size,
                        onClick = { onFilterSelected(size) },
                    )
                }
            }

            if (uiState.isEmpty) {
                Text(
                    text = stringResource(R.string.best_times_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 48.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.rows, key = { it.solve.id }) { row -> SolveRowItem(row) }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    onNavigateBack: () -> Unit,
    onClearHistory: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
            .padding(horizontal = Spacing.TopBarIconInset),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.game_back),
            )
        }
        Text(
            text = stringResource(R.string.best_times_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.best_times_menu),
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.best_times_clear_history)) },
                    onClick = {
                        menuOpen = false
                        onClearHistory()
                    },
                )
            }
        }
    }
}

@Composable
private fun SolveRowItem(row: SolveRow) {
    val extended = QueensTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (row.isBestForSize) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                }
            )
            .padding(Spacing.CardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.ListItemGap),
    ) {
        MiniBoard(boardSize = row.solve.boardSize)

        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    R.string.best_times_board_label,
                    row.solve.boardSize.value,
                ),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = row.occurred.label(row.solve.completedAtMillis),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.outline,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = DurationFormatter.format(row.solve.durationMillis),
                fontFamily = NumericFont,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (row.isBestForSize) extended.success else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = row.deltaMillis
                    ?.let { DurationFormatter.formatDelta(it) }
                    ?: stringResource(R.string.best_times_no_delta),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun MiniBoard(boardSize: BoardSize) {
    val light = QueensTheme.extendedColors.boardSquareLight
    val dark = QueensTheme.extendedColors.boardSquareDark

    Canvas(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .clearAndSetSemantics { },
    ) {
        val side = size.width / boardSize.value
        repeat(boardSize.value) { row ->
            repeat(boardSize.value) { column ->
                drawRect(
                    color = if ((row + column) % 2 == 1) dark else light,
                    topLeft = Offset(column * side, row * side),
                    size = Size(side, side),
                )
            }
        }
    }
}

@Composable
private fun RelativeDay.label(completedAtMillis: Long): String = when (this) {
    RelativeDay.Today -> stringResource(R.string.date_today, clockTime(completedAtMillis))
    RelativeDay.Yesterday -> stringResource(R.string.date_yesterday)
    is RelativeDay.DaysAgo -> pluralStringResource(R.plurals.date_days_ago, days, days)
    RelativeDay.LastWeek -> stringResource(R.string.date_last_week)
    is RelativeDay.WeeksAgo -> pluralStringResource(R.plurals.date_weeks_ago, weeks, weeks)
}

private fun clockTime(millis: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

@PreviewLightDark
@Composable
private fun BestTimesScreenPreview(
    @PreviewParameter(BestTimesScreenPreviewProvider::class) preview: PreviewState<BestTimesUiState>,
) {
    QueensPreviewScreen {
        BestTimesScreen(
            uiState = preview.state,
            onFilterSelected = {},
            onClearHistory = {},
            onNavigateBack = {},
        )
    }
}
