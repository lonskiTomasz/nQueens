package com.queens.puzzle.ui

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.queens.puzzle.core.designsystem.theme.QueensTheme
import com.queens.puzzle.ui.navigation.QueensNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        applyEdgeToEdge(darkTheme = resources.configuration.isSystemInDarkTheme)
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { viewModel.themeState.value is ThemeState.Loading }

        setContent {
            val themeState by viewModel.themeState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            // Held behind the splash screen until the stored choice arrives
            (themeState as? ThemeState.Ready)?.let { ready ->
                val darkTheme = ready.theme.shouldUseDarkTheme(systemDark)

                SideEffect { applyEdgeToEdge(darkTheme) }

                QueensTheme(darkTheme = darkTheme) {
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

    private fun applyEdgeToEdge(darkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
                detectDarkMode = { darkTheme },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = NavigationBarLightScrim,
                darkScrim = NavigationBarDarkScrim,
                detectDarkMode = { darkTheme },
            ),
        )
    }
}

private val Configuration.isSystemInDarkTheme: Boolean
    get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

// androidx.activity's own navigation bar scrims, which are private to that library.
private val NavigationBarLightScrim = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val NavigationBarDarkScrim = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
