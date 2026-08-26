package com.queens.puzzle.model

enum class ThemePreference {
    System,
    Light,
    Dark,
    ;

    fun shouldUseDarkTheme(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }
}
