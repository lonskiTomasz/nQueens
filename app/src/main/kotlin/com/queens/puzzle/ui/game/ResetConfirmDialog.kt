package com.queens.puzzle.ui.game

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.queens.puzzle.R
import com.queens.puzzle.ui.designsystem.component.AlertBadge

/**
 * Confirmation for the destructive reset.
 *
 * A dialog inside the game screen rather than a destination: it is transient UI over a board
 * that stays mounted underneath, and making it a destination would tear the board down and
 * rebuild it just to show a scrim.
 */
@Composable
fun ResetConfirmDialog(
    queensPlaced: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { AlertBadge() },
        title = {
            Text(
                text = stringResource(R.string.reset_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Text(
                text = pluralStringResource(R.plurals.reset_message, queensPlaced, queensPlaced),
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.reset_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.reset_cancel))
            }
        },
    )
}
