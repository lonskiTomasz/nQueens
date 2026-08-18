package com.queens.puzzle.ui.game

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.R
import com.queens.puzzle.common.time.DurationFormatter
import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.model.Position
import com.queens.puzzle.ui.board.BoardGrid
import com.queens.puzzle.ui.designsystem.component.AlertBadge
import com.queens.puzzle.ui.designsystem.component.QueenPips
import com.queens.puzzle.ui.designsystem.preview.PreviewSolvedQueens
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewScreen
import com.queens.puzzle.ui.designsystem.preview.previewGameUiState
import com.queens.puzzle.ui.designsystem.component.TimerChip
import com.queens.puzzle.ui.feedback.GameFeedback
import com.queens.puzzle.ui.feedback.rememberGameFeedback

@Composable
fun GameScreen(
    boardSize: Int,
    resume: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToWin: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel<GameViewModel, GameViewModel.Factory>(
        creationCallback = { factory -> factory.create(boardSize, resume) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feedback = rememberGameFeedback(enabled = uiState.settings.hapticsEnabled)
    val snackbarHostState = remember { SnackbarHostState() }
    val boardFullMessage = stringResource(R.string.game_board_full)

    LaunchedEffect(viewModel, feedback) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GameEffect.HapticPlace -> feedback.place()
                GameEffect.HapticConflict -> feedback.conflict()
                GameEffect.BoardFull -> {
                    feedback.conflict()
                    // The reducer refuses silently; say why rather than looking broken.
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(boardFullMessage)
                }
                GameEffect.CelebrateWin -> feedback.win()
                is GameEffect.NavigateToWin -> onNavigateToWin(effect.solveId)
            }
        }
    }

    GameScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onResetRequested = viewModel::onResetRequested,
        onResetConfirmed = viewModel::onResetConfirmed,
        onResetDismissed = viewModel::onResetDismissed,
        onSettingsOpened = viewModel::onSettingsOpened,
        onSettingsDismissed = viewModel::onSettingsDismissed,
        onShowAttackLinesChanged = viewModel::onShowAttackLinesChanged,
        onHapticsChanged = viewModel::onHapticsChanged,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

/** Stateless, so previews and Compose tests can drive it without a ViewModel. */
@Composable
fun GameScreen(
    uiState: GameUiState,
    onAction: (GameAction) -> Unit,
    onNavigateBack: () -> Unit,
    onResetRequested: () -> Unit,
    onResetConfirmed: () -> Unit,
    onResetDismissed: () -> Unit,
    onSettingsOpened: () -> Unit,
    onSettingsDismissed: () -> Unit,
    onShowAttackLinesChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            GameTopBar(
                title = stringResource(R.string.game_board_label, uiState.boardSize.value),
                elapsed = DurationFormatter.format(uiState.elapsedMillis),
                onNavigateBack = onNavigateBack,
                onSettingsOpened = onSettingsOpened,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.game_queens_left,
                            uiState.queensRemaining,
                            uiState.queensRemaining,
                        ),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        ),
                    )
                    QueenPips(total = uiState.boardSize.value, placed = uiState.queensPlaced)
                }

                Spacer(Modifier.height(16.dp))

                BoardGrid(
                    boardSize = uiState.boardSize,
                    squares = uiState.squares,
                    onSquareClick = { onAction(GameAction.TapSquare(it)) },
                    enabled = !uiState.isSolved,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                AnimatedVisibility(visible = uiState.hasConflicts) {
                    ConflictBanner(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onAction(GameAction.Undo) },
                    enabled = uiState.canUndo,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    Text(stringResource(R.string.game_undo))
                }
                OutlinedButton(
                    onClick = onResetRequested,
                    enabled = uiState.canReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(percent = 50),
                ) {
                    Text(stringResource(R.string.game_reset))
                }
            }
        }
    }

    if (uiState.isResetDialogVisible) {
        ResetConfirmDialog(
            queensPlaced = uiState.queensPlaced,
            onConfirm = onResetConfirmed,
            onDismiss = onResetDismissed,
        )
    }

    if (uiState.isSettingsSheetVisible) {
        GameSettingsSheet(
            settings = uiState.settings,
            onShowAttackLinesChanged = onShowAttackLinesChanged,
            onHapticsChanged = onHapticsChanged,
            onDismiss = onSettingsDismissed,
        )
    }
}

@Composable
private fun GameTopBar(
    title: String,
    elapsed: String,
    onNavigateBack: () -> Unit,
    onSettingsOpened: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.game_back),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        TimerChip(time = elapsed, contentDescription = stringResource(R.string.game_elapsed))
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onSettingsOpened) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.game_settings),
                )
            }
        }
    }
}

/** The banner's mark, small enough to ride inside a label-height row. */
private val BannerBadgeSize = 24.dp

/**
 * One sentence, whatever the queens share.
 *
 * The evaluator knows whether it is a row, a column or a diagonal, and the banner used to say
 * so — but the board is already marking the squares at fault, and naming the line only put
 * that into words. Saying it once is also what keeps the banner a fixed height, since the
 * sentence can no longer change under it.
 */
@Composable
private fun ConflictBanner(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // The reset dialog's warning mark, shrunk: both say the same thing, so they look the
        // same. Inverted colours, because the dialog's disc would vanish into this background.
        AlertBadge(
            size = BannerBadgeSize,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            textStyle = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = stringResource(R.string.game_conflict),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

/** A fresh board: nothing placed, undo and reset both off. */
@PreviewLightDark
@Composable
private fun GameScreenFreshPreview() {
    QueensPreviewScreen {
        PreviewGameScreen(previewGameUiState(queens = emptySet(), elapsedMillis = 0))
    }
}

/** Mid-game, with two queens sharing a diagonal — the banner names the line. */
@PreviewLightDark
@Composable
private fun GameScreenInConflictPreview() {
    QueensPreviewScreen {
        PreviewGameScreen(previewGameUiState())
    }
}

/** The board with attack lines switched off, which is the same board reading differently. */
@Preview
@Composable
private fun GameScreenWithoutAttackLinesPreview() {
    QueensPreviewScreen {
        PreviewGameScreen(
            previewGameUiState(settings = GameSettings(showAttackLines = false)),
        )
    }
}

/** The largest board, where the squares and the glyph are at their smallest. */
@Preview
@Composable
private fun GameScreenLargestBoardPreview() {
    QueensPreviewScreen {
        PreviewGameScreen(
            previewGameUiState(
                boardSize = 12,
                queens = setOf(Position(0, 0), Position(2, 5), Position(7, 11)),
            ),
        )
    }
}

/** Solved: the 4x4 board with every queen at peace. */
@Preview
@Composable
private fun GameScreenSolvedPreview() {
    QueensPreviewScreen {
        PreviewGameScreen(
            previewGameUiState(boardSize = 4, queens = PreviewSolvedQueens, elapsedMillis = 12_000),
        )
    }
}

@Composable
private fun PreviewGameScreen(uiState: GameUiState) {
    GameScreen(
        uiState = uiState,
        onAction = {},
        onNavigateBack = {},
        onResetRequested = {},
        onResetConfirmed = {},
        onResetDismissed = {},
        onSettingsOpened = {},
        onSettingsDismissed = {},
        onShowAttackLinesChanged = {},
        onHapticsChanged = {},
    )
}
