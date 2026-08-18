package com.queens.puzzle.model

/**
 * The player's chosen theme.
 *
 * Seeded from the system at first launch, and set by the player from then on.
 */
enum class ThemePreference {
    Light,
    Dark,
    ;

    fun toggled(): ThemePreference = if (this == Light) Dark else Light

    val isDark: Boolean get() = this == Dark
}
