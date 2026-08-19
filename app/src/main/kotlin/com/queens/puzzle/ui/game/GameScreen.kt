package com.queens.puzzle.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
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
import com.queens.puzzle.ui.designsystem.component.TimerChip
import com.queens.puzzle.ui.designsystem.preview.PreviewSolvedQueens
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewScreen
import com.queens.puzzle.ui.designsystem.preview.previewGameUiState
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

/**
 * Below this the window is too short to stack the board between the header and the buttons —
 * a rotated phone, mostly.
 */
private val CompactHeight = 480.dp

/** A controls column narrower than this is not worth the width it takes off the board. */
private val MinControlsWidth = 220.dp

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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Landscape gives the board the height and the controls the width left over. Both
            // conditions matter: a nearly square window is wider than it is tall and would
            // still leave the controls a sliver.
            val sideBySide =
                maxHeight < CompactHeight && maxWidth >= maxHeight + MinControlsWidth

            Column(Modifier.fillMaxSize()) {
                GameTopBar(
                    title = stringResource(R.string.game_board_label, uiState.boardSize.value),
                    elapsed = DurationFormatter.format(uiState.elapsedMillis),
                    onNavigateBack = onNavigateBack,
                    onSettingsOpened = onSettingsOpened,
                )

                if (sideBySide) {
                    GameContentSideBySide(uiState, onAction, onResetRequested)
                } else {
                    GameContentStacked(uiState, onAction, onResetRequested)
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
private fun ColumnScope.GameContentStacked(
    uiState: GameUiState,
    onAction: (GameAction) -> Unit,
    onResetRequested: () -> Unit,
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
    ) {
        QueensRemaining(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            BoardGrid(
                boardSize = uiState.boardSize,
                squares = uiState.squares,
                onSquareClick = { onAction(GameAction.TapSquare(it)) },
                enabled = !uiState.isSolved,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 16.dp),
            )

            ConflictBannerSlot(
                hasConflicts = uiState.hasConflicts,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            )
        }
    }

    GameActions(
        uiState = uiState,
        onAction = onAction,
        onResetRequested = onResetRequested,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
    )
}

@Composable
private fun ColumnScope.GameContentSideBySide(
    uiState: GameUiState,
    onAction: (GameAction) -> Unit,
    onResetRequested: () -> Unit,
) {
    Row(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 20.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        BoardGrid(
            boardSize = uiState.boardSize,
            squares = uiState.squares,
            onSquareClick = { onAction(GameAction.TapSquare(it)) },
            enabled = !uiState.isSolved,
            // Square off the height, which is the scarce axis here.
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            QueensRemaining(uiState = uiState, modifier = Modifier.fillMaxWidth())

            ConflictBannerSlot(
                hasConflicts = uiState.hasConflicts,
                modifier = Modifier.padding(top = 12.dp),
            )

            Spacer(Modifier.weight(1f))

            GameActions(
                uiState = uiState,
                onAction = onAction,
                onResetRequested = onResetRequested,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun QueensRemaining(uiState: GameUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
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
}

@Composable
private fun GameActions(
    uiState: GameUiState,
    onAction: (GameAction) -> Unit,
    onResetRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
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

private const val BANNER_FADE_MILLIS = 120

@Composable
private fun ConflictBannerSlot(
    hasConflicts: Boolean,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (hasConflicts) 1f else 0f,
        animationSpec = tween(durationMillis = BANNER_FADE_MILLIS),
        label = "conflictBanner",
    )

    ConflictBanner(
        modifier = modifier
            .alpha(alpha)
            // Invisible to the eye is invisible to a screen reader: a live region left in the
            // tree would announce a conflict that is no longer there.
            .then(if (hasConflicts) Modifier else Modifier.clearAndSetSemantics { }),
    )
}

private val BannerBadgeSize = 24.dp

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

/** Rotated: the board keeps the height and the controls take the width it leaves. */
@Preview(widthDp = 740, heightDp = 360)
@Composable
private fun GameScreenLandscapePreview() {
    QueensPreviewScreen {
        PreviewGameScreen(previewGameUiState())
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
