package com.queens.puzzle.ui.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.queens.puzzle.ui.designsystem.preview.QueensPreviewSurface

/** A board-size pill on the home screen. 48 dp tall, so it is already a legal touch target. */
@Composable
fun SizeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minWidth = 56.dp)
            .semantics {
                role = Role.RadioButton
                this.selected = selected
                if (contentDescription != null) this.contentDescription = contentDescription
            },
        shape = RoundedCornerShape(percent = 50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@PreviewLightDark
@Composable
private fun SizeChipPreview() {
    QueensPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SizeChip(label = "4", selected = false, onClick = {})
            SizeChip(label = "8", selected = true, onClick = {})
            SizeChip(label = "12", selected = false, onClick = {})
        }
    }
}
