package com.queens.puzzle.ui.game

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.queens.puzzle.R
import com.queens.puzzle.ui.designsystem.component.AlertBadge
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewSurface

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

@PreviewLightDark
@Composable
private fun ResetConfirmDialogPreview() {
    QueensPreviewSurface {
        ResetConfirmDialog(queensPlaced = 5, onConfirm = {}, onDismiss = {})
    }
}

/** The singular case, which takes different copy rather than "1 queens". */
@Preview
@Composable
private fun ResetConfirmDialogSingularPreview() {
    QueensPreviewSurface {
        ResetConfirmDialog(queensPlaced = 1, onConfirm = {}, onDismiss = {})
    }
}
