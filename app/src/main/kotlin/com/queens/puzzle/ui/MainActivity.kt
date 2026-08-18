package com.queens.puzzle.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.model.ThemePreference
import com.queens.puzzle.ui.designsystem.theme.QueensTheme
import com.queens.puzzle.ui.navigation.QueensNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val themeState by viewModel.themeState.collectAsStateWithLifecycle()

            val systemDark = isSystemInDarkTheme()
            LaunchedEffect(systemDark) {
                viewModel.seedTheme(
                    if (systemDark) ThemePreference.Dark else ThemePreference.Light
                )
            }

            // Held until the stored choice arrives, so the first frame is never the wrong scheme.
            if (themeState is ThemeState.Ready) {
                QueensTheme(darkTheme = (themeState as ThemeState.Ready).theme.isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        QueensNavHost()
                    }
                }
            }
        }
    }
}
