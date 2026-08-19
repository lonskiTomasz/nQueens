package com.queens.puzzle.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.unit.DpSize
import com.queens.puzzle.ui.designsystem.theme.QueensTheme

/**
 * Themes [content] and scales a window of [size] into the test device, so one device covers
 * every shape rather than needing an emulator per orientation. A null [size] means the
 * device's own window.
 */
@Composable
fun ForcedWindow(size: DpSize?, content: @Composable () -> Unit) {
    if (size == null) {
        QueensTheme(content = content)
    } else {
        DeviceConfigurationOverride(DeviceConfigurationOverride.ForcedSize(size)) {
            QueensTheme(content = content)
        }
    }
}
