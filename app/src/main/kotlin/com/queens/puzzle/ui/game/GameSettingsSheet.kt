package com.queens.puzzle.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queens.puzzle.R
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewSurface

/**
 * Gameplay options.
 *
 * A sheet inside the game screen, not a destination — see [ResetConfirmDialog] for why.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsSheet(
    settings: GameSettings,
    onShowAttackLinesChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
    ) {
        GameSettingsContent(
            settings = settings,
            onShowAttackLinesChanged = onShowAttackLinesChanged,
            onHapticsChanged = onHapticsChanged,
            onDone = onDismiss,
        )
    }
}

/**
 * The sheet's contents, held apart from the sheet itself so they can be previewed: a
 * `ModalBottomSheet` renders as an empty window in the preview tooling, its contents do not.
 */
@Composable
private fun GameSettingsContent(
    settings: GameSettings,
    onShowAttackLinesChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
        )

        OutlinedCard(shape = RoundedCornerShape(20.dp)) {
            SettingRow(
                title = stringResource(R.string.settings_attack_lines),
                detail = stringResource(R.string.settings_attack_lines_detail),
                checked = settings.showAttackLines,
                onCheckedChange = onShowAttackLinesChanged,
            )
            HorizontalDivider()
            SettingRow(
                title = stringResource(R.string.settings_haptics),
                detail = stringResource(R.string.settings_haptics_detail),
                checked = settings.hapticsEnabled,
                onCheckedChange = onHapticsChanged,
            )
        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(percent = 50),
        ) {
            Text(
                text = stringResource(R.string.settings_done),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .semantics(mergeDescendants = true) { role = Role.Switch },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@PreviewLightDark
@Composable
private fun GameSettingsContentPreview() {
    QueensPreviewSurface(padding = 0.dp) {
        GameSettingsContent(
            settings = GameSettings(showAttackLines = true, hapticsEnabled = false),
            onShowAttackLinesChanged = {},
            onHapticsChanged = {},
            onDone = {},
        )
    }
}

@Preview
@Composable
private fun GameSettingsContentBothOnPreview() {
    QueensPreviewSurface(padding = 0.dp) {
        GameSettingsContent(
            settings = GameSettings(showAttackLines = true, hapticsEnabled = true),
            onShowAttackLinesChanged = {},
            onHapticsChanged = {},
            onDone = {},
        )
    }
}
