package com.queens.puzzle.ui.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Android system defaults — no custom font files needed
val DisplayFont = FontFamily.Serif
val BodyFont = FontFamily.SansSerif

/** Times and counters are monospaced so digits do not shift width as they tick. */
val NumericFont = FontFamily.Monospace

val QueensTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Medium,
        fontSize = 32.sp, lineHeight = 40.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFont, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFont, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFont, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
)
