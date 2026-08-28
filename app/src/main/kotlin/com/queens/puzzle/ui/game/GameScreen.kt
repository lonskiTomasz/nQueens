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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.R
import com.queens.puzzle.core.util.time.DurationFormatter
import com.queens.puzzle.domain.game.GameAction
import com.queens.puzzle.model.PuzzleType
import com.queens.puzzle.ui.game.board.BoardGrid
import com.queens.puzzle.core.designsystem.component.AlertBadge
import com.queens.puzzle.core.designsystem.component.PiecePips
import com.queens.puzzle.core.designsystem.component.TimerChip
import com.queens.puzzle.core.designsystem.preview.PreviewState
import com.queens.puzzle.core.designsystem.preview.QueensPreviewScreen
import com.queens.puzzle.core.designsystem.theme.Dimens
import com.queens.puzzle.core.designsystem.theme.Spacing
import com.queens.puzzle.ui.game.feedback.rememberGameFeedback
import com.queens.puzzle.ui.game.feedback.rememberGameSound
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    boardSize: Int,
    gameId: Long,
    puzzleType: PuzzleType,
    onNavigateBack: () -> Unit,
    onNavigateToWin: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel<GameViewModel, GameViewModel.Factory>(
        creationCallback = { factory -> factory.create(boardSize, gameId, puzzleType) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val elapsedMillis = viewModel.elapsedMillis.collectAsStateWithLifecycle()
    val feedback = rememberGameFeedback(enabled = uiState.settings.hapticsEnabled)
    val sound = rememberGameSound(enabled = uiState.settings.soundEnabled)
    val snackbarHostState = remember { SnackbarHostState() }
    val boardFullMessage = stringResource(R.string.game_board_full)

    LifecycleStartEffect(viewModel) {
        viewModel.onScreenStarted()
        onStopOrDispose { viewModel.onScreenStopped() }
    }

    LaunchedEffect(viewModel, feedback, sound) {
        var snackbarJob: Job? = null
        viewModel.effects.collect { effect ->
            when (effect) {
                GameEffect.HapticPlace -> feedback.place()
                GameEffect.HapticConflict -> feedback.conflict()
                GameEffect.SoundPlace -> sound.place()
                GameEffect.BoardFull -> {
                    feedback.conflict()
                    snackbarJob?.cancel()
                    snackbarJob = launch { snackbarHostState.showSnackbar(boardFullMessage) }
                }
                GameEffect.CelebrateWin -> feedback.win()
                is GameEffect.NavigateToWin -> onNavigateToWin(effect.solveId)
            }
        }
    }

    GameScreen(
        uiState = uiState,
        elapsedMillis = { elapsedMillis.value },
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        onResetRequested = viewModel::onResetRequested,
        onResetConfirmed = viewModel::onResetConfirmed,
        onResetDismissed = viewModel::onResetDismissed,
        onSettingsOpened = viewModel::onSettingsOpened,
        onSettingsDismissed = viewModel::onSettingsDismissed,
        onShowAttackLinesChanged = viewModel::onShowAttackLinesChanged,
        onHapticsChanged = viewModel::onHapticsChanged,
        onSoundChanged = viewModel::onSoundChanged,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

/** A controls column narrower than this is not worth the width it takes off the board. */
private val MinControlsWidth = 220.dp

@Composable
fun GameScreen(
    uiState: GameUiState,
    elapsedMillis: () -> Long,
    onAction: (GameAction) -> Unit,
    onNavigateBack: () -> Unit,
    onResetRequested: () -> Unit,
    onResetConfirmed: () -> Unit,
    onResetDismissed: () -> Unit,
    onSettingsOpened: () -> Unit,
    onSettingsDismissed: () -> Unit,
    onShowAttackLinesChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
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
                maxHeight < Dimens.CompactHeight && maxWidth >= maxHeight + MinControlsWidth

            Column(Modifier.fillMaxSize()) {
                GameTopBar(
                    title = stringResource(R.string.game_board_label, uiState.boardSize.value),
                    elapsed = elapsedMillis,
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
            piecesPlaced = uiState.piecesPlaced,
            onConfirm = onResetConfirmed,
            onDismiss = onResetDismissed,
        )
    }

    if (uiState.isSettingsSheetVisible) {
        GameSettingsSheet(
            settings = uiState.settings,
            onShowAttackLinesChanged = onShowAttackLinesChanged,
            onHapticsChanged = onHapticsChanged,
            onSoundChanged = onSoundChanged,
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
        PiecesRemaining(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.ScreenPaddingHorizontal),
        )

        Spacer(Modifier.height(Spacing.BlockGap))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BoardGrid(
                boardSize = uiState.boardSize,
                puzzleType = uiState.puzzleType,
                squares = uiState.squares,
                onSquareClick = { onAction(GameAction.TapSquare(it)) },
                enabled = uiState.isBoardEnabled,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = Spacing.ScreenPaddingHorizontal),
            )

            ConflictBannerSlot(
                hasConflicts = uiState.hasConflicts,
                puzzleType = uiState.puzzleType,
                modifier = Modifier.padding(
                    start = Spacing.ScreenPaddingHorizontal,
                    end = Spacing.ScreenPaddingHorizontal,
                    top = Spacing.ContentGap,
                ),
            )
        }
    }

    Spacer(Modifier.height(Spacing.BlockGap))

    GameActions(
        uiState = uiState,
        onAction = onAction,
        onResetRequested = onResetRequested,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = Spacing.ScreenPaddingHorizontal,
                end = Spacing.ScreenPaddingHorizontal,
                bottom = Spacing.ScreenPaddingVertical,
            ),
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
            .padding(
                start = Spacing.ScreenPaddingHorizontal,
                end = Spacing.ScreenPaddingHorizontal,
                bottom = Spacing.BlockGap,
            ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.BlockGap),
    ) {
        BoardGrid(
            boardSize = uiState.boardSize,
            puzzleType = uiState.puzzleType,
            squares = uiState.squares,
            onSquareClick = { onAction(GameAction.TapSquare(it)) },
            enabled = uiState.isBoardEnabled,
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
            PiecesRemaining(uiState = uiState, modifier = Modifier.fillMaxWidth())

            ConflictBannerSlot(
                hasConflicts = uiState.hasConflicts,
                puzzleType = uiState.puzzleType,
                modifier = Modifier.padding(top = Spacing.ContentGap),
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
private fun PiecesRemaining(uiState: GameUiState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = pluralStringResource(
                when (uiState.puzzleType) {
                    PuzzleType.Queens -> R.plurals.game_queens_left
                    PuzzleType.Knights -> R.plurals.game_knights_left
                },
                uiState.piecesRemaining,
                uiState.piecesRemaining,
            ),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            ),
        )
        PiecePips(total = uiState.boardSize.value, placed = uiState.piecesPlaced)
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
        horizontalArrangement = Arrangement.spacedBy(Spacing.ButtonGap),
    ) {
        OutlinedButton(
            onClick = { onAction(GameAction.Undo) },
            enabled = uiState.canUndo,
            modifier = Modifier
                .weight(1f)
                .height(Dimens.SecondaryButtonHeight),
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text(stringResource(R.string.game_undo))
        }
        OutlinedButton(
            onClick = onResetRequested,
            enabled = uiState.canReset,
            modifier = Modifier
                .weight(1f)
                .height(Dimens.SecondaryButtonHeight),
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text(stringResource(R.string.game_reset))
        }
    }
}

@Composable
private fun GameTopBar(
    title: String,
    elapsed: () -> Long,
    onNavigateBack: () -> Unit,
    onSettingsOpened: () -> Unit,
) {
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
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        TimerChip(
            time = DurationFormatter.format(elapsed()),
            contentDescription = stringResource(R.string.game_elapsed),
        )
        Box(
            modifier = Modifier
                .padding(start = Spacing.IconButtonInset)
                .size(Dimens.MinTouchTarget)
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

@Composable
private fun ConflictBannerSlot(
    hasConflicts: Boolean,
    puzzleType: PuzzleType,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(
        targetValue = if (hasConflicts) 1f else 0f,
        animationSpec = tween(durationMillis = 120),
        label = "conflictBanner",
    )

    ConflictBanner(
        puzzleType = puzzleType,
        modifier = modifier
            .alpha(alpha)
            // Invisible to the eye is invisible to a screen reader: a live region left in the
            // tree would announce a conflict that is no longer there.
            .then(if (hasConflicts) Modifier else Modifier.clearAndSetSemantics { }),
    )
}

@Composable
private fun ConflictBanner(puzzleType: PuzzleType, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(12.dp))
            .padding(
                horizontal = Spacing.ContainerPaddingHorizontal,
                vertical = Spacing.ContainerPaddingVertical,
            )
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.IconTextGap),
    ) {
        AlertBadge(
            size = 24.dp,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            textStyle = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = stringResource(
                when (puzzleType) {
                    PuzzleType.Queens -> R.string.game_conflict
                    PuzzleType.Knights -> R.string.game_conflict_knights
                },
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@PreviewLightDark
@Composable
private fun GameScreenPreview(
    @PreviewParameter(GameScreenPreviewProvider::class) preview: PreviewState<GameUiState>,
) {
    QueensPreviewScreen {
        PreviewGameScreen(preview.state)
    }
}

@Preview(widthDp = 740, heightDp = 360)
@Composable
private fun GameScreenLandscapePreview() {
    QueensPreviewScreen {
        PreviewGameScreen(previewGameUiState())
    }
}

@Composable
private fun PreviewGameScreen(uiState: GameUiState, elapsedMillis: Long = 134_000L) {
    GameScreen(
        uiState = uiState,
        elapsedMillis = { elapsedMillis },
        onAction = {},
        onNavigateBack = {},
        onResetRequested = {},
        onResetConfirmed = {},
        onResetDismissed = {},
        onSettingsOpened = {},
        onSettingsDismissed = {},
        onShowAttackLinesChanged = {},
        onHapticsChanged = {},
        onSoundChanged = {},
    )
}
