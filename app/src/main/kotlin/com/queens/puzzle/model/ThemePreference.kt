package com.queens.puzzle.model

enum class ThemePreference {
    Light,
    Dark,
    ;

    fun toggled(): ThemePreference = if (this == Light) Dark else Light

    val isDark: Boolean get() = this == Dark
}
