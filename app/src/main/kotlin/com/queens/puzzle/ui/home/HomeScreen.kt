package com.queens.puzzle.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.R
import com.queens.puzzle.core.util.time.DurationFormatter
import com.queens.puzzle.model.BestTime
import com.queens.puzzle.model.BoardSize
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.core.designsystem.component.PieceGlyph
import com.queens.puzzle.core.designsystem.component.PieceModeToggle
import com.queens.puzzle.core.designsystem.component.QueenGlyph
import com.queens.puzzle.core.designsystem.preview.PreviewState
import com.queens.puzzle.core.designsystem.preview.QueensPreviewScreen
import com.queens.puzzle.core.designsystem.component.SizeChip
import com.queens.puzzle.core.designsystem.component.ThemeToggle
import com.queens.puzzle.core.designsystem.theme.Dimens
import com.queens.puzzle.core.designsystem.theme.NumericFont
import com.queens.puzzle.core.designsystem.theme.Spacing

@Composable
fun HomeScreen(
    onStartGame: (BoardSize, PuzzleType, Long) -> Unit,
    onResumeGame: (ResumableGame) -> Unit,
    onSeeAllBestTimes: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onSizeSelected = viewModel::onSizeSelected,
        onPuzzleTypeSelected = viewModel::onPuzzleTypeSelected,
        onThemeSelected = viewModel::onThemeSelected,
        onStartGame = { boardSize -> onStartGame(boardSize, uiState.puzzleType, viewModel.newGameId()) },
        onResumeGame = onResumeGame,
        onSeeAllBestTimes = onSeeAllBestTimes,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSizeSelected: (BoardSize) -> Unit,
    onPuzzleTypeSelected: (PuzzleType) -> Unit,
    onThemeSelected: (ThemePreference) -> Unit,
    onStartGame: (BoardSize) -> Unit,
    onResumeGame: (ResumableGame) -> Unit,
    onSeeAllBestTimes: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            HomeTopBar(
                isDark = uiState.theme.shouldUseDarkTheme(isSystemInDarkTheme()),
                onThemeSelected = onThemeSelected,
            )

            Column(Modifier.padding(horizontal = Spacing.ScreenPaddingHorizontal)) {
                Spacer(Modifier.height(Spacing.ContentGap))
                PieceModeToggle(
                    selected = uiState.puzzleType,
                    onModeSelected = onPuzzleTypeSelected,
                    queensLabel = stringResource(R.string.piece_mode_queens),
                    knightsLabel = stringResource(R.string.piece_mode_knights),
                )
                Spacer(Modifier.height(Spacing.ContentGap))
                Text(
                    text = stringResource(
                        when (uiState.puzzleType) {
                            PuzzleType.Queens -> R.string.home_headline
                            PuzzleType.Knights -> R.string.home_headline_knights
                        },
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(Spacing.TextGap))
                Text(
                    text = stringResource(
                        when (uiState.puzzleType) {
                            PuzzleType.Queens -> R.string.home_subtitle
                            PuzzleType.Knights -> R.string.home_subtitle_knights
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.SectionGap))
                Text(
                    text = stringResource(R.string.home_board_size),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.LabelGap))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.ChipGap),
                    verticalArrangement = Arrangement.spacedBy(Spacing.ChipGap),
                ) {
                    uiState.sizes.forEach { size ->
                        SizeChip(
                            label = size.value.toString(),
                            selected = size == uiState.selectedSize,
                            onClick = { onSizeSelected(size) },
                            contentDescription = stringResource(
                                R.string.home_board_size_option,
                                size.value,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.ActionGap))
                Button(
                    onClick = { onStartGame(uiState.selectedSize) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.PrimaryButtonHeight),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    Text(
                        text = stringResource(R.string.home_start_game, uiState.selectedSize.value),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
                    )
                }

                if (uiState.canResume) {
                    TextButton(
                        onClick = { onResumeGame(uiState.resumable!!) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.SecondaryButtonHeight),
                    ) {
                        Text(
                            text = stringResource(R.string.home_resume),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                } else {
                    Spacer(Modifier.height(Spacing.SectionGap))
                }
            }

            BestTimesCard(
                bestTimes = uiState.bestTimes,
                onSeeAll = onSeeAllBestTimes,
                modifier = Modifier
                    .padding(
                        start = Spacing.ScreenPaddingHorizontal,
                        end = Spacing.ScreenPaddingHorizontal,
                        top = Spacing.ContentGap,
                        bottom = Spacing.ScreenPaddingVertical,
                    ),
            )
        }
    }
}

@Composable
private fun HomeTopBar(
    isDark: Boolean,
    onThemeSelected: (ThemePreference) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.TopBarHeight)
            .padding(horizontal = Spacing.ScreenPaddingHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.ContentGap),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                QueenGlyph(
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 18.sp,
                )
            }
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.titleLarge,
            )
        }

        ThemeToggle(
            isDark = isDark,
            onThemeSelected = onThemeSelected,
            label = stringResource(R.string.home_theme_dark),
        )
    }
}

@Composable
private fun BestTimesCard(
    bestTimes: List<BestTime>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(
                    start = Spacing.ScreenPaddingHorizontal,
                    end = Spacing.TightPadding,
                    top = Spacing.TightPadding,
                    bottom = Spacing.TightPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.home_best_times),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (bestTimes.isNotEmpty()) {
                TextButton(onClick = onSeeAll) {
                    Text(
                        text = stringResource(R.string.home_see_all),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.ScreenPaddingHorizontal))

        if (bestTimes.isEmpty()) {
            Text(
                text = stringResource(R.string.home_no_best_times),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = Spacing.ScreenPaddingHorizontal,
                    vertical = Spacing.ContentGap,
                ),
            )
        } else {
            bestTimes.forEach { best ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = Spacing.ScreenPaddingHorizontal),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        PieceGlyph(
                            puzzleType = best.puzzleType,
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 15.sp,
                        )
                        Text(
                            text = stringResource(
                                R.string.best_times_board_label,
                                best.boardSize.value,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    Text(
                        text = DurationFormatter.format(best.bestMillis),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = NumericFont,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun HomeScreenThemePreview(
    @PreviewParameter(HomeScreenPreviewProvider::class) preview: PreviewState<HomeUiState>,
) {
    QueensPreviewScreen {
        HomeScreen(
            uiState = preview.state,
            onSizeSelected = {},
            onPuzzleTypeSelected = {},
            onThemeSelected = {},
            onStartGame = {},
            onResumeGame = {},
            onSeeAllBestTimes = {},
        )
    }
}

@Preview(widthDp = 320, heightDp = 560)
@Composable
private fun HomeScreenSmallScreenPreview() {
    QueensPreviewScreen {
        HomeScreen(
            uiState = previewHomeUiState(resumableSize = 8),
            onSizeSelected = {},
            onPuzzleTypeSelected = {},
            onThemeSelected = {},
            onStartGame = {},
            onResumeGame = {},
            onSeeAllBestTimes = {},
        )
    }
}
