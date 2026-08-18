package com.queens.puzzle.ui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Roles with no standard M3 slot, carried alongside the scheme rather than smuggled into an
 * unrelated one (§12, row 9).
 */
data class QueensExtendedColors(
    val success: Color,
    val boardSquareLight: Color,
    val boardSquareDark: Color,
    val queenOnLightSquare: Color,
    val queenOnDarkSquare: Color,
    val queenConflict: Color,
    val winHeadline: Color,
    val winGradientTop: Color,
)

private val LightExtended = QueensExtendedColors(
    success = SuccessGreen,
    boardSquareLight = BoardSquareLight,
    boardSquareDark = BoardSquareDark,
    queenOnLightSquare = QueenOnLightSquare,
    queenOnDarkSquare = QueenOnDarkSquare,
    queenConflict = QueenConflictLight,
    winHeadline = WinHeadlineLight,
    winGradientTop = WinGradientTopLight,
)

private val DarkExtended = QueensExtendedColors(
    success = SuccessGreenDark,
    boardSquareLight = BoardSquareLightDim,
    boardSquareDark = BoardSquareDarkDim,
    queenOnLightSquare = QueenOnDimSquare,
    queenOnDarkSquare = QueenOnDimSquare,
    queenConflict = QueenConflictDark,
    winHeadline = TertiaryDark,
    winGradientTop = WinGradientTopDark,
)

val LocalQueensColors = staticCompositionLocalOf { LightExtended }

private val LightScheme = lightColorScheme(
    primary = PrimaryLight, onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight, onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight, onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight, onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight, onTertiary = OnTertiaryLight, // celebration / win accent
    tertiaryContainer = TertiaryContainerLight, onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight, onError = OnErrorLight, // conflict — reused as the "warning" role
    errorContainer = ErrorContainerLight, onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight, onBackground = OnBackgroundLight,
    surface = SurfaceLight, onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight, onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight, outlineVariant = OutlineVariantLight,
)

private val DarkScheme = darkColorScheme(
    primary = PrimaryDark, onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark, onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark, onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark, onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark, onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark, onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark, onError = OnErrorDark,
    errorContainer = ErrorContainerDark, onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark, onBackground = OnBackgroundDark,
    surface = SurfaceDark, onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark, onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark, outlineVariant = OutlineVariantDark,
)

/**
 * Dynamic colour is deliberately absent (§12, row 10): the board's squares and the
 * conflict/celebration accents are the product's identity, and Material You would repaint
 * exactly the semantics the player reads.
 *
 * [darkTheme] defaults to the system only so previews have something to follow — the app
 * passes the player's stored choice.
 */
@Composable
fun QueensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val extended = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalQueensColors provides extended) {
        MaterialTheme(colorScheme = scheme, typography = QueensTypography, content = content)
    }
}

/** The extended roles, read the same way as `MaterialTheme.colorScheme`. */
object QueensTheme {
    val extendedColors: QueensExtendedColors
        @Composable @ReadOnlyComposable get() = LocalQueensColors.current
}
