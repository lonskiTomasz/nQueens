package com.queens.puzzle.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queens.puzzle.R
import com.queens.puzzle.model.GameSettings
import com.queens.puzzle.core.designsystem.preview.QueensPreviewSurface
import com.queens.puzzle.core.designsystem.theme.Dimens
import com.queens.puzzle.core.designsystem.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSettingsSheet(
    settings: GameSettings,
    onShowAttackLinesChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        GameSettingsContent(
            settings = settings,
            onShowAttackLinesChanged = onShowAttackLinesChanged,
            onHapticsChanged = onHapticsChanged,
            onSoundChanged = onSoundChanged,
            onDone = onDismiss,
        )
    }
}

@Composable
private fun GameSettingsContent(
    settings: GameSettings,
    onShowAttackLinesChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(
                start = Spacing.ScreenPaddingHorizontal,
                end = Spacing.ScreenPaddingHorizontal,
                bottom = Spacing.ActionGap,
            ),
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
            HorizontalDivider()
            SettingRow(
                title = stringResource(R.string.settings_sound),
                detail = stringResource(R.string.settings_sound_detail),
                checked = settings.soundEnabled,
                onCheckedChange = onSoundChanged,
            )
        }

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.PrimaryButtonHeight),
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
            .padding(
                horizontal = Spacing.RowPaddingHorizontal,
                vertical = Spacing.RowPaddingVertical,
            )
            .semantics(mergeDescendants = true) { role = Role.Switch },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.RowControlGap),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.background,
                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@PreviewLightDark
@Composable
private fun GameSettingsContentPreview() {
    QueensPreviewSurface(padding = 0.dp) {
        GameSettingsContent(
            settings = GameSettings(showAttackLines = true, hapticsEnabled = false, soundEnabled = false),
            onShowAttackLinesChanged = {},
            onHapticsChanged = {},
            onSoundChanged = {},
            onDone = {},
        )
    }
}

